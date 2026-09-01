package com.vrd.vehicle.service;

import com.vrd.vehicle.dto.PageResult;
import com.vrd.vehicle.dto.VehicleOnlineLogRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 车辆在线状态变更日志服务（写入 + 查询）
 * <p>
 * 底层写入由 BigDataClient 负责，根据 {@code bigdata.type} 配置自动切换
 * ClickHouse / TDengine / Doris，调用方无需关心实际存储。
 */
public interface VehicleOnlineLogService {

    // ======================== 写入 ========================

    /**
     * 写入单条日志
     *
     * @param record 日志记录（id / eventTime 为空时会自动补全）
     */
    void insert(VehicleOnlineLogRecord record);

    /**
     * 批量写入日志（高写入量场景使用）
     */
    void batchInsert(List<VehicleOnlineLogRecord> records);

    // ======================== 查询 ========================

    /**
     * 分页查询单辆车的状态变更历史
     *
     * @param vin       车架号（必填）
     * @param current   当前页
     * @param size      每页条数
     * @param startTime 查询开始时间（可选，默认近30天）
     * @param endTime   查询结束时间（可选，默认当前）
     * @return 分页结果
     */
    PageResult<VehicleOnlineLogRecord> listByVin(String vin, Integer current, Integer size,
                                                 LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定时间范围内每日在线/离线事件数
     * <pre>
     * 返回列表每一行 Map: {
     *   "statDate": "2026-08-31",
     *   "statusTo": 1,
     *   "cnt": 123
     * }
     * </pre>
     */
    List<Map<String, Object>> statDailyEvents(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计某辆车近 N 天的上线次数与离线次数
     * <pre>
     * { "vin": "...", "onlineEvents": 24, "offlineEvents": 22, "days": 7 }
     * </pre>
     */
    Map<String, Object> statVehicleRecentSummary(String vin, int days);
}
