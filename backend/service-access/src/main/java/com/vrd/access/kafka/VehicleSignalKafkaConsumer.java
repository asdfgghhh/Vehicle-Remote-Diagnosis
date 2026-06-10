package com.vrd.access.kafka;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.access.config.KafkaTopicProperties;
import com.vrd.access.entity.VehicleSignal;
import com.vrd.access.service.SignalIngestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class VehicleSignalKafkaConsumer {

    private final SignalIngestService signalIngestService;
    private final KafkaTopicProperties topicProperties;

    public VehicleSignalKafkaConsumer(SignalIngestService signalIngestService,
                                      KafkaTopicProperties topicProperties) {
        this.signalIngestService = signalIngestService;
        this.topicProperties = topicProperties;
    }

    @KafkaListener(topics = "#{@kafkaTopicProperties.vehicleSignals}", groupId = "access-signal-ingest")
    public void consume(String message) {
        try {
            JSONObject envelope = JSON.parseObject(message);
            String vin = envelope.getString("vin");
            String payload = envelope.getString("payload");
            if (!StringUtils.hasText(vin) || !StringUtils.hasText(payload)) {
                log.warn("Skip invalid signal message: {}", message);
                return;
            }

            JSONObject data = JSON.parseObject(payload);
            Long vehicleId = data.getLong("vehicleId");
            List<VehicleSignal> signals = parseSignals(vin, vehicleId, data);
            if (!signals.isEmpty()) {
                signalIngestService.saveBatch(signals);
                log.debug("Ingested {} signals for vin={}", signals.size(), vin);
            }
        } catch (Exception e) {
            log.error("Failed to consume vehicle signal message", e);
        }
    }

    private List<VehicleSignal> parseSignals(String vin, Long vehicleId, JSONObject data) {
        List<VehicleSignal> signals = new ArrayList<>();
        if (!data.containsKey("signals")) {
            return signals;
        }
        JSONArray signalArray = data.getJSONArray("signals");
        for (int i = 0; i < signalArray.size(); i++) {
            JSONObject signalObj = signalArray.getJSONObject(i);
            VehicleSignal signal = new VehicleSignal();
            signal.setVin(vin);
            signal.setVehicleId(vehicleId);
            signal.setSignalName(signalObj.getString("name"));
            signal.setSignalValue(signalObj.getString("value"));
            try {
                signal.setNumericValue(new BigDecimal(signalObj.getString("value")));
            } catch (Exception e) {
                signal.setNumericValue(BigDecimal.ZERO);
            }
            signal.setUnit(signalObj.getString("unit"));
            signal.setTimestamp(signalObj.getLong("timestamp"));
            signal.setMessageName(signalObj.getString("messageName"));
            signal.setMessageId(signalObj.getInteger("messageId"));
            signal.setCreateTime(LocalDateTime.now());
            if (signal.getTimestamp() != null) {
                long ts = signal.getTimestamp();
                signal.setSignalTime(LocalDateTime.ofEpochSecond(ts / 1000, 0, ZoneOffset.ofHours(8)));
            } else {
                signal.setSignalTime(LocalDateTime.now());
            }
            signals.add(signal);
        }
        return signals;
    }
}
