package com.vrd.access.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.vrd.access.entity.VehicleSignal;
import com.vrd.access.service.SignalIngestService;
import com.vrd.common.clickhouse.ClickHouseHttpClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SignalIngestServiceImpl implements SignalIngestService {

    private final ClickHouseHttpClient clickHouseHttpClient;

    public SignalIngestServiceImpl(ClickHouseHttpClient clickHouseHttpClient) {
        this.clickHouseHttpClient = clickHouseHttpClient;
    }

    @Override
    public void saveBatch(List<VehicleSignal> signals) {
        if (signals.isEmpty()) {
            return;
        }
        
        List<JSONObject> rows = signals.stream().map(signal -> {
            JSONObject row = new JSONObject();
            row.put("id", signal.getId() != null ? signal.getId() : ClickHouseHttpClient.generateId());
            row.put("vin", signal.getVin() != null ? signal.getVin() : "");
            row.put("vehicle_id", signal.getVehicleId() != null ? signal.getVehicleId() : 0L);
            row.put("signal_name", signal.getSignalName() != null ? signal.getSignalName() : "");
            row.put("signal_value", signal.getSignalValue() != null ? signal.getSignalValue() : "");
            row.put("numeric_value", signal.getNumericValue() != null ? signal.getNumericValue().doubleValue() : 0D);
            row.put("unit", signal.getUnit() != null ? signal.getUnit() : "");
            row.put("timestamp", signal.getTimestamp() != null ? signal.getTimestamp() : 0L);
            row.put("signal_time", signal.getSignalTime() != null ? 
                    ClickHouseHttpClient.formatDateTime(signal.getSignalTime()) : 
                    ClickHouseHttpClient.formatDateTime(null));
            row.put("message_name", signal.getMessageName() != null ? signal.getMessageName() : "");
            row.put("message_id", signal.getMessageId() != null ? signal.getMessageId() : 0);
            return row;
        }).collect(Collectors.toList());
        
        clickHouseHttpClient.insertJson("vehicle_signal_records", rows);
    }
}
