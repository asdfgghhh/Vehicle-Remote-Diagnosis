package com.vrd.signal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.signal.entity.VehicleSignal;
import com.vrd.signal.service.SignalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/signal")
public class SignalController {

    @Autowired
    private SignalService signalService;

    @GetMapping("/timeline/{vehicleId}")
    public Result<Map<String, Object>> queryTimeline(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        List<VehicleSignal> signals = signalService.queryByTimeRange(vehicleId, startTime, endTime);
        
        Map<String, List<VehicleSignal>> timeline = new HashMap<>();
        for (VehicleSignal signal : signals) {
            String signalName = signal.getSignalName();
            timeline.computeIfAbsent(signalName, k -> new java.util.ArrayList<>()).add(signal);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", signals.size());
        result.put("timeline", timeline);
        result.put("signals", signals);
        
        return Result.success(result);
    }

    @GetMapping("/page/{vehicleId}")
    public Result<Page<VehicleSignal>> queryPage(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "50") Integer size) {
        
        Page<VehicleSignal> page = signalService.queryByTimeRangePaged(vehicleId, startTime, endTime, current, size);
        return Result.success(page);
    }

    @GetMapping("/signal-name/{vehicleId}")
    public Result<List<VehicleSignal>> queryBySignalName(
            @PathVariable Long vehicleId,
            @RequestParam String signalName,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        List<VehicleSignal> signals = signalService.queryBySignalName(vehicleId, signalName, startTime, endTime);
        return Result.success(signals);
    }

    @GetMapping("/{id}")
    public Result<VehicleSignal> getById(@PathVariable Long id) {
        VehicleSignal signal = signalService.getById(id);
        return Result.success(signal);
    }

    @PostMapping("/receive")
    public Result<Void> receiveSignal(@RequestParam String vin, @RequestBody String payload) {
        signalService.receiveSignal(vin, payload);
        return Result.success();
    }
}
