package com.vrd.bigdata.controller;

import com.vrd.bigdata.kafka.KafkaDataProducer;
import com.vrd.bigdata.service.BigDataQueryService;
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
    private KafkaDataProducer kafkaDataProducer;

    @GetMapping("/signals")
    public Result<Map<String, Object>> querySignals(
            @RequestParam Long vehicleId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        Map<String, Object> result = bigDataQueryService.querySignals(vehicleId, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/logs")
    public Result<Map<String, Object>> queryLogs(
            @RequestParam Long vehicleId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        Map<String, Object> result = bigDataQueryService.queryLogs(vehicleId, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/aggregate")
    public Result<Map<String, Object>> aggregateSignals(
            @RequestParam Long vehicleId,
            @RequestParam String signalName,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        Map<String, Object> result = bigDataQueryService.aggregateSignals(vehicleId, signalName, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/dates")
    public Result<List<String>> getAvailableDates(@RequestParam String dataType) {
        List<String> dates = bigDataQueryService.getAvailableDates(dataType);
        return Result.success(dates);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam String dataType,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        Map<String, Object> stats = bigDataQueryService.getStatistics(dataType, startTime, endTime);
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
