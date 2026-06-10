package com.vrd.access.kafka;

import com.alibaba.fastjson2.JSONObject;
import com.vrd.access.config.KafkaTopicProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaMessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    public KafkaMessageProducer(KafkaTemplate<String, String> kafkaTemplate,
                                KafkaTopicProperties topicProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicProperties = topicProperties;
    }

    public void publishVehicleSignal(String vin, String source, String payload) {
        JSONObject envelope = new JSONObject();
        envelope.put("vin", vin);
        envelope.put("source", source);
        envelope.put("payload", payload);
        String message = envelope.toJSONString();
        kafkaTemplate.send(topicProperties.getVehicleSignals(), vin, message);
        log.debug("Published signal message to Kafka, vin={}", vin);
    }
}
