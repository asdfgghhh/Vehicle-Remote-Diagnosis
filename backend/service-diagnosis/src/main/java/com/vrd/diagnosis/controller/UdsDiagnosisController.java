package com.vrd.diagnosis.controller;

import com.vrd.common.result.Result;
import com.vrd.diagnosis.dto.UdsRequest;
import com.vrd.diagnosis.dto.UdsResponse;
import com.vrd.diagnosis.service.UdsDiagnosisService;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * UDS 远程诊断接口（服务端口 9087，网关路由前缀 /api/diagnosis）
 * <p>
 * 实现 ISO 14229（Unified Diagnostic Services）统一的诊断服务对外 HTTP API。
 * 指令链路：本服务组包 UDS 请求（traceId + requestHex）→ Kafka uds-commands
 * → service-access 转发 MQTT vrd/{vin}/uds/request → 车端 T-Box
 * → 车端响应 MQTT vrd/{vin}/uds/response → Kafka uds-responses → 本服务按 traceId 关联返回。
 * <p>
 * 通用请求体 UdsRequest：vin（车架号）、vehicleId、ecuType（ECU 类型）、
 * serviceId（UDS 服务 ID，如 0x22）、subFunction、did/dataHex 等参数。
 * 通用响应体 UdsResponse：success、responseData/parsedData（正响应解析）、
 * negativeResponse/Code/Desc（否定响应）、sessionStatus、securityStatus、responseTimeMs 等。
 * <p>
 * 配置项 vrd.uds.mock-enabled=true 时走 Mock 数据（不实际下发车端），
 * 响应超时由 vrd.uds.response-timeout-ms（默认 8000ms）控制。
 */
@RestController
@RequestMapping(value={"/diagnosis"})
public class UdsDiagnosisController {
    private static final Logger log = LoggerFactory.getLogger(UdsDiagnosisController.class);
    private final UdsDiagnosisService udsDiagnosisService;

    public UdsDiagnosisController(UdsDiagnosisService udsDiagnosisService) {
        this.udsDiagnosisService = udsDiagnosisService;
    }

    /**
     * 通用 UDS 指令分发入口（0x10/0x11/0x27/0x22/0x2E/0x19 等）
     * <p>POST /diagnosis/uds
     * <p>按 request.serviceId 路由到具体诊断服务实现，适合自由组装原始 UDS 请求的场景。
     *
     * @param request UDS 请求体（vin、ecuType、serviceId、subFunction、参数等）
     * @return UdsResponse 诊断结果（含解析数据或否定响应码）
     */
    @PostMapping(value={"/uds"})
    public Result<UdsResponse> executeUds(@RequestBody UdsRequest request) {
        log.info("UDS request: vin={}, serviceId=0x{}, ecu={}", new Object[]{request.getVin(), Integer.toHexString(request.getServiceId()), request.getEcuType()});
        UdsResponse response = this.udsDiagnosisService.executeRequest(request);
        return Result.success(response);
    }

