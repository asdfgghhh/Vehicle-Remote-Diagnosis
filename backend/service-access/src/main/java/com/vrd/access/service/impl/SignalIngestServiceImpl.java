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
        List rows = signals.stream().map(signal -> {
            JSONObject row = new JSONObject();
            row.put((Object)"id", (Object)(signal.getId() != null ? signal.getId() : System.currentTimeMillis() * 1000L + (long)((int)(Math.random() * 1000.0))));
            row.put((Object)"vin", (Object)(signal.getVin() != null ? signal.getVin() : ""));
            row.put((Object)"vehicle_id", (Object)(signal.getVehicleId() != null ? signal.getVehicleId() : 0L));
            row.put((Object)"signal_name", (Object)(signal.getSignalName() != null ? signal.getSignalName() : ""));
            row.put((Object)"signal_value", (Object)(signal.getSignalValue() != null ? signal.getSignalValue() : ""));
            row.put((Object)"numeric_value", (Object)(signal.getNumericValue() != null ? signal.getNumericValue().doubleValue() : 0.0));
            row.put((Object)"unit", (Object)(signal.getUnit() != null ? signal.getUnit() : ""));
            row.put((Object)"timestamp", (Object)(signal.getTimestamp() != null ? signal.getTimestamp() : 0L));
            row.put((Object)"signal_time", (Object)(signal.getSignalTime() != null ? signal.getSignalTime().format(DATETIME_FORMAT) : LocalDateTime.now().format(DATETIME_FORMAT)));
            row.put((Object)"message_name", (Object)(signal.getMessageName() != null ? signal.getMessageName() : ""));
            row.put((Object)"message_id", (Object)(signal.getMessageId() != null ? signal.getMessageId() : 0));
            return row;
        }).collect(Collectors.toList());
        this.bigDataClient.insertJson("vehicle_signal_records", rows);
    }
}

