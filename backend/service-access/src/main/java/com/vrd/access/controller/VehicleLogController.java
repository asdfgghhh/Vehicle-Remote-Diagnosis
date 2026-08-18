/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.result.Result
 *  org.springframework.format.annotation.DateTimeFormat
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.multipart.MultipartFile
 */
package com.vrd.access.controller;

import com.vrd.access.dto.EcuLogRecord;
import com.vrd.access.service.VehicleLogUploadService;
import com.vrd.common.result.Result;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value={"/ecu-log/vehicle"})
public class VehicleLogController {
    private final VehicleLogUploadService vehicleLogUploadService;

    public VehicleLogController(VehicleLogUploadService vehicleLogUploadService) {
        this.vehicleLogUploadService = vehicleLogUploadService;
    }

    @PostMapping(value={"/init"})
    public Result<String> initUpload(@RequestParam(value="vin") String vin, @RequestParam(value="ecuType") String ecuType, @RequestParam(value="fileName") String fileName, @RequestParam(value="fileSize") Long fileSize, @RequestParam(value="fileMd5", required=false) String fileMd5, @RequestParam(value="logStartTime") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime logStartTime, @RequestParam(value="logEndTime") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime logEndTime) {
        String uploadId = this.vehicleLogUploadService.initUpload(vin, ecuType, fileName, fileSize, fileMd5, logStartTime, logEndTime);
        return Result.success(uploadId);
    }

    @PostMapping(value={"/chunk"})
    public Result<Void> uploadChunk(@RequestParam(value="uploadId") String uploadId, @RequestParam(value="chunkNumber") Integer chunkNumber, @RequestParam(value="chunkSize", required=false) Long chunkSize, @RequestParam(value="file") MultipartFile file) {
        try {
            this.vehicleLogUploadService.uploadChunk(uploadId, chunkNumber, file.getInputStream(), chunkSize != null ? chunkSize.longValue() : file.getSize());
            return Result.success();
        }
        catch (Exception e) {
            return Result.error((String)("\u5206\u7247\u4e0a\u4f20\u5931\u8d25: " + e.getMessage()));
        }
    }

    @PostMapping(value={"/complete"})
    public Result<EcuLogRecord> completeUpload(@RequestParam(value="uploadId") String uploadId) {
        EcuLogRecord record = this.vehicleLogUploadService.completeUpload(uploadId);
        return Result.success(record);
    }

    @PostMapping(value={"/report"})
    public Result<EcuLogRecord> reportLog(@RequestParam(value="file") MultipartFile file, @RequestParam(value="vin") String vin, @RequestParam(value="ecuType") String ecuType, @RequestParam(value="logStartTime") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime logStartTime, @RequestParam(value="logEndTime") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime logEndTime, @RequestParam(value="fileMd5", required=false) String fileMd5) {
        EcuLogRecord record = this.vehicleLogUploadService.reportLog(file, vin, ecuType, logStartTime, logEndTime, fileMd5);
        return Result.success(record);
    }
}

