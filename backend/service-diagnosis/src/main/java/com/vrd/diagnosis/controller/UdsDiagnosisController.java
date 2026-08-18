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

@RestController
@RequestMapping(value={"/diagnosis"})
public class UdsDiagnosisController {
    private static final Logger log = LoggerFactory.getLogger(UdsDiagnosisController.class);
    private final UdsDiagnosisService udsDiagnosisService;

    public UdsDiagnosisController(UdsDiagnosisService udsDiagnosisService) {
        this.udsDiagnosisService = udsDiagnosisService;
    }

    @PostMapping(value={"/uds"})
    public Result<UdsResponse> executeUds(@RequestBody UdsRequest request) {
        log.info("UDS request: vin={}, serviceId=0x{}, ecu={}", new Object[]{request.getVin(), Integer.toHexString(request.getServiceId()), request.getEcuType()});
        UdsResponse response = this.udsDiagnosisService.executeRequest(request);
        return Result.success(response);
    }

    @PostMapping(value={"/session/control"})
    public Result<UdsResponse> sessionControl(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.diagnosticSessionControl(request));
    }

    @PostMapping(value={"/ecu/reset"})
    public Result<UdsResponse> ecuReset(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.ecuReset(request));
    }

    @PostMapping(value={"/security/request-seed"})
    public Result<UdsResponse> requestSeed(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.securityAccessRequestSeed(request));
    }

    @PostMapping(value={"/security/send-key"})
    public Result<UdsResponse> sendKey(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.securityAccessSendKey(request));
    }

    @PostMapping(value={"/data/read"})
    public Result<UdsResponse> readData(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.readDataByIdentifier(request));
    }

    @PostMapping(value={"/data/write"})
    public Result<UdsResponse> writeData(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.writeDataByIdentifier(request));
    }

    @PostMapping(value={"/dtc/read"})
    public Result<UdsResponse> readDtc(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.readDtcInformation(request));
    }

    @PostMapping(value={"/dtc/clear"})
    public Result<UdsResponse> clearDtc(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.clearDiagnosticInformation(request));
    }

    @PostMapping(value={"/routine/control"})
    public Result<UdsResponse> routineControl(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.routineControl(request));
    }

    @PostMapping(value={"/memory/read"})
    public Result<UdsResponse> readMemory(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.readMemoryByAddress(request));
    }

    @PostMapping(value={"/memory/write"})
    public Result<UdsResponse> writeMemory(@RequestBody UdsRequest request) {
        return Result.success(this.udsDiagnosisService.writeMemoryByAddress(request));
    }

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

