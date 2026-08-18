/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson2.JSON
 *  com.alibaba.fastjson2.JSONArray
 *  com.alibaba.fastjson2.JSONObject
 *  com.alibaba.fastjson2.JSONWriter$Feature
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.kafka.annotation.KafkaListener
 *  org.springframework.stereotype.Component
 *  org.springframework.util.StringUtils
 */
package com.vrd.access.kafka;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.vrd.access.config.KafkaTopicProperties;
import com.vrd.access.entity.VehicleSignal;
import com.vrd.access.service.SignalIngestService;
import com.vrd.access.websocket.SignalWebSocketHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class VehicleSignalKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(VehicleSignalKafkaConsumer.class);
    private final SignalIngestService signalIngestService;
    private final KafkaTopicProperties topicProperties;
    private final SignalWebSocketHandler webSocketHandler;

    public VehicleSignalKafkaConsumer(SignalIngestService signalIngestService, KafkaTopicProperties topicProperties, SignalWebSocketHandler webSocketHandler) {
        this.signalIngestService = signalIngestService;
        this.topicProperties = topicProperties;
        this.webSocketHandler = webSocketHandler;
    }

    @KafkaListener(topics={"#{@kafkaTopicProperties.vehicleSignals}"}, groupId="access-signal-ingest")
    public void consume(String message) {
        try {
            JSONObject envelope = JSON.parseObject((String)message);
            String vin = envelope.getString("vin");
            String payload = envelope.getString("payload");
            if (!StringUtils.hasText((String)vin) || !StringUtils.hasText((String)payload)) {
                log.warn("Skip invalid signal message: {}", (Object)message);
                return;
            }
            JSONObject data = JSON.parseObject((String)payload);
            Long vehicleId = data.getLong("vehicleId");
            List<VehicleSignal> signals = this.parseSignals(vin, vehicleId, data);
            if (!signals.isEmpty()) {
                this.signalIngestService.saveBatch(signals);
                log.debug("Ingested {} signals for vin={}", (Object)signals.size(), (Object)vin);
                this.pushToWebSocket(vin, signals, data);
            }
        }
        catch (Exception e) {
            log.error("Failed to consume vehicle signal message", (Throwable)e);
        }
    }

    private void pushToWebSocket(String vin, List<VehicleSignal> signals, JSONObject data) {
        try {
            JSONObject pushMessage = new JSONObject();
            pushMessage.put((Object)"type", (Object)"signal");
            pushMessage.put((Object)"vin", (Object)vin);
            pushMessage.put((Object)"timestamp", (Object)System.currentTimeMillis());
            JSONArray signalArray = new JSONArray();
            for (VehicleSignal signal : signals) {
                JSONObject sigObj = new JSONObject();
                sigObj.put((Object)"name", (Object)signal.getSignalName());
                sigObj.put((Object)"value", (Object)signal.getSignalValue());
                sigObj.put((Object)"numericValue", (Object)signal.getNumericValue());
                sigObj.put((Object)"unit", (Object)signal.getUnit());
                sigObj.put((Object)"messageName", (Object)signal.getMessageName());
                sigObj.put((Object)"messageId", (Object)signal.getMessageId());
                sigObj.put((Object)"timestamp", (Object)signal.getTimestamp());
                signalArray.add((Object)sigObj);
            }
            pushMessage.put((Object)"signals", (Object)signalArray);
            String pushJson = pushMessage.toJSONString(new JSONWriter.Feature[0]);
            this.webSocketHandler.broadcastSignal(vin, pushJson);
            this.webSocketHandler.broadcastToAll(pushJson);
        }
        catch (Exception e) {
            log.error("Failed to push signal via WebSocket for vin={}", (Object)vin, (Object)e);
        }
    }

    private List<VehicleSignal> parseSignals(String vin, Long vehicleId, JSONObject data) {
        ArrayList<VehicleSignal> signals = new ArrayList<VehicleSignal>();
        if (!data.containsKey("signals")) {
            return signals;
        }
        JSONArray signalArray = data.getJSONArray("signals");
        for (int i = 0; i < signalArray.size(); ++i) {
            JSONObject signalObj = signalArray.getJSONObject(i);
            VehicleSignal signal = new VehicleSignal();
            signal.setVin(vin);
            signal.setVehicleId(vehicleId);
            signal.setSignalName(signalObj.getString("name"));
            signal.setSignalValue(signalObj.getString("value"));
            try {
                signal.setNumericValue(new BigDecimal(signalObj.getString("value")));
            }
            catch (Exception e) {
                signal.setNumericValue(BigDecimal.ZERO);
            }
            signal.setUnit(signalObj.getString("unit"));
            signal.setTimestamp(signalObj.getLong("timestamp"));
            signal.setMessageName(signalObj.getString("messageName"));
            signal.setMessageId(signalObj.getInteger("messageId"));
            signal.setCreateTime(LocalDateTime.now());
            if (signal.getTimestamp() != null) {
                long ts = signal.getTimestamp();
                signal.setSignalTime(LocalDateTime.ofEpochSecond(ts / 1000L, 0, ZoneOffset.ofHours(8)));
            } else {
                signal.setSignalTime(LocalDateTime.now());
            }
            signals.add(signal);
        }
        return signals;
    }
}

