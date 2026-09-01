package com.vrd.vehicle.kafka;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.vehicle.online.VehicleOnlineProperties;
import com.vrd.vehicle.online.VehicleOnlineReason;
import com.vrd.vehicle.online.VehicleOnlineStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 车辆在线状态事件 Kafka 消费者
 * <p>
 * 接入层 service-access 接收到 MQTT 上下线消息后，将事件写入
 * {@code vehicle-online-status} Topic，本消费者负责更新 MySQL/Redis/BigData，
 * 并通过注册回调推送 WebSocket。
 * <p>
 * 事件消息格式（JSON）：
 * <pre>
 * {
 *   "vin": "LSVAG4189ES123456",
 *   "event": "online",                    // online / offline
 *   "timestamp": 1710000000000,           // 毫秒级时间戳，可选
 *   "accStatus": 1,                       // 0/1，可选
 *   "signalStrength": -75,                // dBm，可选
 *   "batteryVoltage": 12.8,               // 可选
 *   "source": "MQTT"                      // MQTT / BROKER_WEBHOOK / SIGNAL 等，可选
 * }
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleOnlineStatusKafkaConsumer {

    private final VehicleOnlineStateManager stateManager;
    private final VehicleOnlineProperties properties;

    @KafkaListener(
            topics = {"${vrd.vehicle.online.status-event-topic:vehicle-online-status}"},
            groupId = "${vrd.vehicle.online.status-event-group-id:vehicle-online-status-processor}"
    )
    public void onOnlineStatusEvent(String message) {
        if (!StringUtils.hasText(message)) return;
        try {
            JSONObject event = JSON.parseObject(message);
            String vin = event.getString("vin");
            String action = event.getString("event");
            if (!StringUtils.hasText(vin) || !StringUtils.hasText(action)) {
                log.warn("Skip invalid online-status event: {}", message);
                return;
            }

            Integer newStatus = parseStatus(action);
            if (newStatus == null) {
                log.warn("Unknown online event={} for vin={}", action, vin);
                return;
            }

            LocalDateTime eventTime = parseEventTime(event.getLong("timestamp"));
            String source = event.getString("source");
            if (!StringUtils.hasText(source)) source = "KAFKA";

            String reason = newStatus == 1 ? VehicleOnlineReason.MQTT_ONLINE : VehicleOnlineReason.MQTT_OFFLINE;

            // 把可选字段拼成 extInfo JSON
            String extInfo = buildExtInfo(event);

            stateManager.onStatusEvent(vin, newStatus, reason, source, eventTime, extInfo);
        } catch (Exception e) {
            log.error("Failed to consume vehicle-online-status event: {}", message, e);
        }
    }

    private Integer parseStatus(String action) {
        if (action == null) return null;
        String a = action.toLowerCase();
        if ("online".equals(a) || "on".equals(a) || "1".equals(a)) return 1;
        if ("offline".equals(a) || "off".equals(a) || "0".equals(a)) return 0;
        return null;
    }

    private LocalDateTime parseEventTime(Long ts) {
        if (ts == null) return LocalDateTime.now();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault());
    }

    private String buildExtInfo(JSONObject event) {
        JSONObject ext = new JSONObject();
        String[] keys = {"accStatus", "signalStrength", "batteryVoltage", "reason", "clientId", "ip", "keepAlive"};
        for (String k : keys) {
            if (event.containsKey(k)) ext.put(k, event.get(k));
        }
        return ext.isEmpty() ? null : ext.toJSONString();
    }
}
