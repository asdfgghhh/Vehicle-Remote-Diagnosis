package com.vrd.signal.mqtt;

import com.vrd.signal.service.SignalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

@Component
public class MqttSignalReceiver {

    @Autowired
    private SignalService signalService;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(String payload) {
        try {
            String topic = extractTopic(payload);
            String vin = extractVin(topic);
            String data = extractData(payload);
            
            signalService.receiveSignal(vin, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractTopic(String payload) {
        return payload.split(":")[0];
    }

    private String extractVin(String topic) {
        String[] parts = topic.split("/");
        return parts.length > 2 ? parts[2] : "UNKNOWN";
    }

    private String extractData(String payload) {
        int colonIndex = payload.indexOf(":");
        return colonIndex != -1 ? payload.substring(colonIndex + 1) : payload;
    }
}
