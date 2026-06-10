package com.vrd.access.mqtt;

import com.vrd.access.kafka.KafkaMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class MqttSignalReceiver {

    private static final String MQTT_TOPIC_HEADER = "mqtt_receivedTopic";

    private final KafkaMessageProducer kafkaMessageProducer;

    public MqttSignalReceiver(KafkaMessageProducer kafkaMessageProducer) {
        this.kafkaMessageProducer = kafkaMessageProducer;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        try {
            String topic = message.getHeaders().get(MQTT_TOPIC_HEADER, String.class);
            String vin = extractVin(topic);
            String payload = String.valueOf(message.getPayload());
            kafkaMessageProducer.publishVehicleSignal(vin, "mqtt", payload);
        } catch (Exception e) {
            log.error("Failed to forward MQTT signal to Kafka", e);
        }
    }

    private String extractVin(String topic) {
        if (!StringUtils.hasText(topic)) {
            return "UNKNOWN";
        }
        String[] parts = topic.split("/");
        return parts.length > 2 ? parts[2] : "UNKNOWN";
    }
}
