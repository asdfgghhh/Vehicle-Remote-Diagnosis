package com.vrd.access.service.impl;

import com.vrd.access.config.ClickHouseHttpClient;
import com.vrd.access.dto.EcuLogRecord;
import com.vrd.access.service.EcuLogIngestService;
import org.springframework.stereotype.Service;

@Service
public class EcuLogIngestServiceImpl implements EcuLogIngestService {

    private final ClickHouseHttpClient clickHouseHttpClient;

    public EcuLogIngestServiceImpl(ClickHouseHttpClient clickHouseHttpClient) {
        this.clickHouseHttpClient = clickHouseHttpClient;
    }

    @Override
    public void insertRecord(EcuLogRecord record) {
        clickHouseHttpClient.insertEcuLogRecord(record);
    }

    @Override
    public boolean existsByMd5(String fileMd5) {
        return clickHouseHttpClient.existsByMd5(fileMd5);
    }
}
