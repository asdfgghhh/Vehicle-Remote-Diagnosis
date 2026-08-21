package com.vrd.diagnosis.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.diagnosis.dto.UdsRequest;
import com.vrd.diagnosis.dto.UdsResponse;
import com.vrd.diagnosis.entity.UdsDiagnosisSession;
import com.vrd.diagnosis.entity.UdsDtcRecord;
import com.vrd.diagnosis.kafka.UdsCommandProducer;
import com.vrd.diagnosis.mapper.UdsDiagnosisSessionMapper;
import com.vrd.diagnosis.mapper.UdsDtcRecordMapper;
import com.vrd.diagnosis.service.UdsDiagnosisService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class UdsDiagnosisServiceImpl
extends ServiceImpl<UdsDiagnosisSessionMapper, UdsDiagnosisSession>
implements UdsDiagnosisService {
    private static final Logger log = LoggerFactory.getLogger(UdsDiagnosisServiceImpl.class);
    private final UdsDiagnosisSessionMapper sessionMapper;
    private final UdsDtcRecordMapper dtcRecordMapper;
    private final UdsCommandProducer udsCommandProducer;
    private final Map<String, String> securitySeedCache = new ConcurrentHashMap<String, String>();
    private final Map<String, Integer> ecuSessionState = new ConcurrentHashMap<String, Integer>();
    private final Map<String, CompletableFuture<UdsResponse>> pendingResponses = new ConcurrentHashMap<String, CompletableFuture<UdsResponse>>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${vrd.uds.mock-enabled:true}")
    private boolean mockEnabled;

    @Value("${vrd.uds.response-timeout-ms:8000}")
    private long responseTimeoutMs;

    public UdsDiagnosisServiceImpl(UdsDiagnosisSessionMapper sessionMapper, UdsDtcRecordMapper dtcRecordMapper, UdsCommandProducer udsCommandProducer) {
        this.sessionMapper = sessionMapper;
        this.dtcRecordMapper = dtcRecordMapper;
        this.udsCommandProducer = udsCommandProducer;
    }

    @Override
    public UdsResponse diagnosticSessionControl(UdsRequest request) {
        String traceId = this.generateTraceId();
        int sessionType = request.getSessionType() != null ? request.getSessionType() : 1;
        String ecuKey = request.getVin() + ":" + request.getEcuType();
        this.ecuSessionState.put(ecuKey, sessionType);
        byte[] udaRequest = new byte[]{16, (byte)sessionType};
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 16, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 16, sessionType, startMs);
        }
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 16, "DIAGNOSTIC_SESSION_CONTROL_ACK", System.currentTimeMillis());
        response.setSessionStatus(this.getSessionTypeName(sessionType));
        this.saveSession(request, traceId, 16, sessionType, "SESSION_ACK", 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse ecuReset(UdsRequest request) {
        String traceId = this.generateTraceId();
        int resetType = request.getResetType() != null ? request.getResetType() : 3;
        byte[] udaRequest = new byte[]{17, (byte)resetType};
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 17, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 17, resetType, startMs);
        }
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 17, "ECU_RESET_ACK", System.currentTimeMillis());
        this.saveSession(request, traceId, 17, resetType, "RESET_ACK", 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse securityAccessRequestSeed(UdsRequest request) {
        String traceId = this.generateTraceId();
        int securityLevel = request.getSecurityLevel() != null ? request.getSecurityLevel() : 1;
        int subFunction = securityLevel << 1 | 1;
        byte[] udaRequest = new byte[]{39, (byte)subFunction};
        String seed = this.generateSeed();
        String cacheKey = this.securityCacheKey(request, securityLevel);
        this.securitySeedCache.put(cacheKey, seed);
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 39, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 39, subFunction, startMs);
        }
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 39, "SEED:" + seed, System.currentTimeMillis());
        response.setSecurityStatus("SEED_RECEIVED");
        response.setParsedData(Map.of("seed", seed, "traceId", traceId));
        this.saveSession(request, traceId, 39, subFunction, "SEED:" + seed, 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse securityAccessSendKey(UdsRequest request) {
        String traceId = this.generateTraceId();
        int securityLevel = request.getSecurityLevel() != null ? request.getSecurityLevel() : 1;
        int subFunction = securityLevel << 1 | 2;
        String cacheKey = this.securityCacheKey(request, securityLevel);
        String cachedSeed = this.securitySeedCache.get(cacheKey);
        boolean keyValid = this.validateKey(cachedSeed, request.getSecurityKey());
        byte[] udaRequest = this.buildKeySendFrame(securityLevel, request.getSecurityKey());
        if (!keyValid) {
            this.securitySeedCache.remove(cacheKey);
            UdsResponse nrc = UdsResponse.negativeResponse(traceId, request.getVin(), 39, 53, "\u5bc6\u94a5\u9a8c\u8bc1\u5931\u8d25", System.currentTimeMillis());
            nrc.setSecurityStatus("ACCESS_DENIED");
            this.saveSession(request, traceId, 39, subFunction, "KEY_INVALID", 0, 53, System.currentTimeMillis());
            return nrc;
        }
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 39, udaRequest);
        this.securitySeedCache.remove(cacheKey);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 39, subFunction, startMs);
        }
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 39, "SECURITY_ACCESS_GRANTED", System.currentTimeMillis());
        response.setSecurityStatus("ACCESS_GRANTED");
        this.saveSession(request, traceId, 39, subFunction, "ACCESS_GRANTED", 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse readDataByIdentifier(UdsRequest request) {
        String traceId = this.generateTraceId();
        int did = request.getDataIdentifier() != null ? request.getDataIdentifier() : 61840;
        byte[] udaRequest = new byte[]{34, (byte)(did >> 8 & 0xFF), (byte)(did & 0xFF)};
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 34, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 34, did, startMs);
        }
        String mockData = "DID_" + String.format("%04X", did) + "_DATA";
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 34, mockData, System.currentTimeMillis());
        this.saveSession(request, traceId, 34, did, mockData, 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse writeDataByIdentifier(UdsRequest request) {
        String traceId = this.generateTraceId();
        int did = request.getDataIdentifier() != null ? request.getDataIdentifier() : 1;
        byte[] udaRequest = this.buildWriteDidFrame(did, request.getRequestData());
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 46, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 46, did, startMs);
        }
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 46, "WRITE_ACK", System.currentTimeMillis());
        this.saveSession(request, traceId, 46, did, "WRITE_ACK", 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse readDtcInformation(UdsRequest request) {
        String traceId = this.generateTraceId();
        int subFunction = 1;
        byte[] udaRequest = new byte[]{25, (byte)subFunction, (byte)(request.getDtcStatusMask() != null ? request.getDtcStatusMask() : 255)};
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 25, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 25, subFunction, startMs);
        }
        String dtcData = "DTC_COUNT:3|P0300:Confirmed|U0100:Pending|C1201:Permanent";
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 25, dtcData, System.currentTimeMillis());
        this.saveDtcRecords(request, dtcData, traceId);
        this.saveSession(request, traceId, 25, subFunction, dtcData, 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse clearDiagnosticInformation(UdsRequest request) {
        String traceId = this.generateTraceId();
        byte[] udaRequest = new byte[]{20, -1, -1, -1};
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 20, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 20, null, startMs);
        }
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 20, "CLEAR_ACK", System.currentTimeMillis());
        this.saveSession(request, traceId, 20, null, "CLEAR_ACK", 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse routineControl(UdsRequest request) {
        String traceId = this.generateTraceId();
        int routineId = request.getRoutineId() != null ? request.getRoutineId() : 57345;
        byte[] udaRequest = new byte[]{49, 1, (byte)(routineId >> 8 & 0xFF), (byte)(routineId & 0xFF)};
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 49, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 49, routineId, startMs);
        }
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 49, "ROUTINE_STARTED:" + String.format("%04X", routineId), System.currentTimeMillis());
        this.saveSession(request, traceId, 49, routineId, "ROUTINE_STARTED", 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse readMemoryByAddress(UdsRequest request) {
        String traceId = this.generateTraceId();
        byte[] udaRequest = this.buildMemoryReadFrame(request.getMemoryAddress(), request.getMemorySize());
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 35, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 35, null, startMs);
        }
        String mockData = "MEM_0x" + Long.toHexString(request.getMemoryAddress()) + "_SIZE_" + request.getMemorySize();
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 35, mockData, System.currentTimeMillis());
        this.saveSession(request, traceId, 35, null, mockData, 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse writeMemoryByAddress(UdsRequest request) {
        String traceId = this.generateTraceId();
        byte[] udaRequest = this.buildMemoryWriteFrame(request.getMemoryAddress(), request.getRequestData());
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 61, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 61, null, startMs);
        }
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 61, "MEM_WRITE_ACK", System.currentTimeMillis());
        this.saveSession(request, traceId, 61, null, "MEM_WRITE_ACK", 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse inputOutputControl(UdsRequest request) {
        String traceId = this.generateTraceId();
        byte[] udaRequest = new byte[]{47, (byte)(request.getDataIdentifier() != null ? request.getDataIdentifier() >> 8 & 0xFF : 0), (byte)(request.getDataIdentifier() != null ? request.getDataIdentifier() & 0xFF : 1), 3};
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 47, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 47, null, startMs);
        }
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 47, "IO_CONTROL_ACK", System.currentTimeMillis());
        this.saveSession(request, traceId, 47, null, "IO_CONTROL_ACK", 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse testerPresent(UdsRequest request) {
        String traceId = this.generateTraceId();
        byte[] udaRequest = new byte[]{62, 0};
        long startMs = System.currentTimeMillis();
        this.sendUdsCommand(request, traceId, 62, udaRequest);
        if (!this.mockEnabled) {
            return this.awaitResponse(request, traceId, 62, null, startMs);
        }
        UdsResponse response = UdsResponse.success(traceId, request.getVin(), 62, "TESTER_PRESENT_ACK", System.currentTimeMillis());
        this.saveSession(request, traceId, 62, null, "ACK", 1, null, System.currentTimeMillis());
        return response;
    }

    @Override
    public UdsResponse executeRequest(UdsRequest request) {
        if (request.getServiceId() == null) {
            return UdsResponse.error("NONE", request.getVin(), 0, "serviceId is required");
        }
        return switch (request.getServiceId()) {
            case 16 -> this.diagnosticSessionControl(request);
            case 17 -> this.ecuReset(request);
            case 39 -> {
                if (request.getSecurityKey() != null && !request.getSecurityKey().isEmpty()) {
                    yield this.securityAccessSendKey(request);
                }
                yield this.securityAccessRequestSeed(request);
            }
            case 34 -> this.readDataByIdentifier(request);
            case 46 -> this.writeDataByIdentifier(request);
            case 25 -> this.readDtcInformation(request);
            case 20 -> this.clearDiagnosticInformation(request);
            case 49 -> this.routineControl(request);
            case 35 -> this.readMemoryByAddress(request);
            case 61 -> this.writeMemoryByAddress(request);
            case 47 -> this.inputOutputControl(request);
            case 62 -> this.testerPresent(request);
            case 52, 53, 54, 55 -> UdsResponse.success(this.generateTraceId(), request.getVin(), request.getServiceId(), "DOWNLOAD_UPLOAD_ACK", System.currentTimeMillis());
            default -> UdsResponse.negativeResponse(this.generateTraceId(), request.getVin(), request.getServiceId(), 17, "\u670d\u52a1 0x" + Integer.toHexString(request.getServiceId()) + " \u6682\u4e0d\u652f\u6301", System.currentTimeMillis());
        };
    }

    @Override
    public Object querySessions(String vin, Integer page, Integer size) {
        Page<UdsDiagnosisSession> pageParam = new Page<>(page != null ? page.longValue() : 1L, size != null ? size.longValue() : 20L);
        LambdaQueryWrapper<UdsDiagnosisSession> wrapper = new LambdaQueryWrapper<>();
        if (vin != null && !vin.isEmpty()) {
            wrapper.eq(UdsDiagnosisSession::getVin, vin);
        }
        wrapper.orderByDesc(UdsDiagnosisSession::getRequestTime);
        return this.page(pageParam, wrapper);
    }

    @Override
    public void completeResponse(UdsResponse response) {
        if (response == null || response.getTraceId() == null) {
            log.warn("Ignore invalid UDS response: {}", response);
            return;
        }
        CompletableFuture<UdsResponse> future = this.pendingResponses.remove(response.getTraceId());
        if (future == null) {
            log.warn("No pending request for UDS response traceId={}, serviceId=0x{}", response.getTraceId(),
                    response.getServiceId() != null ? Integer.toHexString(response.getServiceId()) : "?");
            return;
        }
        future.complete(response);
        log.info("UDS response matched: traceId={}, serviceId=0x{}, success={}", response.getTraceId(),
                response.getServiceId() != null ? Integer.toHexString(response.getServiceId()) : "?",
                response.getSuccess());
    }

    private UdsResponse awaitResponse(UdsRequest request, String traceId, int serviceId, Integer subFunction, long startMs) {
        CompletableFuture<UdsResponse> future = new CompletableFuture<>();
        this.pendingResponses.put(traceId, future);
        try {
            UdsResponse response = future.get(this.responseTimeoutMs, TimeUnit.MILLISECONDS);
            if (response.getResponseTimeMs() == null) {
                response.setResponseTimeMs(System.currentTimeMillis() - startMs);
            }
            String responseData = response.getResponseData();
            boolean ok = Boolean.TRUE.equals(response.getSuccess());
            this.saveSession(request, traceId, serviceId, subFunction, responseData, ok ? 1 : 0,
                    response.getNegativeResponseCode(), System.currentTimeMillis() - startMs);
            if (serviceId == 25 && responseData != null) {
                this.saveDtcRecords(request, responseData, traceId);
            }
            return response;
        }
        catch (TimeoutException e) {
            log.warn("UDS response timeout: traceId={}, serviceId=0x{}, timeout={}ms", traceId, Integer.toHexString(serviceId), this.responseTimeoutMs);
            UdsResponse err = UdsResponse.error(traceId, request.getVin(), serviceId, "\u8f66\u7aef\u54cd\u5e94\u8d85\u65f6(" + this.responseTimeoutMs + "ms)\uff0c\u8bf7\u786e\u8ba4\u8f66\u8f86\u5728\u7ebf");
            err.setResponseTimeMs(System.currentTimeMillis() - startMs);
            this.saveSession(request, traceId, serviceId, subFunction, err.getNegativeResponseDesc(), 0, null, System.currentTimeMillis() - startMs);
            return err;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("UDS response wait interrupted: traceId={}", traceId, e);
            return UdsResponse.error(traceId, request.getVin(), serviceId, "\u7b49\u5f85\u8f66\u7aef\u54cd\u5e94\u88ab\u4e2d\u65ad");
        }
        catch (Exception e) {
            log.error("UDS response wait failed: traceId={}", traceId, e);
            return UdsResponse.error(traceId, request.getVin(), serviceId, "\u7b49\u5f85\u8f66\u7aef\u54cd\u5e94\u5f02\u5e38: " + e.getMessage());
        }
        finally {
            this.pendingResponses.remove(traceId);
        }
    }

    private String securityCacheKey(UdsRequest request, int securityLevel) {
        return (request.getVin() == null ? "UNKNOWN" : request.getVin()) + ":" + securityLevel;
    }

    @Async
    protected void sendUdsCommand(UdsRequest request, String traceId, int serviceId, byte[] udaRequest) {
        try {
            HashMap<String, Object> command = new HashMap<String, Object>();
            command.put("traceId", traceId);
            command.put("vin", request.getVin());
            command.put("vehicleId", request.getVehicleId());
            command.put("ecuType", request.getEcuType());
            command.put("serviceId", serviceId);
            command.put("subFunction", request.getSubFunction());
            command.put("requestHex", this.bytesToHex(udaRequest));
            command.put("timestamp", System.currentTimeMillis());
            this.udsCommandProducer.sendUdsCommand(request.getVin(), command);
            log.info("UDS command sent: traceId={}, serviceId=0x{}, vin={}", new Object[]{traceId, Integer.toHexString(serviceId), request.getVin()});
        }
        catch (Exception e) {
            log.error("Failed to send UDS command: traceId={}", (Object)traceId, (Object)e);
        }
    }

    private void saveSession(UdsRequest request, String traceId, int serviceId, Integer subFunction, String responseData, int success, Integer nrc, long responseTimeMs) {
        try {
            UdsDiagnosisSession session = new UdsDiagnosisSession();
            session.setTraceId(traceId);
            session.setVin(request.getVin());
            session.setVehicleId(request.getVehicleId());
            session.setEcuType(request.getEcuType());
            session.setServiceId(serviceId);
            session.setSubFunction(subFunction);
            session.setRequestData(request.getRequestData());
            session.setResponseData(responseData);
            session.setSuccess(success);
            session.setNegativeResponseCode(nrc);
            session.setResponseTimeMs(responseTimeMs);
            session.setOperator(request.getOperator());
            session.setRequestTime(LocalDateTime.now());
            session.setResponseTime(LocalDateTime.now());
            session.setCreateTime(LocalDateTime.now());
            this.sessionMapper.insert(session);
        }
        catch (Exception e) {
            log.error("Failed to save UDS session: traceId={}", (Object)traceId, (Object)e);
        }
    }

    private void saveDtcRecords(UdsRequest request, String dtcData, String traceId) {
        try {
            String[] parts = dtcData.split("\\|");
            for (int i = 1; i < parts.length; ++i) {
                String[] dtcParts = parts[i].split(":");
                if (dtcParts.length < 2) continue;
                UdsDtcRecord record = new UdsDtcRecord();
                record.setTraceId(traceId);
                record.setVin(request.getVin());
                record.setVehicleId(request.getVehicleId());
                record.setEcuType(request.getEcuType());
                record.setDtcCode(dtcParts[0]);
                String status = dtcParts[1];
                record.setFaultStatus(status.contains("Confirmed") ? 1 : (status.contains("Pending") ? 0 : 2));
                record.setSeverity(status.contains("Permanent") ? 1 : 2);
                record.setDtcDescription("DTC " + dtcParts[0]);
                record.setDetectionTime(LocalDateTime.now());
                record.setCreateTime(LocalDateTime.now());
                this.dtcRecordMapper.insert(record);
            }
        }
        catch (Exception e) {
            log.error("Failed to save DTC records: traceId={}", (Object)traceId, (Object)e);
        }
    }

    private String generateTraceId() {
        return "UDS_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateSeed() {
        long seed = this.secureRandom.nextLong() & 0xFFFFFFFFL;
        return String.format("%08X", seed);
    }

    private boolean validateKey(String seed, String key) {
        if (seed == null || key == null) {
            return false;
        }
        return key.length() >= 8;
    }

    private byte[] buildKeySendFrame(int securityLevel, String key) {
        byte[] keyBytes = this.hexToBytes(key);
        byte[] frame = new byte[2 + keyBytes.length];
        int subFunction = securityLevel << 1 | 2;
        frame[0] = 39;
        frame[1] = (byte)subFunction;
        System.arraycopy(keyBytes, 0, frame, 2, keyBytes.length);
        return frame;
    }

    private byte[] buildWriteDidFrame(int did, String data) {
        byte[] byArray;
        if (data != null) {
            byArray = this.hexToBytes(data);
        } else {
            byte[] byArray2 = new byte[1];
            byArray = byArray2;
            byArray2[0] = 0;
        }
        byte[] dataBytes = byArray;
        byte[] frame = new byte[3 + dataBytes.length];
        frame[0] = 46;
        frame[1] = (byte)(did >> 8 & 0xFF);
        frame[2] = (byte)(did & 0xFF);
        System.arraycopy(dataBytes, 0, frame, 3, dataBytes.length);
        return frame;
    }

    private byte[] buildMemoryReadFrame(Long address, Integer size) {
        byte[] frame = new byte[]{35, 20, (byte)(address >> 24 & 0xFFL), (byte)(address >> 16 & 0xFFL), (byte)(address >> 8 & 0xFFL), (byte)(address & 0xFFL)};
        return frame;
    }

    private byte[] buildMemoryWriteFrame(Long address, String data) {
        byte[] byArray;
        if (data != null) {
            byArray = this.hexToBytes(data);
        } else {
            byte[] byArray2 = new byte[1];
            byArray = byArray2;
            byArray2[0] = 0;
        }
        byte[] dataBytes = byArray;
        byte[] frame = new byte[6 + dataBytes.length];
        frame[0] = 61;
        frame[1] = 20;
        frame[2] = (byte)(address >> 24 & 0xFFL);
        frame[3] = (byte)(address >> 16 & 0xFFL);
        frame[4] = (byte)(address >> 8 & 0xFFL);
        frame[5] = (byte)(address & 0xFFL);
        System.arraycopy(dataBytes, 0, frame, 6, dataBytes.length);
        return frame;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }

    private byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        hex = hex.replaceAll("\\s+", "");
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private String getSessionTypeName(int type) {
        return switch (type) {
            case 1 -> "DEFAULT";
            case 2 -> "PROGRAMMING";
            case 3 -> "EXTENDED_DIAGNOSTIC";
            case 4 -> "SAFETY_SYSTEM";
            default -> "UNKNOWN_0x" + Integer.toHexString(type);
        };
    }
}

