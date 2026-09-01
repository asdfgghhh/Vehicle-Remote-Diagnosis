package com.vrd.vehicle.controller;

import com.vrd.common.result.Result;
import com.vrd.vehicle.dto.PageResult;
import com.vrd.vehicle.dto.VehicleOnlineLogRecord;
import com.vrd.vehicle.service.VehicleOnlineLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 车辆在线状态变更日志查询接口
 * <p>
 * 服务端口 9082，网关路由前缀 /api/vehicle
 */
@Slf4j
@RestController
@RequestMapping("/vehicle/online-log")
@RequiredArgsConstructor
public class VehicleOnlineLogController {

    private final VehicleOnlineLogService onlineLogService;

    /**
     * 分页查询指定车辆的状态变更历史（最常用：车辆详情页状态轨迹）
     *
     * @param vin       车架号（必填）
     * @param current   当前页码，默认 1
     * @param size      每页条数，默认 20，上限 500
     * @param startTime 开始时间（可选，默认近 30 天）
     * @param endTime   结束时间（可选，默认当前）
     */
    @GetMapping("/{vin}")
    public Result<PageResult<VehicleOnlineLogRecord>> listByVin(
            @PathVariable String vin,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(onlineLogService.listByVin(vin, current, size, startTime, endTime));
    }

    /**
     * 统计每日在线/离线事件数（车辆大盘：趋势图使用）
     */
    @GetMapping("/stat/daily-events")
    public Result<List<Map<String, Object>>> statDailyEvents(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(onlineLogService.statDailyEvents(startTime, endTime));
    }

    /**
     * 查询某辆车近 N 天上/下线概览（车辆详情页：上线次数卡片等）
     *
     * @param vin  车架号
     * @param days 天数，默认 7，上限 365
     */
    @GetMapping("/summary/{vin}")
    public Result<Map<String, Object>> statVehicleRecentSummary(
            @PathVariable String vin,
            @RequestParam(defaultValue = "7") Integer days) {
        return Result.success(onlineLogService.statVehicleRecentSummary(vin, days));
    }
}
