package com.vrd.vehicle.online;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 首页仪表盘 Redis 增量计数器
 * <p>
 * 通过 Redis INCR/DECR 维护实时计数，避免百万级车辆时 SQL COUNT(*) 全表扫描。
 * 各 Kafka Consumer 在更新 DB 后调用对应方法增减计数器，
 * 首页仪表盘通过 REST 接口读取计数器值。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardCacheManager {

    private final StringRedisTemplate redisTemplate;

    // Redis Key 常量
    private static final String KEY_ONLINE_COUNT  = "dashboard:online_count";
    private static final String KEY_ALERT_COUNT   = "dashboard:alert_count";
    private static final String KEY_FAULT_COUNT   = "dashboard:fault_count";
    private static final String KEY_TOTAL_VEHICLE = "dashboard:total_vehicle";

    /** 车辆上线：在线数 +1 */
    public void incrementOnline() {
        redisTemplate.opsForValue().increment(KEY_ONLINE_COUNT);
    }

    /** 车辆下线：在线数 -1 */
    public void decrementOnline() {
        Long val = redisTemplate.opsForValue().decrement(KEY_ONLINE_COUNT);
        if (val != null && val < 0) {
            redisTemplate.opsForValue().set(KEY_ONLINE_COUNT, "0");
        }
    }

    /** 新告警产生：告警数 +1 */
    public void incrementAlert() {
        redisTemplate.opsForValue().increment(KEY_ALERT_COUNT);
    }

    /** 新故障产生：故障数 +1 */
    public void incrementFault() {
        redisTemplate.opsForValue().increment(KEY_FAULT_COUNT);
    }

    /** 车辆总数初始化（服务启动时从 DB count 同步一次） */
    public void initTotalVehicle(long count) {
        redisTemplate.opsForValue().set(KEY_TOTAL_VEHICLE, String.valueOf(count));
    }

    /** 车辆录入：总数 +1 */
    public void incrementTotalVehicle() {
        redisTemplate.opsForValue().increment(KEY_TOTAL_VEHICLE);
    }

    /** 车辆删除：总数 -1 */
    public void decrementTotalVehicle() {
        Long val = redisTemplate.opsForValue().decrement(KEY_TOTAL_VEHICLE);
        if (val != null && val < 0) {
            redisTemplate.opsForValue().set(KEY_TOTAL_VEHICLE, "0");
        }
    }

    public long getOnlineCount() {
        return getLong(KEY_ONLINE_COUNT);
    }

    public long getAlertCount() {
        return getLong(KEY_ALERT_COUNT);
    }

    public long getFaultCount() {
        return getLong(KEY_FAULT_COUNT);
    }

    public long getTotalVehicle() {
        return getLong(KEY_TOTAL_VEHICLE);
    }

    private long getLong(String key) {
        String val = redisTemplate.opsForValue().get(key);
        if (val == null) return 0;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
