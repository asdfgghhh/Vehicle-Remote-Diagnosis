package com.vrd.access.service;

import com.vrd.access.dto.EcuLogRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

public interface VehicleLogUploadService {

    String initUpload(String vin, String ecuType, String fileName, Long fileSize, String fileMd5,
                      LocalDateTime logStartTime, LocalDateTime logEndTime);

    void uploadChunk(String uploadId, Integer chunkNumber, InputStream inputStream, Long chunkSize);

    EcuLogRecord completeUpload(String uploadId);

    EcuLogRecord reportLog(MultipartFile file, String vin, String ecuType,
                           LocalDateTime logStartTime, LocalDateTime logEndTime, String fileMd5);
}
