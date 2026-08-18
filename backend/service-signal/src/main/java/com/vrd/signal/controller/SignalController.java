/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.result.Result
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.format.annotation.DateTimeFormat
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.vrd.signal.controller;

import com.vrd.common.result.Result;
import com.vrd.signal.dto.SignalPageResult;
import com.vrd.signal.entity.VehicleSignal;
import com.vrd.signal.service.SignalService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/signal"})
public class SignalController {
    @Autowired
    private SignalService signalService;

    @GetMapping(value={"/timeline/{vehicleId}"})
    public Result<Map<String, Object>> queryTimeline(@PathVariable Long vehicleId, @RequestParam(required=false) String vin, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime startTime, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<VehicleSignal> signals = this.signalService.queryByTimeRange(vin, vehicleId, startTime, endTime);
        HashMap<String, List> timeline = new HashMap<String, List>();
        for (VehicleSignal signal : signals) {
            timeline.computeIfAbsent(signal.getSignalName(), k -> new ArrayList()).add(signal);
        }
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("total", signals.size());
        result.put("timeline", timeline);
        result.put("signals", signals);
        return Result.success(result);
    }

    @GetMapping(value={"/page/{vehicleId}"})
    public Result<SignalPageResult> queryPage(@PathVariable Long vehicleId, @RequestParam(required=false) String vin, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime startTime, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime endTime, @RequestParam(defaultValue="1") Integer current, @RequestParam(defaultValue="50") Integer size) {
        SignalPageResult page = this.signalService.queryByTimeRangePaged(vin, vehicleId, startTime, endTime, current, size);
        return Result.success((Object)page);
    }

    @GetMapping(value={"/signal-name/{vehicleId}"})
    public Result<List<VehicleSignal>> queryBySignalName(@PathVariable Long vehicleId, @RequestParam(required=false) String vin, @RequestParam String signalName, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime startTime, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<VehicleSignal> signals = this.signalService.queryBySignalName(vin, vehicleId, signalName, startTime, endTime);
        return Result.success(signals);
    }

    @GetMapping(value={"/{id}"})
    public Result<VehicleSignal> getById(@PathVariable Long id) {
        VehicleSignal signal = this.signalService.getById(id);
        return Result.success((Object)signal);
    }
}

