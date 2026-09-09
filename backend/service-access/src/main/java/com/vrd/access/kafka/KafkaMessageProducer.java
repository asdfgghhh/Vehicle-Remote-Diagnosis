/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson2.JSONObject
 *  com.alibaba.fastjson2.JSONWriter$Feature
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.kafka.core.KafkaTemplate
 *  org.springframework.stereotype.Component
 */
package com.vrd.access.kafka;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.vrd.access.config.KafkaTopicProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    public KafkaMessageProducer(KafkaTemplate<String, String> kafkaTemplate, KafkaTopicProperties topicProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicProperties = topicProperties;
    }

    public void publishVehicleSignal(String vin, String source, String payload) {
        JSONObject envelope = new JSONObject();
        envelope.put("vin", vin);
        envelope.put("source", source);
        envelope.put("payload", payload);
        String message = envelope.toJSONString(new JSONWriter.Feature[0]);
        this.kafkaTemplate.send(this.topicProperties.getVehicleSignals(), vin, message);
        log.debug("Published signal message to Kafka, vin={}", vin);
    }

    public void publishUdsResponse(String vin, String payload) {
        JSONObject envelope = new JSONObject();
        envelope.put("vin", vin);
        envelope.put("source", "mqtt");
        envelope.put("payload", payload);
        String message = envelope.toJSONString(new JSONWriter.Feature[0]);
        this.kafkaTemplate.send(this.topicProperties.getUdsResponses(), vin, message);
        log.debug("Published UDS response to Kafka topic={}, vin={}", this.topicProperties.getUdsResponses(), vin);
    }

    /**
     * 发布车辆在线状态事件（Kafka -> service-vehicle 消费者）
     *
     * @param eventPayload 已组装好的事件 JSON（包含 vin / event / timestamp / source / ext...）
     */
    public void publishVehicleOnlineStatus(JSONObject eventPayload) {
        if (eventPayload == null || !eventPayload.containsKey("vin")) {
            log.warn("Skip invalid online-status payload: {}", eventPayload);
            return;
        }
        if (!eventPayload.containsKey("source")) {
            eventPayload.put("source", "MQTT");
        }
        if (!eventPayload.containsKey("timestamp")) {
            eventPayload.put("timestamp", System.currentTimeMillis());
        }
        String vin = eventPayload.getString("vin");
        this.kafkaTemplate.send(
                this.topicProperties.getVehicleOnlineStatus(),
                vin,
                eventPayload.toJSONString(new JSONWriter.Feature[0]));
        log.debug("Published vehicle online status event to Kafka: vin={}, event={}",
                vin, eventPayload.getString("event"));
    }

    /**
     * 发布 DBC 下发 ACK 到 Kafka（service-access 生产 -> service-dbc 消费）
     *
     * @param vin     车辆 VIN
     * @param payload 车端上报的 ACK JSON 原文
     */
    public void publishDbcDispatchAck(String vin, String payload) {
        JSONObject envelope = new JSONObject();
        envelope.put("vin", vin);
        envelope.put("source", "mqtt");
        envelope.put("payload", payload);
        String message = envelope.toJSONString(new JSONWriter.Feature[0]);
        this.kafkaTemplate.send(this.topicProperties.getDbcDispatchAck(), vin, message);
        log.debug("Published DBC dispatch ACK to Kafka: vin={}, topic={}", vin, this.topicProperties.getDbcDispatchAck());
    }
}

