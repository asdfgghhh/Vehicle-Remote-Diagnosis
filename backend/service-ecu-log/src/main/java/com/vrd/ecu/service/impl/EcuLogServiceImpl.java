package com.vrd.ecu.service.impl;

import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageService;
import com.vrd.ecu.dto.EcuLogRecord;
import com.vrd.ecu.dto.PageResult;
import com.vrd.ecu.service.EcuLogClickHouseService;
import com.vrd.ecu.service.EcuLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

@Service
public class EcuLogServiceImpl implements EcuLogService {

    @Autowired
    private EcuLogClickHouseService clickHouseService;

    @Autowired
    private StorageService storageService;

    @Value("${file.log.temp-path:/data/vrd/logs/temp}")
    private String tempPath;

    @Override
    public PageResult<EcuLogRecord> page(Integer current, Integer size, String vin, String ecuType,
                                     LocalDateTime startTime, LocalDateTime endTime) {
        return clickHouseService.search(current, size, vin, ecuType, startTime, endTime);
    }

    @Override
    public File downloadLog(Long recordId) {
        EcuLogRecord record = clickHouseService.getById(recordId);
        if (record == null) {
            throw new BusinessException("日志记录不存在");
        }

        File tempFile = new File(tempPath, recordId + "_" + record.getFileName());
        tempFile.getParentFile().mkdirs();

        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            storageService.download(record.getStorageKey(), outputStream);
            return tempFile;
        } catch (IOException e) {
            throw new BusinessException("下载日志文件失败: " + e.getMessage());
        }
    }

    @Override
    public EcuLogRecord getById(Long recordId) {
        return clickHouseService.getById(recordId);
    }
}
