package com.vrd.vehicle.online;

import com.vrd.vehicle.dto.VehicleOnlineLogRecord;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.service.VehicleOnlineLogService;
import com.vrd.vehicle.service.VehicleService;
import com.vrd.vehicle.websocket.OnlineStatusWebSocketHandler;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 车辆在线状态管理器（核心业务类）
 * <p>
 * 职责：
 * <ol>
 *   <li>处理一条「状态变更事件」：Redis 更新 → MySQL vehicle.status / last_online_time
 *       → 写 BigData 日志 → 直接 WebSocket 推送前端</li>
 *   <li>业务信号隐含刷新：touchLastSeen(vin) 仅更新 lastSeen，不翻转状态</li>
 *   <li>兜底扫描：获取所有在线 VIN，判定超时并强制离线</li>
 * </ol>
 * 数据流：MQTT → service-access(网关) → Kafka → service-vehicle(本类)
 *         → 更新DB + 直接 WebSocket 推送前端
 * <p>
 * 经验 #1531640：状态变更必须以「时间戳最后写入」为准，禁止乱序导致 ONLINE 被 OFFLINE 覆盖；
 *                通过 eventTime 与 lastSeen 对比执行"乐观更新"。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleOnlineStateManager {

    private final VehicleOnlineProperties properties;
    private final VehicleService vehicleService;
    private final VehicleOnlineLogService onlineLogService;
    private final StringRedisTemplate redisTemplate;
    private final OnlineStatusWebSocketHandler webSocketHandler;
    private final DashboardCacheManager dashboardCacheManager;

    // ========================================================================
    // 对外 API
    // ========================================================================

    /**
     * 处理一条显式的状态变更事件（来自 MQTT / API / Kafka）
     *
     * @param vin        车架号（必填）
     * @param newStatus  目标状态：0-离线 1-在线 2-未知
     * @param reason     原因（见 VehicleOnlineReason 常量）
     * @param source     来源 MQTT/KAFKA/API/SCHEDULER 等
     * @param eventTime  事件时间（null=now）
     * @param extInfo    扩展信息 JSON（可为 null）
     * @return true=状态实际发生变更并落库；false=未变更或车辆不存在
     */
    public boolean onStatusEvent(String vin, int newStatus, String reason, String source,
                                 LocalDateTime eventTime, String extInfo) {
        if (!StringUtils.hasText(vin)) return false;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime evt = eventTime != null ? eventTime : now;

        // 1) 查询车辆是否存在
        Vehicle vehicle = vehicleService.lambdaQuery()
                .eq(Vehicle::getVin, vin)
                .eq(Vehicle::getDeleted, 0)
                .one();
        if (vehicle == null) {
            log.warn("onStatusEvent: vin={} not found in vehicle table, ignore event(status={}, reason={})",
                    vin, newStatus, reason);
            return false;
        }

        // 2) 乐观并发保护：若已有更新的记录，忽略乱序旧事件
        Long cachedLastSeen = getLastSeenMs(vin);
        long evtMs = toEpochMs(evt);
        if (cachedLastSeen != null && cachedLastSeen > evtMs) {
            log.debug("onStatusEvent: vin={} skip out-of-order event (evt={} < cachedLastSeen={})",
                    vin, evtMs, cachedLastSeen);
            return false;
        }

        // 3) 当前状态（DB 优先，Redis 作为后备）
        int currentStatus = getCurrentStatusFromCacheOrDb(vehicle);

        // 4) 更新 Redis：lastSeen + status
        updateRedisState(vin, newStatus, evtMs);

        // 5) 如果状态未变更，仅更新 last_online_time（如果 ONLINE），不记录日志
        if (currentStatus == newStatus && newStatus != VehicleOnlineStatus.UNKNOWN.getCode()) {
            if (newStatus == VehicleOnlineStatus.ONLINE.getCode()) {
                // 在线且未变：刷新 last_online_time（避免 N 分钟无事件导致列表里显示过时时间）
                touchVehicleLastOnlineTimeIfNeeded(vehicle, evt);
            }
            return false;
        }

        // 6) 状态发生变更：落库 vehicle
        try {
            vehicle.setStatus(newStatus);
            vehicle.setLastOnlineTime(evt);
            vehicle.setUpdateTime(now);
            vehicleService.updateById(vehicle);
        } catch (Exception e) {
            log.error("Failed to update vehicle.status for vin={}", vin, e);
            throw e;
        }

        // 7) 更新首页仪表盘 Redis 计数器（在线数增减）
        try {
            if (newStatus == VehicleOnlineStatus.ONLINE.getCode()) {
                dashboardCacheManager.incrementOnline();
            } else if (newStatus == VehicleOnlineStatus.OFFLINE.getCode()) {
                dashboardCacheManager.decrementOnline();
            }
        } catch (Exception e) {
            log.warn("Dashboard counter update failed for vin={}", vin, e);
        }

        // 8) 写 BigData 日志
        try {
            VehicleOnlineLogRecord logRecord = new VehicleOnlineLogRecord();
            logRecord.setVin(vin);
            logRecord.setVehicleId(vehicle.getId());
            logRecord.setStatusFrom(currentStatus);
            logRecord.setStatusTo(newStatus);
            logRecord.setReason(reason);
            logRecord.setSource(source);
            logRecord.setExtInfo(extInfo);
            logRecord.setEventTime(evt);
            onlineLogService.insert(logRecord);
        } catch (Exception e) {
            if (!properties.isLogWriteDegradeOnError()) throw e;
            log.warn("VehicleOnlineLog insert degrade for vin={}: {}", vin, e.getMessage());
        }

        // 8) 推送回调（WebSocket 等），异步避免阻塞主链路
        try {
            dispatchPush(vin, currentStatus, newStatus, reason, evt);
        } catch (Exception e) {
            log.warn("dispatchPush failed for vin={}", vin, e);
        }

        log.info("Vehicle online status changed: vin={} {} -> {}, reason={}, source={}",
                vin, currentStatus, newStatus, reason, source);
        return true;
    }

    /**
     * 业务信号/心跳隐含刷新：只更新 lastSeen，不翻转状态
     * <p>
     * 如果当前缓存状态为 OFFLINE/UNKNOWN 且距离上次上报超过阈值，
     * 则改为显式 ONLINE 事件（状态翻转+日志+推送）。
     */
    public void touchLastSeen(String vin, LocalDateTime eventTime, String source, String extInfo) {
        if (!StringUtils.hasText(vin)) return;
        LocalDateTime evt = eventTime != null ? eventTime : LocalDateTime.now();
        long evtMs = toEpochMs(evt);

        // 先更新 lastSeen
        String hashKey = properties.getRedisHashPrefix() + vin;
        redisTemplate.opsForHash().put(hashKey, "lastSeen", String.valueOf(evtMs));
        redisTemplate.expire(hashKey, properties.getRedisKeyTtlSeconds(), TimeUnit.SECONDS);

        Integer cachedStatus = getStatusFromCache(vin);
        if (cachedStatus == null) return;

        // 当前是 OFFLINE/UNKNOWN，但有新的业务信号到了 → 判定为上线（SIGNAL_IMPLICIT）
        if (cachedStatus != VehicleOnlineStatus.ONLINE.getCode()) {
            onStatusEvent(vin, VehicleOnlineStatus.ONLINE.getCode(),
                    VehicleOnlineReason.SIGNAL_IMPLICIT, source, evt, extInfo);
        }
    }

    /**
     * 兜底扫描：遍历 Redis SET 中标记为在线的车辆，超过阈值无上报则强制离线。
     *
     * @return 被强制置离线的 VIN 数量
     */
    public int scanAndForceOffline() {
        if (!properties.isTimeoutScanEnabled()) {
            log.debug("timeout scan disabled, skip");
            return 0;
        }
        int timeoutMillis = properties.getTimeoutMinutes() * 60 * 1000;
        long now = System.currentTimeMillis();
        Set<String> onlineVins = getOnlineVinsFromCache();
        if (onlineVins == null || onlineVins.isEmpty()) return 0;

        int forced = 0;
        for (String vin : onlineVins) {
            try {
                Long lastSeen = getLastSeenMs(vin);
                if (lastSeen == null) {
                    // Hash 已失效或被清除 → 安全起见置离线
                    forceOffline0(vin, now, "missing-cache");
                    forced++;
                    continue;
                }
                if (now - lastSeen > timeoutMillis) {
                    forceOffline0(vin, now, "timeout-" + (now - lastSeen) / 1000 + "s");
                    forced++;
                }
            } catch (Exception e) {
                log.warn("scanAndForceOffline error for vin={}", vin, e);
            }
        }
        log.info("scanAndForceOffline done: totalCheck={}, forcedOffline={}", onlineVins.size(), forced);
        return forced;
    }

    // ========================================================================
    // 内部帮助
    // ========================================================================

    private void forceOffline0(String vin, long nowMs, String detail) {
        LocalDateTime eventTime = LocalDateTime.now();
        String ext = StringUtils.hasText(detail) ? "{\"detail\":\"" + detail + "\"}" : null;
        onStatusEvent(vin, VehicleOnlineStatus.OFFLINE.getCode(),
                VehicleOnlineReason.TIMEOUT_CHECK, "SCHEDULER", eventTime, ext);
    }

    private int getCurrentStatusFromCacheOrDb(Vehicle vehicle) {
        Integer status = getStatusFromCache(vehicle.getVin());
        if (status != null) return status;
        if (vehicle.getStatus() == null) return VehicleOnlineStatus.UNKNOWN.getCode();
        return vehicle.getStatus();
    }

    private Integer getStatusFromCache(String vin) {
        String hashKey = properties.getRedisHashPrefix() + vin;
        Object o = redisTemplate.opsForHash().get(hashKey, "status");
        if (o == null) return null;
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long getLastSeenMs(String vin) {
        String hashKey = properties.getRedisHashPrefix() + vin;
        Object o = redisTemplate.opsForHash().get(hashKey, "lastSeen");
        if (o == null) return null;
        try {
            return Long.parseLong(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Set<String> getOnlineVinsFromCache() {
        String key = properties.getRedisOnlineSetKey();
        Set<String> members = redisTemplate.opsForSet().members(key);
        if (members == null) return Collections.emptySet();
        return members;
    }

    private void updateRedisState(String vin, int newStatus, long eventTimeMs) {
        String hashKey = properties.getRedisHashPrefix() + vin;
        String setKey = properties.getRedisOnlineSetKey();
        redisTemplate.opsForHash().put(hashKey, "status", String.valueOf(newStatus));
        redisTemplate.opsForHash().put(hashKey, "lastSeen", String.valueOf(eventTimeMs));
        redisTemplate.expire(hashKey, properties.getRedisKeyTtlSeconds(), TimeUnit.SECONDS);

        if (newStatus == VehicleOnlineStatus.ONLINE.getCode()) {
            redisTemplate.opsForSet().add(setKey, vin);
        } else {
            redisTemplate.opsForSet().remove(setKey, vin);
        }
    }

    private void touchVehicleLastOnlineTimeIfNeeded(Vehicle vehicle, LocalDateTime eventTime) {
        LocalDateTime last = vehicle.getLastOnlineTime();
        // 距离上次落库超过 1 分钟再更新，降低 MySQL 写入频率
        if (last == null || java.time.Duration.between(last, eventTime).toMinutes() >= 1) {
            try {
                Vehicle upd = new Vehicle();
                upd.setId(vehicle.getId());
                upd.setLastOnlineTime(eventTime);
                upd.setUpdateTime(LocalDateTime.now());
                vehicleService.updateById(upd);
            } catch (Exception e) {
                log.warn("touchVehicleLastOnlineTime failed for vin={}", vehicle.getVin(), e);
            }
        }
    }

    /**
     * 状态变更后直接通过 service-vehicle 的 WebSocket 推送前端
     */
    private void dispatchPush(String vin, int statusFrom, int statusTo, String reason, LocalDateTime evt) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("type", "onlineStatus");
            payload.put("vin", vin);
            payload.put("status", statusTo);
            payload.put("statusFrom", statusFrom);
            payload.put("statusLabel", VehicleOnlineStatus.fromCode(statusTo).getLabel());
            payload.put("reason", reason);
            payload.put("eventTime", evt == null ? null : evt.toString());
            payload.put("timestamp", toEpochMs(evt == null ? LocalDateTime.now() : evt));
            webSocketHandler.broadcast(vin, payload.toJSONString());
        } catch (Exception e) {
            log.warn("dispatchPush (WebSocket) failed for vin={}", vin, e);
        }
    }

    private static long toEpochMs(LocalDateTime dateTime) {
        return dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
