/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson2.JSONObject
 *  com.vrd.common.bigdata.BigDataClient
 *  org.springframework.stereotype.Service
 */
package com.vrd.access.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.vrd.access.entity.VehicleSignal;
import com.vrd.access.service.SignalIngestService;
import com.vrd.common.bigdata.BigDataClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SignalIngestServiceImpl
implements SignalIngestService {
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final BigDataClient bigDataClient;

    public SignalIngestServiceImpl(BigDataClient bigDataClient) {
        this.bigDataClient = bigDataClient;
    }

    @Override
    public void saveBatch(List<VehicleSignal> signals) {
        if (signals.isEmpty()) {
            return;
        }
        List<JSONObject> rows = signals.stream().map(signal -> {
            JSONObject row = new JSONObject();
            row.put("id", (signal.getId() != null ? signal.getId() : System.currentTimeMillis() * 1000L + (long)((int)(Math.random() * 1000.0))));
            row.put("vin", (signal.getVin() != null ? signal.getVin() : ""));
            row.put("vehicle_id", (signal.getVehicleId() != null ? signal.getVehicleId() : 0L));
            row.put("signal_name", (signal.getSignalName() != null ? signal.getSignalName() : ""));
            row.put("signal_value", (signal.getSignalValue() != null ? signal.getSignalValue() : ""));
            row.put("numeric_value", (signal.getNumericValue() != null ? signal.getNumericValue().doubleValue() : 0.0));
            row.put("unit", (signal.getUnit() != null ? signal.getUnit() : ""));
            row.put("timestamp", (signal.getTimestamp() != null ? signal.getTimestamp() : 0L));
            row.put("signal_time", (signal.getSignalTime() != null ? signal.getSignalTime().format(DATETIME_FORMAT) : LocalDateTime.now().format(DATETIME_FORMAT)));
            row.put("message_name", (signal.getMessageName() != null ? signal.getMessageName() : ""));
            row.put("message_id", (signal.getMessageId() != null ? signal.getMessageId() : 0));
            return row;
        }).collect(Collectors.toList());
        this.bigDataClient.insertJson("vehicle_signal_records", rows);
    }
}

