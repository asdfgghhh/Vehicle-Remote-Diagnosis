/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.integration.annotation.ServiceActivator
 *  org.springframework.messaging.Message
 *  org.springframework.stereotype.Component
 *  org.springframework.util.StringUtils
 */
package com.vrd.access.mqtt;

import com.vrd.access.kafka.KafkaMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MqttSignalReceiver {
    private static final Logger log = LoggerFactory.getLogger(MqttSignalReceiver.class);
    private static final String MQTT_TOPIC_HEADER = "mqtt_receivedTopic";
    private final KafkaMessageProducer kafkaMessageProducer;

    public MqttSignalReceiver(KafkaMessageProducer kafkaMessageProducer) {
        this.kafkaMessageProducer = kafkaMessageProducer;
    }

    @ServiceActivator(inputChannel="mqttInputChannel")
    public void handleMessage(Message<?> message) {
        try {
            String topic = (String)message.getHeaders().get((Object)MQTT_TOPIC_HEADER, String.class);
            String vin = this.extractVin(topic);
            String payload = String.valueOf(message.getPayload());
            this.kafkaMessageProducer.publishVehicleSignal(vin, "mqtt", payload);
        }
        catch (Exception e) {
            log.error("Failed to forward MQTT signal to Kafka", (Throwable)e);
        }
    }

    private String extractVin(String topic) {
        if (!StringUtils.hasText((String)topic)) {
            return "UNKNOWN";
        }
        String[] parts = topic.split("/");
        return parts.length > 2 ? parts[2] : "UNKNOWN";
    }
}

