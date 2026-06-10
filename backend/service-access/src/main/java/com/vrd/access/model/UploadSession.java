package com.vrd.access.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class UploadSession {

    private String uploadId;
    private String vin;
    private String ecuType;
    private String fileName;
    private Long fileSize;
    private String fileMd5;
    private LocalDateTime logStartTime;
    private LocalDateTime logEndTime;
    private LocalDateTime uploadStartTime;
    private final Set<Integer> uploadedChunks = ConcurrentHashMap.newKeySet();
}
