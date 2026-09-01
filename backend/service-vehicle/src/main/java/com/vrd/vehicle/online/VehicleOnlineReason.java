package com.vrd.vehicle.online;

/**
 * 车辆在线状态变更原因常量
 * <p>
 * 对应 vehicle_online_log.reason 字段，便于事后排查。
 */
public final class VehicleOnlineReason {

    private VehicleOnlineReason() {}

    /** TBOX 通过 MQTT 显式上报上线 */
    public static final String MQTT_ONLINE = "mqtt_online";

    /** TBOX 通过 MQTT 显式上报下线（优雅下电） */
    public static final String MQTT_OFFLINE = "mqtt_offline";

    /** 定时任务：车辆 lastSeen 超过阈值，判定为离线 */
    public static final String TIMEOUT_CHECK = "timeout_check";

    /** 定时任务：每小时/每日在线数快照 */
    public static final String SCHEDULER_SNAPSHOT = "scheduler_snapshot";

    /** 管理后台/API 手工触发的状态变更 */
    public static final String API_UPDATE = "api_update";

    /** MQTT Broker Lifecycle Webhook 触发（可选） */
    public static final String BROKER_WEBHOOK = "broker_webhook";

    /** TBOX 业务信号/心跳消息隐含刷新，且状态发生翻转 */
    public static final String SIGNAL_IMPLICIT = "signal_implicit";
}
