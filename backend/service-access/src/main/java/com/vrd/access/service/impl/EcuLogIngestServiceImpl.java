package com.vrd.access.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.vrd.access.dto.EcuLogRecord;
import com.vrd.access.service.EcuLogIngestService;
import com.vrd.common.bigdata.BigDataClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class EcuLogIngestServiceImpl implements EcuLogIngestService {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BigDataClient bigDataClient;

    public EcuLogIngestServiceImpl(BigDataClient bigDataClient) {
        this.bigDataClient = bigDataClient;
    }

    @Override
    public void insertRecord(EcuLogRecord record) {
        JSONObject row = new JSONObject();
        row.put("id", record.getId() != null ? record.getId() : System.currentTimeMillis() * 1000L + (int)(Math.random() * 1000));
        row.put("vin", record.getVin() != null ? record.getVin() : "");
        row.put("ecu_type", record.getEcuType() != null ? record.getEcuType() : "");
        row.put("log_start_time", record.getLogStartTime() != null ? 
                record.getLogStartTime().format(DATETIME_FORMAT) : 
                LocalDateTime.now().format(DATETIME_FORMAT));
        row.put("log_end_time", record.getLogEndTime() != null ? 
                record.getLogEndTime().format(DATETIME_FORMAT) : 
                LocalDateTime.now().format(DATETIME_FORMAT));
        row.put("upload_start_time", record.getUploadStartTime() != null ? 
                record.getUploadStartTime().format(DATETIME_FORMAT) : 
                LocalDateTime.now().format(DATETIME_FORMAT));
        row.put("upload_end_time", record.getUploadEndTime() != null ? 
                record.getUploadEndTime().format(DATETIME_FORMAT) : 
                LocalDateTime.now().format(DATETIME_FORMAT));
        row.put("storage_address", record.getStorageAddress() != null ? record.getStorageAddress() : "");
        row.put("storage_key", record.getStorageKey() != null ? record.getStorageKey() : "");
        row.put("storage_type", record.getStorageType() != null ? record.getStorageType() : "");
        row.put("file_name", record.getFileName() != null ? record.getFileName() : "");
        row.put("file_size", record.getFileSize() != null ? record.getFileSize() : 0L);
        row.put("file_md5", record.getFileMd5() != null ? record.getFileMd5() : "");
        
        bigDataClient.insertJson("ecu_log_records", List.of(row));
    }

    @Override
    public boolean existsByMd5(String fileMd5) {
        if (!StringUtils.hasText(fileMd5)) {
            return false;
        }
        return bigDataClient.queryCount(
                "SELECT count(*) FROM ecu_log_records WHERE file_md5 = '" + fileMd5.trim() + "'") > 0;
    }
}