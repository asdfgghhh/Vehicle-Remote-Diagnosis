package com.vrd.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆在线状态变更日志记录
 * <p>
 * 该 DTO 同时作为：
 * <ul>
 *   <li>BigDataClient.insertJson 的行对象（写入 ClickHouse / TDengine）</li>
 *   <li>Controller 查询返回对象</li>
 * </ul>
 * 对外字段命名使用小驼峰，写入时序库时由 Service 层转换成对应字段名。
 * 参考经验 #201464：通过中间转换层隔离存储字段与对外VO。
 */
@Data
public class VehicleOnlineLogRecord {

    /** 雪花 ID，用于链路追踪 */
    private Long id;

    /** 车架号 */
    private String vin;

    /** 车辆 ID（对应 MySQL vehicle.id） */
    private Long vehicleId;

    /** 变更前状态：0-离线 1-在线 2-未知 */
    private Integer statusFrom;

    /** 变更后状态：0-离线 1-在线 2-未知 */
    private Integer statusTo;

    /** 变更原因，见 {@link com.vrd.vehicle.online.VehicleOnlineReason} */
    private String reason;

    /** 来源：MQTT / KAFKA / SCHEDULER / API / BROKER_WEBHOOK */
    private String source;

    /** 扩展信息（JSON 字符串，可选） */
    private String extInfo;

    /** 事件发生时间（TBOX 上报时间或定时任务判定时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime eventTime;

    /** 落库时间（仅用于追踪写入延迟） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime createTime;

    // ========== 帮助方法 ==========

    public String getStatusLabel() {
        if (statusTo == null) return "";
        return switch (statusTo) {
            case 0 -> "离线";
            case 1 -> "在线";
            case 2 -> "未知";
            default -> "";
        };
    }
}
