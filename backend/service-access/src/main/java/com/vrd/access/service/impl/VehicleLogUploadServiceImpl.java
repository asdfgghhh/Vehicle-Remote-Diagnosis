package com.vrd.access.service.impl;

import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageService;
import com.vrd.access.dto.EcuLogRecord;
import com.vrd.access.model.UploadSession;
import com.vrd.access.service.EcuLogIngestService;
import com.vrd.access.service.UploadSessionStore;
import com.vrd.access.service.VehicleLogUploadService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VehicleLogUploadServiceImpl implements VehicleLogUploadService {

    private final UploadSessionStore sessionStore;
    private final EcuLogIngestService ecuLogIngestService;
    private final StorageService storageService;

    @Value("${file.log.temp-path:/data/vrd/logs/temp}")
    private String tempPath;

    public VehicleLogUploadServiceImpl(UploadSessionStore sessionStore,
                                       EcuLogIngestService ecuLogIngestService,
                                       StorageService storageService) {
        this.sessionStore = sessionStore;
        this.ecuLogIngestService = ecuLogIngestService;
        this.storageService = storageService;
    }

    @Override
    public String initUpload(String vin, String ecuType, String fileName, Long fileSize, String fileMd5,
                             LocalDateTime logStartTime, LocalDateTime logEndTime) {
        validateMetadata(vin, ecuType, fileName, logStartTime, logEndTime);
        if (StringUtils.hasText(fileMd5) && ecuLogIngestService.existsByMd5(fileMd5)) {
            throw new BusinessException("该日志文件已上报，请勿重复上传");
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
        sessionStore.save(session);

        chunkDir(session.getUploadId()).mkdirs();
        return session.getUploadId();
    }

    @Override
    public void uploadChunk(String uploadId, Integer chunkNumber, InputStream inputStream, Long chunkSize) {
        requireSession(uploadId);
        File chunkFile = new File(chunkDir(uploadId), String.valueOf(chunkNumber));
        try (OutputStream outputStream = new FileOutputStream(chunkFile)) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            throw new BusinessException("分片上传失败: " + e.getMessage());
        }
        requireSession(uploadId).getUploadedChunks().add(chunkNumber);
    }

    @Override
    public EcuLogRecord completeUpload(String uploadId) {
        UploadSession session = requireSession(uploadId);
        LocalDateTime uploadEndTime = LocalDateTime.now();
        try {
            File mergedFile = mergeChunks(uploadId);
            long fileSize = mergedFile.length();
            String objectKey = buildObjectKey(session.getVin(), session.getEcuType(), session.getFileName());
            String storageAddress;
            try (InputStream inputStream = new FileInputStream(mergedFile)) {
                storageAddress = storageService.upload(objectKey, inputStream, fileSize, "application/octet-stream");
            }
            EcuLogRecord record = buildRecord(session, objectKey, storageAddress, fileSize, uploadEndTime);
            ecuLogIngestService.insertRecord(record);
            cleanupSession(uploadId);
            return record;
        } catch (IOException e) {
            throw new BusinessException("合并上传失败: " + e.getMessage());
        }
    }

    @Override
    public EcuLogRecord reportLog(MultipartFile file, String vin, String ecuType,
                                  LocalDateTime logStartTime, LocalDateTime logEndTime, String fileMd5) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("日志文件不能为空");
        }
        validateMetadata(vin, ecuType, file.getOriginalFilename(), logStartTime, logEndTime);
        if (StringUtils.hasText(fileMd5) && ecuLogIngestService.existsByMd5(fileMd5)) {
            throw new BusinessException("该日志文件已上报，请勿重复上传");
        }

        LocalDateTime uploadStartTime = LocalDateTime.now();
        String fileName = file.getOriginalFilename();
        String objectKey = buildObjectKey(vin, ecuType, fileName);
        try {
            String storageAddress = storageService.upload(
                    objectKey, file.getInputStream(), file.getSize(), file.getContentType());

            EcuLogRecord record = new EcuLogRecord();
            record.setId(nextId());
            record.setVin(vin.trim());
            record.setEcuType(ecuType.trim());
            record.setLogStartTime(logStartTime);
            record.setLogEndTime(logEndTime);
            record.setUploadStartTime(uploadStartTime);
            record.setUploadEndTime(LocalDateTime.now());
            record.setStorageAddress(storageAddress);
            record.setStorageKey(objectKey);
            record.setStorageType(storageService.getStorageType().name());
            record.setFileName(fileName);
            record.setFileSize(file.getSize());
            record.setFileMd5(StringUtils.hasText(fileMd5) ? fileMd5.trim() : "");

            ecuLogIngestService.insertRecord(record);
            return record;
        } catch (IOException e) {
            throw new BusinessException("日志上报失败: " + e.getMessage());
        }
    }

    private EcuLogRecord buildRecord(UploadSession session, String objectKey, String storageAddress,
                                     long fileSize, LocalDateTime uploadEndTime) {
        EcuLogRecord record = new EcuLogRecord();
        record.setId(nextId());
        record.setVin(session.getVin());
        record.setEcuType(session.getEcuType());
        record.setLogStartTime(session.getLogStartTime());
        record.setLogEndTime(session.getLogEndTime());
        record.setUploadStartTime(session.getUploadStartTime());
        record.setUploadEndTime(uploadEndTime);
        record.setStorageAddress(storageAddress);
        record.setStorageKey(objectKey);
        record.setStorageType(storageService.getStorageType().name());
        record.setFileName(session.getFileName());
        record.setFileSize(fileSize);
        record.setFileMd5(session.getFileMd5() == null ? "" : session.getFileMd5());
        return record;
    }

    private File mergeChunks(String uploadId) throws IOException {
        File chunkDir = chunkDir(uploadId);
        File[] chunkFiles = chunkDir.listFiles((dir, name) -> name.matches("\\d+"));
        if (chunkFiles == null || chunkFiles.length == 0) {
            throw new BusinessException("没有找到分片文件");
        }
        List<File> sorted = List.of(chunkFiles).stream()
                .sorted(Comparator.comparingInt(f -> Integer.parseInt(f.getName())))
                .collect(Collectors.toList());
        UploadSession session = requireSession(uploadId);
        File mergedFile = new File(tempPath, uploadId + "_" + session.getFileName());
        mergedFile.getParentFile().mkdirs();
        try (OutputStream outputStream = new FileOutputStream(mergedFile)) {
            for (File chunk : sorted) {
                FileUtils.copyFile(chunk, outputStream);
            }
        }
        return mergedFile;
    }

    private void cleanupSession(String uploadId) {
        sessionStore.remove(uploadId);
        File dir = chunkDir(uploadId);
        if (dir.exists()) {
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException ignored) {
            }
        }
    }

    private UploadSession requireSession(String uploadId) {
        UploadSession session = sessionStore.get(uploadId);
        if (session == null) {
            throw new BusinessException("上传会话不存在或已过期");
        }
        return session;
    }

    private File chunkDir(String uploadId) {
        return new File(tempPath, uploadId);
    }

    private String buildObjectKey(String vin, String ecuType, String fileName) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return "logs/" + vin + "/" + ecuType + "/" + dateStr + "/" + fileName;
    }

    private long nextId() {
        return System.currentTimeMillis() * 1000 + (long) (Math.random() * 1000);
    }

    private void validateMetadata(String vin, String ecuType, String fileName,
                                  LocalDateTime logStartTime, LocalDateTime logEndTime) {
        if (!StringUtils.hasText(vin)) {
            throw new BusinessException("VIN号不能为空");
        }
        if (!StringUtils.hasText(ecuType)) {
            throw new BusinessException("控制器缩写不能为空");
        }
        if (!StringUtils.hasText(fileName)) {
            throw new BusinessException("文件名不能为空");
        }
        if (logStartTime == null || logEndTime == null) {
            throw new BusinessException("日志开始时间和结束时间不能为空");
        }
        if (logStartTime.isAfter(logEndTime)) {
            throw new BusinessException("日志开始时间不能晚于结束时间");
        }
    }
}
