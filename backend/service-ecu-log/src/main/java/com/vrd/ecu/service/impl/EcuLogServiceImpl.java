/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.exception.BusinessException
 *  com.vrd.common.storage.StorageService
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.stereotype.Service
 */
package com.vrd.ecu.service.impl;

import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageService;
import com.vrd.ecu.dto.EcuLogRecord;
import com.vrd.ecu.dto.PageResult;
import com.vrd.ecu.service.EcuLogClickHouseService;
import com.vrd.ecu.service.EcuLogService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EcuLogServiceImpl
implements EcuLogService {
    @Autowired
    private EcuLogClickHouseService clickHouseService;
    @Autowired
    private StorageService storageService;
    @Value(value="${file.log.temp-path:/data/vrd/logs/temp}")
    private String tempPath;

    @Override
    public PageResult<EcuLogRecord> page(Integer current, Integer size, String vin, String ecuType, LocalDateTime startTime, LocalDateTime endTime) {
        return this.clickHouseService.search(current, size, vin, ecuType, startTime, endTime);
    }

    @Override
    public File downloadLog(Long recordId) {
        File file;
        EcuLogRecord record = this.clickHouseService.getById(recordId);
        if (record == null) {
            throw new BusinessException("\u65e5\u5fd7\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }
        File tempFile = new File(this.tempPath, recordId + "_" + record.getFileName());
        tempFile.getParentFile().mkdirs();
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        try {
            this.storageService.download(record.getStorageKey(), (OutputStream)outputStream);
            file = tempFile;
        }
        catch (Throwable throwable) {
            try {
                try {
                    ((OutputStream)outputStream).close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                throw new BusinessException("\u4e0b\u8f7d\u65e5\u5fd7\u6587\u4ef6\u5931\u8d25: " + e.getMessage());
            }
        }
        ((OutputStream)outputStream).close();
        return file;
    }

    @Override
    public EcuLogRecord getById(Long recordId) {
        return this.clickHouseService.getById(recordId);
    }
}

