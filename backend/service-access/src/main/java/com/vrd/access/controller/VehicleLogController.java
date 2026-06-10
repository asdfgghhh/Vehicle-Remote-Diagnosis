package com.vrd.access.controller;

import com.vrd.access.dto.EcuLogRecord;
import com.vrd.access.kafka.KafkaMessageProducer;
import com.vrd.access.service.VehicleLogUploadService;
import com.vrd.common.result.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ecu-log/vehicle")
public class VehicleLogController {

    private final VehicleLogUploadService vehicleLogUploadService;

    public VehicleLogController(VehicleLogUploadService vehicleLogUploadService) {
        this.vehicleLogUploadService = vehicleLogUploadService;
    }

    @PostMapping("/init")
    public Result<String> initUpload(
            @RequestParam("vin") String vin,
            @RequestParam("ecuType") String ecuType,
            @RequestParam("fileName") String fileName,
            @RequestParam("fileSize") Long fileSize,
            @RequestParam(value = "fileMd5", required = false) String fileMd5,
            @RequestParam("logStartTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime logStartTime,
            @RequestParam("logEndTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime logEndTime) {
        String uploadId = vehicleLogUploadService.initUpload(vin, ecuType, fileName, fileSize, fileMd5,
                logStartTime, logEndTime);
        return Result.success(uploadId);
    }

    @PostMapping("/chunk")
    public Result<Void> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkNumber") Integer chunkNumber,
            @RequestParam(value = "chunkSize", required = false) Long chunkSize,
            @RequestParam("file") MultipartFile file) {
        try {
            vehicleLogUploadService.uploadChunk(uploadId, chunkNumber,
                    file.getInputStream(), chunkSize != null ? chunkSize : file.getSize());
            return Result.success();
        } catch (Exception e) {
            return Result.error("分片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/complete")
    public Result<EcuLogRecord> completeUpload(@RequestParam("uploadId") String uploadId) {
        EcuLogRecord record = vehicleLogUploadService.completeUpload(uploadId);
        return Result.success(record);
    }

    @PostMapping("/report")
    public Result<EcuLogRecord> reportLog(
            @RequestParam("file") MultipartFile file,
            @RequestParam("vin") String vin,
            @RequestParam("ecuType") String ecuType,
            @RequestParam("logStartTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime logStartTime,
            @RequestParam("logEndTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime logEndTime,
            @RequestParam(value = "fileMd5", required = false) String fileMd5) {
        EcuLogRecord record = vehicleLogUploadService.reportLog(file, vin, ecuType, logStartTime, logEndTime, fileMd5);
        return Result.success(record);
    }
}
