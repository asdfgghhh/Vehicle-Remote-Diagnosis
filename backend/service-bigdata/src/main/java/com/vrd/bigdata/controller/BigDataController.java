package com.vrd.bigdata.controller;

import com.vrd.bigdata.kafka.KafkaDataProducer;
import com.vrd.bigdata.service.BigDataQueryService;
import com.vrd.bigdata.service.DorisQueryService;
import com.vrd.bigdata.service.HdfsService;
import com.vrd.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bigdata")
public class BigDataController {

    @Autowired
    private HdfsService hdfsService;

    @Autowired
    private BigDataQueryService bigDataQueryService;

    @Autowired
    private DorisQueryService dorisQueryService;

    @Autowired
    private KafkaDataProducer kafkaDataProducer;

    @GetMapping("/signals")
    public Result<Map<String, Object>> querySignals(
            @RequestParam Long vehicleId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) String signalName) {
        Map<String, Object> result = dorisQueryService.querySignals(vehicleId, signalName, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/logs")
    public Result<Map<String, Object>> queryLogs(
            @RequestParam Long vehicleId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) String ecuType) {
        Map<String, Object> result = dorisQueryService.queryLogs(vehicleId, ecuType, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/diagnostics")
    public Result<Map<String, Object>> queryDiagnostics(
            @RequestParam Long vehicleId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        Map<String, Object> result = dorisQueryService.queryDiagnostics(vehicleId, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/aggregate")
    public Result<Map<String, Object>> aggregateSignals(
            @RequestParam Long vehicleId,
            @RequestParam String signalName,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        Map<String, Object> result = dorisQueryService.aggregateSignals(vehicleId, signalName, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/trend")
    public Result<Map<String, Object>> getSignalTrend(
            @RequestParam Long vehicleId,
            @RequestParam String signalName,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        Map<String, Object> result = dorisQueryService.getSignalTrend(vehicleId, signalName, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/health-report")
    public Result<Map<String, Object>> getVehicleHealthReport(
            @RequestParam Long vehicleId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        Map<String, Object> result = dorisQueryService.getVehicleHealthReport(vehicleId, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/top-errors")
    public Result<List<Map<String, Object>>> getTopErrorCodes(
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> result = dorisQueryService.getTopErrorCodes(startTime, endTime, limit);
        return Result.success(result);
    }

    @GetMapping("/dates")
    public Result<List<Map<String, Object>>> getAvailableDates(@RequestParam String dataType) {
        List<Map<String, Object>> dates = dorisQueryService.getAvailableDates(dataType);
        return Result.success(dates);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam String dataType,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        Map<String, Object> stats = dorisQueryService.getStatistics(dataType, startTime, endTime);
        return Result.success(stats);
    }

    @PostMapping("/save")
    public Result<Void> saveToHdfs(@RequestParam String data, @RequestParam String path) {
        hdfsService.saveToHdfs(data, path);
        return Result.success();
    }

    @GetMapping("/read")
    public Result<String> readFromHdfs(@RequestParam String path) {
        String content = hdfsService.readFromHdfs(path);
        return Result.success(content);
    }

    @GetMapping("/files")
    public Result<List<String>> listFiles(@RequestParam String directory) {
        List<String> files = hdfsService.listFiles(directory);
        return Result.success(files);
    }

    @PostMapping("/send/signals")
    public Result<Void> sendSignals(@RequestBody String message) {
        kafkaDataProducer.sendSignals(message);
        return Result.success();
    }

    @PostMapping("/send/logs")
    public Result<Void> sendLogs(@RequestBody String message) {
        kafkaDataProducer.sendLogs(message);
        return Result.success();
    }
}
