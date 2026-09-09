package com.vrd.common.message;

import java.io.Serializable;

/**
 * DBC 下发回调消息（service-access -> Kafka -> service-dbc）
 *
 * <p>service-access 在 MQTT 发送到车端成功/失败后，发送回调消息到 Kafka topic dbc-dispatch-callback，
 * service-dbc 消费后更新 dispatch_log 状态。
 *
 * <p>回调状态取值：
 * <ul>
 *   <li>SENT    - MQTT broker 已接收（QoS1 PUBACK 确认）</li>
 *   <li>FAILED  - MQTT 发送失败（broker 不可达、超时等）</li>
 * </ul>
 *
 * <p>注意：车端应用结果（DOWNLOADED/APPLIED/FAILED）走 DbcDispatchAck，
 * 本回调仅表示"MQTT 指令是否成功送达 broker"这一段链路状态。
 */
public class DbcDispatchCallback implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 链路追踪 ID，与 dispatch_log.result 中的 traceId 对应 */
    private String traceId;
    /** 车辆 VIN */
    private String vin;
    /** DBC 文件 ID */
    private Long dbcFileId;
    /** 回调状态：SENT / FAILED */
    private String status;
    /** 结果描述（错误信息等） */
    private String message;
    /** 回调时间戳（毫秒） */
    private Long timestamp;

    public DbcDispatchCallback() {
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Long getDbcFileId() {
        return dbcFileId;
    }

    public void setDbcFileId(Long dbcFileId) {
        this.dbcFileId = dbcFileId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DbcDispatchCallback{" +
                "traceId='" + traceId + '\'' +
                ", vin='" + vin + '\'' +
                ", dbcFileId=" + dbcFileId +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
