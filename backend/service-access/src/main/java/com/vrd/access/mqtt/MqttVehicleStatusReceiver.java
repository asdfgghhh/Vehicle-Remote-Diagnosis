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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * MQTT 车辆在线状态接收器（service-access 网关层）
 * <p>
 * 订阅 topic 格式：vrd/{vin}/status ，示例 payload：
 * <pre>
 * {"event":"online","accStatus":1,"signalStrength":-75,"batteryVoltage":12.8,"reason":"ignition_on"}
 * {"event":"offline","reason":"graceful_shutdown"}
 * </pre>
 * service-access 仅作为网关：解析 MQTT 消息 → 转发 Kafka(vehicle-online-status)。
 * 不直接推送前端，前端通知由 service-vehicle 更新数据库后通过 Kafka 回传。
 * </ol>
 */
@Component
public class MqttVehicleStatusReceiver {
    private static final Logger log = LoggerFactory.getLogger(MqttVehicleStatusReceiver.class);
    private static final String MQTT_TOPIC_HEADER = "mqtt_receivedTopic";

    private final KafkaMessageProducer kafkaMessageProducer;

    public MqttVehicleStatusReceiver(KafkaMessageProducer kafkaMessageProducer) {
        this.kafkaMessageProducer = kafkaMessageProducer;
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

            // 网关职责：仅转发到 Kafka，由 service-vehicle 消费后更新数据库并通知前端
            kafkaMessageProducer.publishVehicleOnlineStatus(event);

            log.info("MQTT vehicle status forwarded to Kafka: vin={}, event={}", vin, event.getString("event"));
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