    /**
     * 诊断会话控制（UDS 0x10 DiagnosticSessionControl）
     * <p>POST /diagnosis/session/control
     * <p>切换 ECU 诊断会话模式（0x01 默认 / 0x02 编程 / 0x03 扩展等）。
     *
     * @param request UDS 请求体（subFunction 为目标会话类型）
     * @return UdsResponse 含 sessionStatus（切换后的会话状态）
     */
    @PostMapping(value={"/session/control"})
    public Result<UdsResponse> sessionControl(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.diagnosticSessionControl(request));
    }

    /**
     * ECU 复位（UDS 0x11 ECUReset）
     * <p>POST /diagnosis/ecu/reset
     * <p>触发 ECU 硬复位（0x01）/ 软复位（0x03）/ 钥匙复位（0x05）等。
     *
     * @param request UDS 请求体（subFunction 为复位类型）
     * @return UdsResponse 复位执行结果
     */
    @PostMapping(value={"/ecu/reset"})
    public Result<UdsResponse> ecuReset(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.ecuReset(request));
    }

    /**
     * 安全访问-请求种子（UDS 0x27 SecurityAccess，子功能奇数）
     * <p>POST /diagnosis/security/request-seed
     * <p>向 ECU 请求安全访问种子（seed），服务端缓存 seed 供后续 sendKey 校验。
     *
     * @param request UDS 请求体（subFunction 为安全级别，如 0x01）
     * @return UdsResponse responseData 中包含种子值
     */
    @PostMapping(value={"/security/request-seed"})
    public Result<UdsResponse> requestSeed(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.securityAccessRequestSeed(request));
    }

    /**
     * 安全访问-发送密钥（UDS 0x27 SecurityAccess，子功能偶数）
     * <p>POST /diagnosis/security/send-key
     * <p>提交根据种子计算的密钥，由 ECU 校验以解锁受保护服务。
     *
     * @param request UDS 请求体（dataHex/key 为密钥值）
     * @return UdsResponse 含 securityStatus（解锁成功/失败）
     */
    @PostMapping(value={"/security/send-key"})
    public Result<UdsResponse> sendKey(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.securityAccessSendKey(request));
    }

    /**
     * 按 ID 读取数据（UDS 0x22 ReadDataByIdentifier）
     * <p>POST /diagnosis/data/read
     *
     * @param request UDS 请求体（did 为数据标识符，如 VIN/软件版本等 DID）
     * @return UdsResponse parsedData 中为 DID 数据解析结果
     */
    @PostMapping(value={"/data/read"})
    public Result<UdsResponse> readData(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.readDataByIdentifier(request));
    }

    /**
     * 按 ID 写入数据（UDS 0x2E WriteDataByIdentifier）
     * <p>POST /diagnosis/data/write
     *
     * @param request UDS 请求体（did 为目标标识符，dataHex 为写入数据）
     * @return UdsResponse 写入执行结果
     */
    @PostMapping(value={"/data/write"})
    public Result<UdsResponse> writeData(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.writeDataByIdentifier(request));
    }

    /**
     * 读取 DTC 故障码信息（UDS 0x19 ReadDTCInformation）
     * <p>POST /diagnosis/dtc/read
     *
     * @param request UDS 请求体（subFunction 如 0x02 按状态掩码读 DTC）
     * @return UdsResponse parsedData 中为 DTC 列表及状态位解析
     */
    @PostMapping(value={"/dtc/read"})
    public Result<UdsResponse> readDtc(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.readDtcInformation(request));
    }

    /**
     * 清除 DTC 诊断信息（UDS 0x14 ClearDiagnosticInformation）
     * <p>POST /diagnosis/dtc/clear
     *
     * @param request UDS 请求体（groupOfDTC 为 DTC 组掩码，0xFFFFFF 表示全部）
     * @return UdsResponse 清除执行结果
     */
    @PostMapping(value={"/dtc/clear"})
    public Result<UdsResponse> clearDtc(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.clearDiagnosticInformation(request));
    }

    /**
     * 例程控制（UDS 0x31 RoutineControl）
     * <p>POST /diagnosis/routine/control
     * <p>启动（0x01）/ 停止（0x02）/ 请求结果（0x03）指定例程（如自检、擦写）。
     *
     * @param request UDS 请求体（subFunction 为操作类型，routineId 为例程 ID）
     * @return UdsResponse 例程执行状态/结果
     */
    @PostMapping(value={"/routine/control"})
    public Result<UdsResponse> routineControl(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.routineControl(request));
    }

    /**
     * 按地址读取内存（UDS 0x23 ReadMemoryByAddress）
     * <p>POST /diagnosis/memory/read
     *
     * @param request UDS 请求体（address 内存起始地址，length 读取长度）
     * @return UdsResponse responseData 中为内存原始数据
     */
    @PostMapping(value={"/memory/read"})
    public Result<UdsResponse> readMemory(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.readMemoryByAddress(request));
    }

    /**
     * 按地址写入内存（UDS 0x3D WriteMemoryByAddress）
     * <p>POST /diagnosis/memory/write
     *
     * @param request UDS 请求体（address 目标内存地址，dataHex 写入数据）
     * @return UdsResponse 写入执行结果
     */
    @PostMapping(value={"/memory/write"})
    public Result<UdsResponse> writeMemory(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.writeMemoryByAddress(request));
    }

    /**
     * 输入输出控制（UDS 0x2F InputOutputControlByIdentifier）
     * <p>POST /diagnosis/io/control
     * <p>对 ECU IO 端口进行接管控制（如强制风扇转速、点亮指示灯）。
     *
     * @param request UDS 请求体（did 为 IO 标识符，control 记录含控制值）
     * @return UdsResponse 控制执行结果
     */
    @PostMapping(value={"/io/control"})
    public Result<UdsResponse> ioControl(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.inputOutputControl(request));
    }

    @PostMapping(value={"/tester-present"})
    public Result<UdsResponse> testerPresent(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.testerPresent(request));
    }

    @GetMapping(value={"/sessions"})
    public Result<Object> querySessions(@RequestParam(required=false) String vin, @RequestParam(defaultValue="1") Integer page, @RequestParam(defaultValue="20") Integer size) {
        return Result.success(this.udsDiagnosisService.querySessions(vin, page, size));
    }

    @GetMapping(value={"/services"})
    public Result<List<Map<String, Object>>> getSupportedServices() {
        List<Map<String, Object>> services = List.of(Map.of("serviceId", "0x10", "name", "\u8bca\u65ad\u4f1a\u8bdd\u63a7\u5236", "description", "\u5207\u6362 ECU \u8bca\u65ad\u4f1a\u8bdd\u6a21\u5f0f"), Map.of("serviceId", "0x11", "name", "ECU \u590d\u4f4d", "description", "ECU \u786c\u590d\u4f4d/\u8f6f\u590d\u4f4d/\u94a5\u5319\u590d\u4f4d"), Map.of("serviceId", "0x27", "name", "\u5b89\u5168\u8bbf\u95ee", "description", "\u5b89\u5168\u8bbf\u95ee\u79cd\u5b50/\u5bc6\u94a5\u9a8c\u8bc1"), Map.of("serviceId", "0x22", "name", "\u6309 ID \u8bfb\u53d6\u6570\u636e", "description", "\u901a\u8fc7 DID \u8bfb\u53d6 ECU \u6570\u636e"), Map.of("serviceId", "0x2E", "name", "\u6309 ID \u5199\u5165\u6570\u636e", "description", "\u901a\u8fc7 DID \u5199\u5165 ECU \u6570\u636e"), Map.of("serviceId", "0x19", "name", "\u8bfb\u53d6 DTC \u4fe1\u606f", "description", "\u8bfb\u53d6\u6545\u969c\u8bca\u65ad\u7801"), Map.of("serviceId", "0x14", "name", "\u6e05\u9664\u8bca\u65ad\u4fe1\u606f", "description", "\u6e05\u9664 DTC \u548c\u76f8\u5173\u8bca\u65ad\u4fe1\u606f"), Map.of("serviceId", "0x31", "name", "\u4f8b\u7a0b\u63a7\u5236", "description", "\u542f\u52a8/\u505c\u6b62/\u8bf7\u6c42\u7ed3\u679c"), Map.of("serviceId", "0x23", "name", "\u6309\u5730\u5740\u8bfb\u53d6\u5185\u5b58", "description", "\u8bfb\u53d6 ECU \u5185\u5b58\u5730\u5740\u6570\u636e"), Map.of("serviceId", "0x3D", "name", "\u6309\u5730\u5740\u5199\u5165\u5185\u5b58", "description", "\u5199\u5165 ECU \u5185\u5b58\u5730\u5740\u6570\u636e"), Map.of("serviceId", "0x2F", "name", "\u8f93\u5165\u8f93\u51fa\u63a7\u5236", "description", "\u63a7\u5236 ECU IO \u7aef\u53e3"), Map.of("serviceId", "0x3E", "name", "\u6d4b\u8bd5\u8bbe\u5907\u5728\u7ebf", "description", "\u4fdd\u6301\u8bca\u65ad\u4f1a\u8bdd\u6d3b\u8dc3"), Map.of("serviceId", "0x34", "name", "\u8bf7\u6c42\u4e0b\u8f7d", "description", "\u8bf7\u6c42\u5411 ECU \u4e0b\u8f7d\u6570\u636e"), Map.of("serviceId", "0x35", "name", "\u8bf7\u6c42\u4e0a\u4f20", "description", "\u8bf7\u6c42\u4ece ECU \u4e0a\u4f20\u6570\u636e"), Map.of("serviceId", "0x36", "name", "\u4f20\u8f93\u6570\u636e", "description", "\u6570\u636e\u4f20\u8f93"), Map.of("serviceId", "0x37", "name", "\u8bf7\u6c42\u4f20\u8f93\u9000\u51fa", "description", "\u9000\u51fa\u6570\u636e\u4f20\u8f93\u6a21\u5f0f"));
        return Result.success(services);
    }
}

