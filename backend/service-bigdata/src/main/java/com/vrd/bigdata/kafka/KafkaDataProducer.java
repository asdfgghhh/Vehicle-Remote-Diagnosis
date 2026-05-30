package com.vrd.bigdata.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDataProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendSignals(String message) {
        kafkaTemplate.send("vehicle-signals", message);
    }

    public void sendLogs(String message) {
        kafkaTemplate.send("ecu-logs", message);
    }

    public void sendDiagnostics(String message) {
        kafkaTemplate.send("diagnostics", message);
    }
}
