package com.vrd.vehicle.online;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleOnlineStat;
import com.vrd.vehicle.mapper.VehicleMapper;
import com.vrd.vehicle.mapper.VehicleOnlineStatMapper;
import com.vrd.vehicle.online.VehicleOnlineProperties;
import com.vrd.vehicle.online.VehicleOnlineStateManager;
import com.vrd.vehicle.online.VehicleOnlineStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 车辆在线状态定时任务：
 * <ol>
 *   <li>超时扫描兜底（每 1 分钟）</li>
 *   <li>每小时整点在线数快照 → vehicle_online_stat(granularity=hour)</li>
 *   <li>每天 0 点在线数快照  → vehicle_online_stat(granularity=day)</li>
 * </ol>
 * Cron 表达式见 {@link VehicleOnlineProperties}。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleOnlineScheduler {

    private final VehicleOnlineStateManager stateManager;
    private final VehicleOnlineProperties properties;
    private final VehicleMapper vehicleMapper;
    private final VehicleOnlineStatMapper statMapper;

    /** 兜底超时扫描 */
    @Scheduled(cron = "${vrd.vehicle.online.scan-cron:0 * * * * ?}")
    public void scanTimeoutVehicles() {
        try {
            int forced = stateManager.scanAndForceOffline();
            if (forced > 0) {
                log.info("timeout-scan finished, forcedOffline={}", forced);
            }
        } catch (Exception e) {
            log.error("timeout-scan failed", e);
        }
    }

    /** 每小时在线数快照 */
    @Scheduled(cron = "${vrd.vehicle.online.hourly-stat-cron:0 0 * * * ?}")
    public void snapshotHourly() {
        snapshotStat("hour");
    }

    /** 每天在线数快照 */
    @Scheduled(cron = "${vrd.vehicle.online.daily-stat-cron:0 0 0 * * ?}")
    public void snapshotDaily() {
        snapshotStat("day");
    }

    private void snapshotStat(String granularity) {
        try {
            long onlineCount = vehicleMapper.selectCount(
                    new LambdaQueryWrapper<Vehicle>()
                            .eq(Vehicle::getDeleted, 0)
                            .eq(Vehicle::getStatus, VehicleOnlineStatus.ONLINE.getCode()));

            LocalDateTime statTime;
            LocalDateTime now = LocalDateTime.now();
            if ("hour".equals(granularity)) {
                statTime = now.withMinute(0).withSecond(0).withNano(0);
            } else {
                statTime = now.toLocalDate().atStartOfDay();
            }

            // Upsert：唯一键 (stat_time, stat_granularity)
            VehicleOnlineStat exist = statMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<VehicleOnlineStat>()
                            .eq("stat_time", java.sql.Timestamp.valueOf(statTime))
                            .eq("stat_granularity", granularity)
                            .last("LIMIT 1"));
            if (exist != null) {
                exist.setOnlineCount((int) onlineCount);
                exist.setCreateTime(LocalDateTime.now());
                statMapper.updateById(exist);
            } else {
                VehicleOnlineStat s = new VehicleOnlineStat();
                s.setStatTime(statTime);
                s.setStatGranularity(granularity);
                s.setOnlineCount((int) onlineCount);
                s.setCreateTime(LocalDateTime.now());
                statMapper.insert(s);
            }
            log.info("online-stat snapshot: granularity={}, statTime={}, onlineCount={}",
                    granularity, statTime, onlineCount);
        } catch (Exception e) {
            log.error("online-stat snapshot failed, granularity={}", granularity, e);
        }
    }
}
