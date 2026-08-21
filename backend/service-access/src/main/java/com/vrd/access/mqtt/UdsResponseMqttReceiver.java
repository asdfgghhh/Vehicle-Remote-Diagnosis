package com.vrd.access.mqtt;

import com.vrd.access.kafka.KafkaMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * P0-2: 车端 ECU 响应 MQTT 上行 → Kafka uds-responses
 *
 * 车端将诊断响应发布到 MQTT topic: vrd/{vin}/uds/response，
 * 本组件订阅 vrd/+/uds/response，原样转发到 Kafka uds-responses 供 service-diagnosis 消费。
 */
@Component
public class UdsResponseMqttReceiver {
    private static final Logger log = LoggerFactory.getLogger(UdsResponseMqttReceiver.class);
    private static final String MQTT_TOPIC_HEADER = "mqtt_receivedTopic";

    private final KafkaMessageProducer kafkaMessageProducer;

    public UdsResponseMqttReceiver(KafkaMessageProducer kafkaMessageProducer) {
        this.kafkaMessageProducer = kafkaMessageProducer;
    }

    @ServiceActivator(inputChannel = "mqttUdsInputChannel")
    public void handleMessage(Message<?> message) {
        try {
            String topic = (String) message.getHeaders().get(MQTT_TOPIC_HEADER, String.class);
            String vin = this.extractVin(topic);
            String payload = String.valueOf(message.getPayload());
            this.kafkaMessageProducer.publishUdsResponse(vin, payload);
            log.debug("Forwarded UDS response to Kafka, vin={}, topic={}", vin, topic);
        } catch (Exception e) {
            log.error("Failed to forward MQTT UDS response to Kafka", e);
        }
    }

    private String extractVin(String topic) {
        if (!StringUtils.hasText(topic)) {
            return "UNKNOWN";
        }
        // topic 格式: vrd/{vin}/uds/response，vin 位于第二段
        String[] parts = topic.split("/");
        return parts.length > 1 ? parts[1] : "UNKNOWN";
    }
}
