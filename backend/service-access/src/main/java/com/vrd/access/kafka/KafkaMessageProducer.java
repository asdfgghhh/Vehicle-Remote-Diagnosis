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
}

