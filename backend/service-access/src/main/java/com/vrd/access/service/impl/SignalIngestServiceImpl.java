package com.vrd.access.service.impl;

import com.vrd.access.config.ClickHouseHttpClient;
import com.vrd.access.entity.VehicleSignal;
import com.vrd.access.service.SignalIngestService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignalIngestServiceImpl implements SignalIngestService {

    private final ClickHouseHttpClient clickHouseHttpClient;

    public SignalIngestServiceImpl(ClickHouseHttpClient clickHouseHttpClient) {
        this.clickHouseHttpClient = clickHouseHttpClient;
    }

    @Override
    public void saveBatch(List<VehicleSignal> signals) {
        clickHouseHttpClient.insertSignals(signals);
    }
}
