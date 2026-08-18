/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson2.JSON
 *  com.alibaba.fastjson2.JSONArray
 *  com.alibaba.fastjson2.JSONObject
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.kafka.annotation.KafkaListener
 *  org.springframework.stereotype.Component
 */
package com.vrd.vehicle.kafka;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.vehicle.rule.engine.AlertRuleEngine;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertEvaluationConsumer {
    private static final Logger log = LoggerFactory.getLogger(AlertEvaluationConsumer.class);
    private final AlertRuleEngine alertRuleEngine;

    public AlertEvaluationConsumer(AlertRuleEngine alertRuleEngine) {
        this.alertRuleEngine = alertRuleEngine;
    }

    @KafkaListener(topics={"${kafka.topics.alert-signals:vehicle-signals}"}, groupId="vehicle-alert-evaluator")
    public void consume(String message) {
        try {
            JSONObject envelope = JSON.parseObject((String)message);
            String vin = envelope.getString("vin");
            String payload = envelope.getString("payload");
            if (vin == null || payload == null) {
                return;
            }
            JSONObject data = JSON.parseObject((String)payload);
            Long vehicleId = data.getLong("vehicleId");
            JSONArray signals = data.getJSONArray("signals");
            if (signals == null || signals.isEmpty()) {
                return;
            }
            for (int i = 0; i < signals.size(); ++i) {
                JSONObject signal = signals.getJSONObject(i);
                String signalName = signal.getString("name");
                String signalValue = signal.getString("value");
                String messageName = signal.getString("messageName");
                if (signalName == null || signalValue == null) continue;
                try {
                    BigDecimal value = new BigDecimal(signalValue);
                    this.alertRuleEngine.evaluate(vin, vehicleId, signalName, value, messageName);
                    continue;
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to evaluate alert rules", (Throwable)e);
        }
    }
}

