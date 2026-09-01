/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson2.JSONObject
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.integration.annotation.ServiceActivator
 *  org.springframework.messaging.Message
 *  org.springframework.stereotype.Component
 *  org.springframework.util.StringUtils
 */
package com.vrd.access.mqtt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.access.kafka.KafkaMessageProducer;
import com.vrd.access.websocket.SignalWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MQTT 车辆在线状态接收器（service-access）
 * <p>
 * 订阅 topic 格式：vrd/{vin}/status ，示例 payload：
 * <pre>
 * {"event":"online","accStatus":1,"signalStrength":-75,"batteryVoltage":12.8,"reason":"ignition_on"}
 * {"event":"offline","reason":"graceful_shutdown"}
 * </pre>
 * 处理逻辑：
 * <ol>
 *   <li>从 topic 中提取 VIN</li>
 *   <li>组装在线状态事件 JSON</li>
 *   <li>发送 Kafka (vehicle-online-status) → 由 service-vehicle 消费者落库+写日志</li>
 *   <li><b>低延迟路径</b>：直接通过本地 {@link SignalWebSocketHandler} 推送 onlineStatus 消息，
 *       前端无需等 Kafka 消费链路即可感知状态变更</li>
 * </ol>
 */
@Component
public class MqttVehicleStatusReceiver {
    private static final Logger log = LoggerFactory.getLogger(MqttVehicleStatusReceiver.class);
    private static final String MQTT_TOPIC_HEADER = "mqtt_receivedTopic";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final KafkaMessageProducer kafkaMessageProducer;
    private final SignalWebSocketHandler signalWebSocketHandler;

    public MqttVehicleStatusReceiver(KafkaMessageProducer kafkaMessageProducer,
                                     SignalWebSocketHandler signalWebSocketHandler) {
        this.kafkaMessageProducer = kafkaMessageProducer;
        this.signalWebSocketHandler = signalWebSocketHandler;
    }

    @ServiceActivator(inputChannel = "mqttStatusInputChannel")
    public void handleMessage(Message<?> message) {
        try {
            String topic = (String) message.getHeaders().get(MQTT_TOPIC_HEADER, String.class);
            String vin = extractVin(topic);
            if ("UNKNOWN".equals(vin)) {
                log.warn("Skip mqtt status event: vin missing in topic={}", topic);
                return;
            }
            String payload = String.valueOf(message.getPayload());
            JSONObject payloadJson;
            try {
                payloadJson = JSON.parseObject(payload);
                if (payloadJson == null) payloadJson = new JSONObject();
            } catch (Exception e) {
                log.warn("mqtt status payload is not valid JSON for vin={}, raw={}", vin, payload);
                payloadJson = new JSONObject();
                // 如果不是 JSON，按纯 text 推断：online/offline 关键字
                if (payload != null) {
                    String lower = payload.toLowerCase();
                    if (lower.contains("online") || lower.contains("on")) {
                        payloadJson.put("event", "online");
                    } else if (lower.contains("offline") || lower.contains("off")) {
                        payloadJson.put("event", "offline");
                    }
                }
            }

            // 组装 Kafka 事件
            JSONObject event = new JSONObject();
            event.put("vin", vin);
            event.putAll(payloadJson);
            event.put("source", "MQTT");
            if (!event.containsKey("timestamp")) {
                event.put("timestamp", System.currentTimeMillis());
            }

            // 1) 发 Kafka → service-vehicle 消费落库
            kafkaMessageProducer.publishVehicleOnlineStatus(event);

            // 2) 直接 WebSocket 推送（低延迟路径），
            //    即使 service-vehicle 暂时挂掉，前端也能先看到状态变化。
            String ev = event.getString("event");
            Integer status = "online".equalsIgnoreCase(ev) ? 1 : "offline".equalsIgnoreCase(ev) ? 0 : null;
            if (status != null) {
                JSONObject pushPayload = new JSONObject();
                pushPayload.put("type", "onlineStatus");
                pushPayload.put("vin", vin);
                pushPayload.put("status", status);
                pushPayload.put("statusLabel", status == 1 ? "在线" : "离线");
                pushPayload.put("reason", event.getString("reason"));
                pushPayload.put("eventTime", LocalDateTime.now().format(DTF));
                pushPayload.put("timestamp", event.getLong("timestamp"));
                String json = pushPayload.toJSONString();
                // VIN 订阅会话推送
                signalWebSocketHandler.broadcastSignal(vin, json);
                // 全局广播会话推送（车辆列表页常用）
                signalWebSocketHandler.broadcastToAll(json);
            }

            log.info("MQTT vehicle status received: vin={}, event={}", vin, ev);
        } catch (Exception e) {
            log.error("Failed to process MQTT vehicle status event", e);
        }
    }

    private String extractVin(String topic) {
        if (!StringUtils.hasText(topic)) return "UNKNOWN";
        String[] parts = topic.split("/");
        // topic: vrd/{vin}/status → parts[1]=vin
        return parts.length > 2 ? parts[1] : "UNKNOWN";
    }
}
