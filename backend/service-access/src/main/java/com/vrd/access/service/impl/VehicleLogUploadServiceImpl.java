/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.exception.BusinessException
 *  com.vrd.common.storage.StorageService
 *  org.apache.commons.io.FileUtils
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.stereotype.Service
 *  org.springframework.util.StringUtils
 *  org.springframework.web.multipart.MultipartFile
 */
package com.vrd.access.service.impl;

import com.vrd.access.dto.EcuLogRecord;
import com.vrd.access.model.UploadSession;
import com.vrd.access.service.EcuLogIngestService;
import com.vrd.access.service.UploadSessionStore;
import com.vrd.access.service.VehicleLogUploadService;
import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VehicleLogUploadServiceImpl
implements VehicleLogUploadService {
    private final UploadSessionStore sessionStore;
    private final EcuLogIngestService ecuLogIngestService;
    private final StorageService storageService;
    @Value(value="${file.log.temp-path:/data/vrd/logs/temp}")
    private String tempPath;

    public VehicleLogUploadServiceImpl(UploadSessionStore sessionStore, EcuLogIngestService ecuLogIngestService, StorageService storageService) {
        this.sessionStore = sessionStore;
        this.ecuLogIngestService = ecuLogIngestService;
        this.storageService = storageService;
    }

    @Override
    public String initUpload(String vin, String ecuType, String fileName, Long fileSize, String fileMd5, LocalDateTime logStartTime, LocalDateTime logEndTime) {
        this.validateMetadata(vin, ecuType, fileName, logStartTime, logEndTime);
        if (StringUtils.hasText((String)fileMd5) && this.ecuLogIngestService.existsByMd5(fileMd5)) {
            throw new BusinessException("\u8be5\u65e5\u5fd7\u6587\u4ef6\u5df2\u4e0a\u62a5\uff0c\u8bf7\u52ff\u91cd\u590d\u4e0a\u4f20");
        }
        UploadSession session = new UploadSession();
        session.setUploadId(UUID.randomUUID().toString());
        session.setVin(vin.trim());
        session.setEcuType(ecuType.trim());
        session.setFileName(fileName.trim());
        session.setFileSize(fileSize);
        session.setFileMd5(fileMd5);
        session.setLogStartTime(logStartTime);
        session.setLogEndTime(logEndTime);
        session.setUploadStartTime(LocalDateTime.now());
        this.sessionStore.save(session);
        this.chunkDir(session.getUploadId()).mkdirs();
        return session.getUploadId();
    }

    @Override
    public void uploadChunk(String uploadId, Integer chunkNumber, InputStream inputStream, Long chunkSize) {
        this.requireSession(uploadId);
        File chunkFile = new File(this.chunkDir(uploadId), String.valueOf(chunkNumber));
        try (FileOutputStream outputStream = new FileOutputStream(chunkFile);){
            inputStream.transferTo(outputStream);
        }
        catch (IOException e) {
            throw new BusinessException("\u5206\u7247\u4e0a\u4f20\u5931\u8d25: " + e.getMessage());
        }
        this.requireSession(uploadId).getUploadedChunks().add(chunkNumber);
    }

    @Override
    public EcuLogRecord completeUpload(String uploadId) {
        UploadSession session = this.requireSession(uploadId);
        LocalDateTime uploadEndTime = LocalDateTime.now();
        try {
            String storageAddress;
            File mergedFile = this.mergeChunks(uploadId);
            long fileSize = mergedFile.length();
            String objectKey = this.buildObjectKey(session.getVin(), session.getEcuType(), session.getFileName());
            try (FileInputStream inputStream = new FileInputStream(mergedFile);){
                storageAddress = this.storageService.upload(objectKey, (InputStream)inputStream, fileSize, "application/octet-stream");
            }
            EcuLogRecord record = this.buildRecord(session, objectKey, storageAddress, fileSize, uploadEndTime);
            this.ecuLogIngestService.insertRecord(record);
            this.cleanupSession(uploadId);
            return record;
        }
        catch (IOException e) {
            throw new BusinessException("\u5408\u5e76\u4e0a\u4f20\u5931\u8d25: " + e.getMessage());
        }
    }

    @Override
    public EcuLogRecord reportLog(MultipartFile file, String vin, String ecuType, LocalDateTime logStartTime, LocalDateTime logEndTime, String fileMd5) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("\u65e5\u5fd7\u6587\u4ef6\u4e0d\u80fd\u4e3a\u7a7a");
        }
        this.validateMetadata(vin, ecuType, file.getOriginalFilename(), logStartTime, logEndTime);
        if (StringUtils.hasText((String)fileMd5) && this.ecuLogIngestService.existsByMd5(fileMd5)) {
            throw new BusinessException("\u8be5\u65e5\u5fd7\u6587\u4ef6\u5df2\u4e0a\u62a5\uff0c\u8bf7\u52ff\u91cd\u590d\u4e0a\u4f20");
        }
        LocalDateTime uploadStartTime = LocalDateTime.now();
        String fileName = file.getOriginalFilename();
        String objectKey = this.buildObjectKey(vin, ecuType, fileName);
        try {
            String storageAddress = this.storageService.upload(objectKey, file.getInputStream(), file.getSize(), file.getContentType());
            EcuLogRecord record = new EcuLogRecord();
            record.setId(this.nextId());
            record.setVin(vin.trim());
            record.setEcuType(ecuType.trim());
            record.setLogStartTime(logStartTime);
            record.setLogEndTime(logEndTime);
            record.setUploadStartTime(uploadStartTime);
            record.setUploadEndTime(LocalDateTime.now());
            record.setStorageAddress(storageAddress);
            record.setStorageKey(objectKey);
            record.setStorageType(this.storageService.getStorageType().name());
            record.setFileName(fileName);
            record.setFileSize(file.getSize());
            record.setFileMd5(StringUtils.hasText((String)fileMd5) ? fileMd5.trim() : "");
            this.ecuLogIngestService.insertRecord(record);
            return record;
        }
        catch (IOException e) {
            throw new BusinessException("\u65e5\u5fd7\u4e0a\u62a5\u5931\u8d25: " + e.getMessage());
        }
    }

    private EcuLogRecord buildRecord(UploadSession session, String objectKey, String storageAddress, long fileSize, LocalDateTime uploadEndTime) {
        EcuLogRecord record = new EcuLogRecord();
        record.setId(this.nextId());
        record.setVin(session.getVin());
        record.setEcuType(session.getEcuType());
        record.setLogStartTime(session.getLogStartTime());
        record.setLogEndTime(session.getLogEndTime());
        record.setUploadStartTime(session.getUploadStartTime());
        record.setUploadEndTime(uploadEndTime);
        record.setStorageAddress(storageAddress);
        record.setStorageKey(objectKey);
        record.setStorageType(this.storageService.getStorageType().name());
        record.setFileName(session.getFileName());
        record.setFileSize(fileSize);
        record.setFileMd5(session.getFileMd5() == null ? "" : session.getFileMd5());
        return record;
    }

    private File mergeChunks(String uploadId) throws IOException {
        File chunkDir = this.chunkDir(uploadId);
        File[] chunkFiles = chunkDir.listFiles((dir, name) -> name.matches("\\d+"));
        if (chunkFiles == null || chunkFiles.length == 0) {
            throw new BusinessException("\u6ca1\u6709\u627e\u5230\u5206\u7247\u6587\u4ef6");
        }
        List sorted = List.of(chunkFiles).stream().sorted(Comparator.comparingInt(f -> Integer.parseInt(f.getName()))).collect(Collectors.toList());
        UploadSession session = this.requireSession(uploadId);
        File mergedFile = new File(this.tempPath, uploadId + "_" + session.getFileName());
        mergedFile.getParentFile().mkdirs();
        try (FileOutputStream outputStream = new FileOutputStream(mergedFile);){
            for (File chunk : sorted) {
                FileUtils.copyFile((File)chunk, (OutputStream)outputStream);
            }
        }
        return mergedFile;
    }

    private void cleanupSession(String uploadId) {
        this.sessionStore.remove(uploadId);
        File dir = this.chunkDir(uploadId);
        if (dir.exists()) {
            try {
                FileUtils.deleteDirectory((File)dir);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    private UploadSession requireSession(String uploadId) {
        UploadSession session = this.sessionStore.get(uploadId);
        if (session == null) {
            throw new BusinessException("\u4e0a\u4f20\u4f1a\u8bdd\u4e0d\u5b58\u5728\u6216\u5df2\u8fc7\u671f");
        }
        return session;
    }

    private File chunkDir(String uploadId) {
        return new File(this.tempPath, uploadId);
    }

    private String buildObjectKey(String vin, String ecuType, String fileName) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return "logs/" + vin + "/" + ecuType + "/" + dateStr + "/" + fileName;
    }

    private long nextId() {
        return System.currentTimeMillis() * 1000L + (long)(Math.random() * 1000.0);
    }

    private void validateMetadata(String vin, String ecuType, String fileName, LocalDateTime logStartTime, LocalDateTime logEndTime) {
        if (!StringUtils.hasText((String)vin)) {
            throw new BusinessException("VIN\u53f7\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (!StringUtils.hasText((String)ecuType)) {
            throw new BusinessException("\u63a7\u5236\u5668\u7f29\u5199\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (!StringUtils.hasText((String)fileName)) {
            throw new BusinessException("\u6587\u4ef6\u540d\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (logStartTime == null || logEndTime == null) {
            throw new BusinessException("\u65e5\u5fd7\u5f00\u59cb\u65f6\u95f4\u548c\u7ed3\u675f\u65f6\u95f4\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (logStartTime.isAfter(logEndTime)) {
            throw new BusinessException("\u65e5\u5fd7\u5f00\u59cb\u65f6\u95f4\u4e0d\u80fd\u665a\u4e8e\u7ed3\u675f\u65f6\u95f4");
        }
    }
}

