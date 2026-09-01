package com.vrd.vehicle.online;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 车辆在线状态管理配置（支持 Nacos 动态刷新）
 * <p>
 * 通过配置项切换离线超时阈值、Kafka Topic、定时扫描周期等，避免硬编码。
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "vrd.vehicle.online")
public class VehicleOnlineProperties {

    // ======== Kafka ========
    /** 在线状态变更事件 Kafka Topic（vehicle-online-status） */
    private String statusEventTopic = "vehicle-online-status";

    /** 在线状态事件消费组 ID */
    private String statusEventGroupId = "vehicle-online-status-processor";

    /** 车辆在线统计快照 Topic（service-access 可选择不消费，仅作审计） */
    private String statsSnapshotTopic = "vehicle-online-stats";

    // ======== 超时判定 ========
    /** 车辆无任何上报超过该分钟数，视为离线（兜底扫描用） */
    private int timeoutMinutes = 5;

    /** 启用兜底定时扫描（生产环境必须开启） */
    private boolean timeoutScanEnabled = true;

    // ======== 定时任务 Cron ========
    /** 兜底超时扫描 Cron（默认每分钟执行一次） */
    private String scanCron = "0 * * * * ?";

    /** 每小时在线数快照 Cron（默认每小时整点） */
    private String hourlyStatCron = "0 0 * * * ?";

    /** 每日在线数快照 Cron（默认每天0点） */
    private String dailyStatCron = "0 0 0 * * ?";

    // ======== Redis ========
    /** 单辆车在线状态 Hash TTL（秒），默认 24 小时 */
    private int redisKeyTtlSeconds = 86400;

    /** Redis 单辆车状态 Hash Key 前缀 */
    private String redisHashPrefix = "vehicle:online:";

    /** Redis 所有在线车辆 VIN 集合 Key */
    private String redisOnlineSetKey = "vehicle:online:all";

    // ======== 大数据写入 ========
    /** 写入 TDengine/ClickHouse 的日志表名 */
    private String bigDataTableName = "vehicle_online_log";

    /** TDengine 超级表名（写入 USING ... TAGS 时使用） */
    private String tdengineStableName = "stb_vehicle_online_log";

    /** 写入日志的批次大小（事件多时自动批量攒批） */
    private int logWriteBatchSize = 500;

    /** 写入失败是否降级（true=失败仅记录警告日志，不影响主流程） */
    private boolean logWriteDegradeOnError = true;
}
