package com.vrd.access.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.vrd.access.dto.EcuLogRecord;
import com.vrd.access.service.EcuLogIngestService;
import com.vrd.common.clickhouse.ClickHouseHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class EcuLogIngestServiceImpl implements EcuLogIngestService {

    private final ClickHouseHttpClient clickHouseHttpClient;

    public EcuLogIngestServiceImpl(ClickHouseHttpClient clickHouseHttpClient) {
        this.clickHouseHttpClient = clickHouseHttpClient;
    }

    @Override
    public void insertRecord(EcuLogRecord record) {
        JSONObject row = new JSONObject();
        row.put("id", record.getId() != null ? record.getId() : ClickHouseHttpClient.generateId());
        row.put("vin", record.getVin() != null ? record.getVin() : "");
        row.put("ecu_type", record.getEcuType() != null ? record.getEcuType() : "");
        row.put("log_start_time", record.getLogStartTime() != null ? 
                ClickHouseHttpClient.formatDateTime(record.getLogStartTime()) : 
                ClickHouseHttpClient.formatDateTime(LocalDateTime.now()));
        row.put("log_end_time", record.getLogEndTime() != null ? 
                ClickHouseHttpClient.formatDateTime(record.getLogEndTime()) : 
                ClickHouseHttpClient.formatDateTime(LocalDateTime.now()));
        row.put("upload_start_time", record.getUploadStartTime() != null ? 
                ClickHouseHttpClient.formatDateTime(record.getUploadStartTime()) : 
                ClickHouseHttpClient.formatDateTime(LocalDateTime.now()));
        row.put("upload_end_time", record.getUploadEndTime() != null ? 
                ClickHouseHttpClient.formatDateTime(record.getUploadEndTime()) : 
                ClickHouseHttpClient.formatDateTime(LocalDateTime.now()));
        row.put("storage_address", record.getStorageAddress() != null ? record.getStorageAddress() : "");
        row.put("storage_key", record.getStorageKey() != null ? record.getStorageKey() : "");
        row.put("storage_type", record.getStorageType() != null ? record.getStorageType() : "");
        row.put("file_name", record.getFileName() != null ? record.getFileName() : "");
        row.put("file_size", record.getFileSize() != null ? record.getFileSize() : 0L);
        row.put("file_md5", record.getFileMd5() != null ? record.getFileMd5() : "");
        
        clickHouseHttpClient.insertJson("ecu_log_records", List.of(row));
    }

    @Override
    public boolean existsByMd5(String fileMd5) {
        if (!StringUtils.hasText(fileMd5)) {
            return false;
        }
        return clickHouseHttpClient.exists("ecu_log_records", "file_md5 = " + ClickHouseHttpClient.literal(fileMd5.trim()));
    }
}
