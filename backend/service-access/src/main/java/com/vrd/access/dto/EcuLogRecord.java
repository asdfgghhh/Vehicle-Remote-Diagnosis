package com.vrd.access.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EcuLogRecord {

    private Long id;
    private String vin;
    private String ecuType;
    private LocalDateTime logStartTime;
    private LocalDateTime logEndTime;
    private LocalDateTime uploadStartTime;
    private LocalDateTime uploadEndTime;
    private String storageAddress;
    private String storageKey;
    private String storageType;
    private String fileName;
    private Long fileSize;
    private String fileMd5;
}
