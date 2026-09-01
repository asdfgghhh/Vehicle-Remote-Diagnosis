package com.vrd.signal.controller;

import com.vrd.common.result.Result;
import com.vrd.signal.dto.SignalPageResult;
import com.vrd.signal.entity.VehicleSignal;
import com.vrd.signal.service.SignalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 车辆信号数据接口（服务端口 9085，网关路由前缀 /api/signal）
 * <p>
 * 提供车辆历史信号的时间线查询、分页查询、按信号名查询等能力，
 * 数据存储于 ClickHouse（大数据量时序查询）。
 */
@RestController
@RequestMapping("/signal")
public class SignalController {

    @Autowired
    private SignalService signalService;

    /**
     * 按时间范围查询车辆信号时间线
     * <p>GET /signal/timeline/{vehicleId}
     * <p>返回结果按信号名分组，用于前端信号回放/曲线图渲染。
     *
     * @param vehicleId 车辆 ID
     * @param vin       可选，车架号（辅助过滤）
     * @param startTime 时间范围起点（格式 yyyy-MM-dd HH:mm:ss，必填）
     * @param endTime   时间范围终点（格式 yyyy-MM-dd HH:mm:ss，必填）
     * @return Map：total（总条数）、timeline（信号名 → 信号列表）、signals（扁平列表）
     */
    @GetMapping("/timeline/{vehicleId}")
    public Result<Map<String, Object>> queryTimeline(@PathVariable Long vehicleId,
                                                     @RequestParam(required = false) String vin,
                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<VehicleSignal> signals = this.signalService.queryByTimeRange(vin, vehicleId, startTime, endTime);
        Map<String, List<VehicleSignal>> timeline = new HashMap<>();
        for (VehicleSignal signal : signals) {
            timeline.computeIfAbsent(signal.getSignalName(), k -> new ArrayList<>()).add(signal);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", signals.size());
        result.put("timeline", timeline);
        result.put("signals", signals);
        return Result.success(result);
    }

    /**
     * 按时间范围分页查询车辆信号
     * <p>GET /signal/page/{vehicleId}
     *
     * @param vehicleId 车辆 ID
     * @param vin       可选，车架号（辅助过滤）
     * @param startTime 时间范围起点（格式 yyyy-MM-dd HH:mm:ss，必填）
     * @param endTime   时间范围终点（格式 yyyy-MM-dd HH:mm:ss，必填）
     * @param current   当前页码，默认 1
     * @param size      每页条数，默认 50
     * @return 分页结果 SignalPageResult
     */
    @GetMapping("/page/{vehicleId}")
    public Result<SignalPageResult> queryPage(@PathVariable Long vehicleId,
                                              @RequestParam(required = false) String vin,
                                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                                              @RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "50") Integer size) {
        SignalPageResult page = this.signalService.queryByTimeRangePaged(vin, vehicleId, startTime, endTime, current, size);
        return Result.success(page);
    }

    /**
     * 按信号名查询车辆信号历史
     * <p>GET /signal/signal-name/{vehicleId}?signalName={name}
     *
     * @param vehicleId  车辆 ID
     * @param vin        可选，车架号（辅助过滤）
     * @param signalName 信号名称（必填）
     * @param startTime  时间范围起点（格式 yyyy-MM-dd HH:mm:ss，必填）
     * @param endTime    时间范围终点（格式 yyyy-MM-dd HH:mm:ss，必填）
     * @return List&lt;VehicleSignal&gt; 该信号的时间序列数据
     */
    @GetMapping("/signal-name/{vehicleId}")
    public Result<List<VehicleSignal>> queryBySignalName(@PathVariable Long vehicleId,
                                                         @RequestParam(required = false) String vin,
                                                         @RequestParam String signalName,
                                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<VehicleSignal> signals = this.signalService.queryBySignalName(vin, vehicleId, signalName, startTime, endTime);
        return Result.success(signals);
    }

    /**
     * 按 ID 查询单条信号记录
     * <p>GET /signal/{id}
     *
     * @param id 信号记录 ID
     * @return VehicleSignal 信号记录；不存在时 data 为 null
     */
    @GetMapping("/{id}")
    public Result<VehicleSignal> getById(@PathVariable Long id) {
        VehicleSignal signal = this.signalService.getById(id);
        return Result.success(signal);
    }
}
