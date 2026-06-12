package com.vrd.dbc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageKeyUtils;
import com.vrd.common.storage.StorageService;
import com.vrd.dbc.entity.DbcFile;
import com.vrd.dbc.entity.DispatchLog;
import com.vrd.dbc.mapper.DbcFileMapper;
import com.vrd.dbc.mapper.DispatchLogMapper;
import com.vrd.dbc.service.DbcFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DbcFileServiceImpl extends ServiceImpl<DbcFileMapper, DbcFile> implements DbcFileService {

    @Autowired
    private DispatchLogMapper dispatchLogMapper;

    @Autowired
    private StorageService storageService;

    @Override
    public Page<DbcFile> page(Integer current, Integer size, String keyword, Long modelId) {
        Page<DbcFile> page = new Page<>(current, size);
        LambdaQueryWrapper<DbcFile> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(DbcFile::getFileName, keyword);
        }
        if (modelId != null) {
            wrapper.eq(DbcFile::getModelId, modelId);
        }
        wrapper.eq(DbcFile::getDeleted, 0);
        wrapper.orderByDesc(DbcFile::getCreateTime);

        IPage<DbcFile> result = page(page, wrapper);
        return (Page<DbcFile>) result;
    }

    @Override
    public DbcFile uploadAndParse(MultipartFile file, Long modelId, String modelName, String version, String description) {
        if (modelId == null) {
            throw new BusinessException("请选择车型");
        }
        try {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String originalFilename = file.getOriginalFilename();
            String objectKey = "dbc/" + dateStr + "/" + originalFilename;
            String storageAddress = storageService.upload(
                    objectKey, file.getInputStream(), file.getSize(), "application/octet-stream");

            String parseResult = parseDbcContent(openDbcInputStream(objectKey, null, storageAddress));

            DbcFile dbcFile = new DbcFile();
            dbcFile.setModelId(modelId);
            dbcFile.setModelName(modelName);
            dbcFile.setFileName(originalFilename);
            dbcFile.setStorageKey(objectKey);
            dbcFile.setStorageAddress(storageAddress);
            dbcFile.setStorageType(storageService.getStorageType().name());
            dbcFile.setFilePath(objectKey);
            dbcFile.setFileSize(file.getSize());
            dbcFile.setVersion(version);
            dbcFile.setDescription(description);
            dbcFile.setParseResult(parseResult);
            dbcFile.setMessageCount(countMessages(parseResult));
            dbcFile.setSignalCount(countSignals(parseResult));
            dbcFile.setStatus(1);
            dbcFile.setDeleted(0);
            dbcFile.setCreateTime(LocalDateTime.now());
            dbcFile.setUpdateTime(LocalDateTime.now());
            
            save(dbcFile);
            
            return dbcFile;
        } catch (IOException e) {
            throw new BusinessException("上传DBC文件失败: " + e.getMessage());
        }
    }

    @Override
    public String parseDbcFile(String filePath) {
        return parseDbcContent(openDbcInputStream(null, filePath, null));
    }

    private String parseDbcContent(InputStream inputStream) {
        StringBuilder parseResult = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("BO_ ")) {
                    parseResult.append("MESSAGE: ").append(line).append("\n");
                } else if (line.startsWith("SG_ ")) {
                    parseResult.append("  SIGNAL: ").append(line).append("\n");
                } else if (line.startsWith("CM_ SG_")) {
                    parseResult.append("  COMMENT: ").append(line).append("\n");
                } else if (line.startsWith("VAL_ ")) {
                    parseResult.append("  VALUE: ").append(line).append("\n");
                } else if (line.startsWith("BA_ ")) {
                    appendCycleAttributes(parseResult, line);
                }
            }
        } catch (IOException e) {
            throw new BusinessException("解析DBC文件失败: " + e.getMessage());
        }
        return parseResult.toString();
    }

    private InputStream openDbcInputStream(String storageKey, String filePath, String storageAddress) {
        String objectKey = StorageKeyUtils.resolveObjectKey(storageKey, filePath, storageAddress, storageService);
        if (objectKey != null) {
            return storageService.openInputStream(objectKey);
        }
        File legacyFile = StorageKeyUtils.resolveLegacyLocalFile(filePath);
        if (legacyFile != null) {
            try {
                return new FileInputStream(legacyFile);
            } catch (IOException e) {
                throw new BusinessException("读取DBC文件失败: " + e.getMessage());
            }
        }
        throw new BusinessException("DBC文件不存在或存储地址无效");
    }

    private InputStream openDbcInputStream(DbcFile dbcFile) {
        return openDbcInputStream(dbcFile.getStorageKey(), dbcFile.getFilePath(), dbcFile.getStorageAddress());
    }

    private void appendCycleAttributes(StringBuilder parseResult, String line) {
        Matcher msgCycleMatcher = RAW_MSG_CYCLE.matcher(line);
        if (msgCycleMatcher.find()) {
            parseResult.append("  MSG_CYCLE: ")
                    .append(msgCycleMatcher.group(1)).append(" ")
                    .append(msgCycleMatcher.group(2)).append("\n");
            return;
        }
        Matcher sigSampleMatcher = RAW_SIG_SAMPLE.matcher(line);
        if (sigSampleMatcher.find()) {
            parseResult.append("  SIG_SAMPLE: ")
                    .append(sigSampleMatcher.group(1)).append(" ")
                    .append(sigSampleMatcher.group(2)).append(" ")
                    .append(sigSampleMatcher.group(3)).append("\n");
        }
    }

    private Map<String, String> buildMessageCycleMap(String parseResult) {
        Map<String, String> cycles = new HashMap<>();
        for (String line : parseResult.split("\n")) {
            Matcher matcher = MSG_CYCLE_LINE.matcher(line.trim());
            if (matcher.find()) {
                cycles.put(matcher.group(1), matcher.group(2));
            }
        }
        return cycles;
    }

    private Map<String, String> buildSignalSampleMap(String parseResult) {
        Map<String, String> samples = new HashMap<>();
        for (String line : parseResult.split("\n")) {
            Matcher matcher = SIG_SAMPLE_LINE.matcher(line.trim());
            if (matcher.find()) {
                samples.put(matcher.group(1) + ":" + matcher.group(2), matcher.group(3));
            }
        }
        return samples;
    }

    private String resolveSamplePeriod(String messageId, String signalName,
                                       Map<String, String> messageCycles,
                                       Map<String, String> signalSamples) {
        String signalKey = messageId + ":" + signalName;
        String sampleMs = signalSamples.get(signalKey);
        if (sampleMs == null || sampleMs.isEmpty()) {
            sampleMs = messageCycles.get(messageId);
        }
        return sampleMs == null ? "" : sampleMs;
    }

    private String formatSamplePeriod(String sampleMs) {
        if (sampleMs == null || sampleMs.isEmpty()) {
            return "";
        }
        return sampleMs + " ms";
    }

    @Override
    public List<String> getMessageNames(String parseResult) {
        List<String> messages = new ArrayList<>();
        Pattern pattern = Pattern.compile("MESSAGE:\\s*BO_\\s*\\d+\\s+(\\w+):");
        Matcher matcher = pattern.matcher(parseResult);
        
        while (matcher.find()) {
            messages.add(matcher.group(1));
        }
        
        return messages;
    }

    @Override
    public List<Map<String, String>> getSignalDefinitions(String parseResult) {
        List<Map<String, String>> signals = new ArrayList<>();
        if (parseResult == null || parseResult.isEmpty()) {
            return signals;
        }

        String currentMessage = "";
        Pattern messagePattern = Pattern.compile("MESSAGE:\\s*BO_\\s*\\d+\\s+(\\w+):");
        Pattern signalPattern = Pattern.compile("SIGNAL:\\s+SG_\\s+(\\w+)");

        for (String line : parseResult.split("\n")) {
            Matcher messageMatcher = messagePattern.matcher(line);
            if (messageMatcher.find()) {
                currentMessage = messageMatcher.group(1);
                continue;
            }

            Matcher signalMatcher = signalPattern.matcher(line);
            if (signalMatcher.find()) {
                Map<String, String> signal = new HashMap<>();
                signal.put("name", signalMatcher.group(1));
                signal.put("messageName", currentMessage);
                signals.add(signal);
            }
        }

        return signals;
    }

    private static final Pattern MESSAGE_LINE = Pattern.compile(
            "MESSAGE:\\s*BO_\\s*(\\d+)\\s+(\\w+):\\s*(\\d+)\\s+(\\w+)");
    private static final Pattern SIGNAL_LINE = Pattern.compile(
            "SIGNAL:\\s+SG_\\s+(\\w+)\\s*:\\s*(\\d+)\\|(\\d+)@(\\d)([+-])\\s*\\(([^,]+),([^)]+)\\)\\s*\\[([^|]*)\\|([^\\]]*)\\]\\s*\"([^\"]*)\"\\s*(\\w+)");
    private static final Pattern COMMENT_LINE = Pattern.compile(
            "COMMENT:\\s+CM_\\s+SG_\\s+(\\d+)\\s+(\\w+)\\s+\"(.*)\"");
    private static final Pattern VALUE_LINE = Pattern.compile(
            "VALUE:\\s+VAL_\\s+(\\d+)\\s+(\\w+)\\s+(.+)");
    private static final Pattern MSG_CYCLE_LINE = Pattern.compile(
            "MSG_CYCLE:\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern SIG_SAMPLE_LINE = Pattern.compile(
            "SIG_SAMPLE:\\s+(\\d+)\\s+(\\w+)\\s+(\\d+)");
    private static final Pattern RAW_MSG_CYCLE = Pattern.compile(
            "BA_\\s+\"GenMsgCycleTime\"\\s+BO_\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern RAW_SIG_SAMPLE = Pattern.compile(
            "BA_\\s+\"SamplePeriod\"\\s+SG_\\s+(\\d+)\\s+(\\w+)\\s+(\\d+)");

    @Override
    public List<Map<String, String>> getSignalDetails(String parseResult) {
        List<Map<String, String>> signals = new ArrayList<>();
        if (parseResult == null || parseResult.isEmpty()) {
            return signals;
        }

        Map<String, String> comments = new HashMap<>();
        Map<String, StringBuilder> valueTables = new HashMap<>();
        Map<String, String> messageCycles = buildMessageCycleMap(parseResult);
        Map<String, String> signalSamples = buildSignalSampleMap(parseResult);

        for (String line : parseResult.split("\n")) {
            Matcher commentMatcher = COMMENT_LINE.matcher(line.trim());
            if (commentMatcher.find()) {
                comments.put(commentMatcher.group(1) + ":" + commentMatcher.group(2), commentMatcher.group(3));
                continue;
            }
            Matcher valueMatcher = VALUE_LINE.matcher(line.trim());
            if (valueMatcher.find()) {
                String key = valueMatcher.group(1) + ":" + valueMatcher.group(2);
                valueTables.computeIfAbsent(key, k -> new StringBuilder()).append(valueMatcher.group(3)).append(" ");
            }
        }

        String currentMessageId = "";
        String currentMessageName = "";
        for (String line : parseResult.split("\n")) {
            Matcher messageMatcher = MESSAGE_LINE.matcher(line.trim());
            if (messageMatcher.find()) {
                currentMessageId = messageMatcher.group(1);
                currentMessageName = messageMatcher.group(2);
                continue;
            }

            Matcher signalMatcher = SIGNAL_LINE.matcher(line.trim());
            if (!signalMatcher.find()) {
                continue;
            }

            String signalName = signalMatcher.group(1);
            String key = currentMessageId + ":" + signalName;
            Map<String, String> signal = new HashMap<>();
            signal.put("name", signalName);
            signal.put("messageName", currentMessageName);
            signal.put("messageId", currentMessageId);
            signal.put("startBit", signalMatcher.group(2));
            signal.put("length", signalMatcher.group(3));
            signal.put("byteOrder", "1".equals(signalMatcher.group(4)) ? "Intel" : "Motorola");
            signal.put("signed", "+".equals(signalMatcher.group(5)) ? "无符号" : "有符号");
            signal.put("factor", signalMatcher.group(6));
            signal.put("offset", signalMatcher.group(7));
            signal.put("min", signalMatcher.group(8));
            signal.put("max", signalMatcher.group(9));
            signal.put("unit", signalMatcher.group(10));
            signal.put("receiver", signalMatcher.group(11));
            signal.put("comment", comments.getOrDefault(key, ""));
            signal.put("valueDesc", formatValueDesc(valueTables.get(key)));
            String sampleMs = resolveSamplePeriod(currentMessageId, signalName, messageCycles, signalSamples);
            signal.put("samplePeriodMs", sampleMs);
            signal.put("samplePeriod", formatSamplePeriod(sampleMs));
            signals.add(signal);
        }

        return signals;
    }

    private String formatValueDesc(StringBuilder raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String content = raw.toString().trim();
        if (content.endsWith(";")) {
            content = content.substring(0, content.length() - 1).trim();
        }
        Pattern pair = Pattern.compile("(\\d+)\\s+\"([^\"]*)\"");
        Matcher matcher = pair.matcher(content);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            if (result.length() > 0) {
                result.append("; ");
            }
            result.append(matcher.group(1)).append("=").append(matcher.group(2));
        }
        return result.length() > 0 ? result.toString() : content;
    }

    @Override
    public List<Map<String, String>> getSignalDetailsByFileId(Long id) {
        DbcFile dbcFile = getById(id);
        if (dbcFile == null || dbcFile.getDeleted() == 1) {
            throw new BusinessException("DBC文件不存在");
        }
        String parseResult = enrichParseResultWithCycles(dbcFile.getParseResult(), dbcFile);
        return getSignalDetails(parseResult);
    }

    private String enrichParseResultWithCycles(String parseResult, DbcFile dbcFile) {
        StringBuilder enriched = new StringBuilder(parseResult == null ? "" : parseResult);
        try (InputStream inputStream = openDbcInputStream(dbcFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("BA_ ")) {
                    appendCycleAttributes(enriched, line);
                }
            }
        } catch (IOException e) {
            throw new BusinessException("读取DBC采样周期失败: " + e.getMessage());
        }
        return enriched.toString();
    }

    @Override
    public void updateMetadata(Long id, String version, String description) {
        DbcFile dbcFile = getById(id);
        if (dbcFile == null || dbcFile.getDeleted() == 1) {
            throw new BusinessException("DBC文件不存在");
        }
        if (version != null) {
            dbcFile.setVersion(version);
        }
        if (description != null) {
            dbcFile.setDescription(description);
        }
        dbcFile.setUpdateTime(LocalDateTime.now());
        updateById(dbcFile);
    }

    @Override
    public void publish(Long id) {
        DbcFile dbcFile = getById(id);
        if (dbcFile == null || dbcFile.getDeleted() == 1) {
            throw new BusinessException("DBC文件不存在");
        }
        dbcFile.setStatus(2);
        dbcFile.setUpdateTime(LocalDateTime.now());
        updateById(dbcFile);
    }

    @Override
    public void revoke(Long id) {
        DbcFile dbcFile = getById(id);
        if (dbcFile == null || dbcFile.getDeleted() == 1) {
            throw new BusinessException("DBC文件不存在");
        }
        dbcFile.setStatus(0);
        dbcFile.setUpdateTime(LocalDateTime.now());
        updateById(dbcFile);
    }

    private int countMessages(String parseResult) {
        int count = 0;
        Pattern pattern = Pattern.compile("BO_ \\d+");
        java.util.regex.Matcher matcher = pattern.matcher(parseResult);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int countSignals(String parseResult) {
        int count = 0;
        Pattern pattern = Pattern.compile("SG_ \\w+");
        java.util.regex.Matcher matcher = pattern.matcher(parseResult);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    @Override
    public void dispatchToVehicle(Long dbcFileId, Long vehicleId) {
        DbcFile dbcFile = getById(dbcFileId);
        if (dbcFile == null) {
            throw new BusinessException("DBC文件不存在");
        }
        
        DispatchLog dispatchLog = new DispatchLog();
        dispatchLog.setDbcFileId(dbcFileId);
        dispatchLog.setVehicleId(vehicleId);
        dispatchLog.setDispatchType("SINGLE");
        dispatchLog.setStatus(1);
        dispatchLog.setDispatchTime(LocalDateTime.now());
        dispatchLog.setCreateTime(LocalDateTime.now());
        
        try {
            String result = sendToVehicle(
                    StorageKeyUtils.resolveObjectKey(dbcFile.getStorageKey(), dbcFile.getFilePath(),
                            dbcFile.getStorageAddress(), storageService),
                    vehicleId);
            dispatchLog.setStatus(2);
            dispatchLog.setResult(result);
        } catch (Exception e) {
            dispatchLog.setStatus(3);
            dispatchLog.setResult("失败: " + e.getMessage());
        }
        
        dispatchLogMapper.insert(dispatchLog);
    }

    @Override
    public void dispatchToVehicles(Long dbcFileId, List<Long> vehicleIds) {
        for (Long vehicleId : vehicleIds) {
            dispatchToVehicle(dbcFileId, vehicleId);
        }
    }

    private String sendToVehicle(String storageKey, Long vehicleId) {
        return "SUCCESS: DBC文件已下发到车辆 " + vehicleId + ", key=" + storageKey;
    }
}
