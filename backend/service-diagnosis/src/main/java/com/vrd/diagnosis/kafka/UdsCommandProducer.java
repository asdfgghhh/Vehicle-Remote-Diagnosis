package com.vrd.diagnosis.kafka;

import com.alibaba.fastjson2.JSON;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UdsCommandProducer {
    private static final Logger log = LoggerFactory.getLogger(UdsCommandProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value(value="${kafka.topics.uds-commands:uds-commands}")
    private String udsCommandsTopic;

    public UdsCommandProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUdsCommand(String vin, Map<String, Object> command) {
        String message = JSON.toJSONString(command);
        this.kafkaTemplate.send(this.udsCommandsTopic, vin, message);
        log.debug("UDS command published to Kafka topic={}, vin={}", this.udsCommandsTopic, vin);
    }
}

