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

/**
 * 车端 ECU 日志上传接口（服务端口 9086，网关路由前缀 /api/ecu-log/vehicle）
 * <p>
 * 面向车端 T-Box 的日志上传通道，支持两种模式：
 * <ul>
 *   <li>分片上传：init（初始化）→ chunk（逐片上传）→ complete（合并完成）</li>
 *   <li>直传：report（小文件一次性上传）</li>
 * </ul>
 */
@RestController
@RequestMapping(value={"/ecu-log/vehicle"})
public class VehicleLogController {
    private final VehicleLogUploadService vehicleLogUploadService;

    public VehicleLogController(VehicleLogUploadService vehicleLogUploadService) {
        this.vehicleLogUploadService = vehicleLogUploadService;
    }

    /**
     * 初始化分片上传（分片上传三步之一）
     * <p>POST /ecu-log/vehicle/init
     *
     * @param vin          车架号
     * @param ecuType      ECU 类型
     * @param fileName     日志文件名
     * @param fileSize     文件总大小（字节）
     * @param fileMd5      可选，文件 MD5 校验值
     * @param logStartTime 日志采集开始时间（格式 yyyy-MM-dd HH:mm:ss）
     * @param logEndTime   日志采集结束时间（格式 yyyy-MM-dd HH:mm:ss）
     * @return String 本次上传的全局 uploadId（后续 chunk/complete 接口使用）
     */
    @PostMapping(value={"/init"})
    public Result<String> initUpload(@RequestParam(value="vin") String vin, @RequestParam(value="ecuType") String ecuType, @RequestParam(value="fileName") String fileName, @RequestParam(value="fileSize") Long fileSize, @RequestParam(value="fileMd5", required=false) String fileMd5, @RequestParam(value="logStartTime") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime logStartTime, @RequestParam(value="logEndTime") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime logEndTime) {
        String uploadId = this.vehicleLogUploadService.initUpload(vin, ecuType, fileName, fileSize, fileMd5, logStartTime, logEndTime);
        return Result.success(uploadId);
    }

    /**
     * 上传单个日志分片（分片上传三步之二）
     * <p>POST /ecu-log/vehicle/chunk（multipart/form-data）
     * <p>可乱序上传，服务端按 chunkNumber 归位，complete 时合并。
     *
     * @param uploadId    init 接口返回的上传 ID
     * @param chunkNumber 分片序号（从 1 开始）
     * @param chunkSize   可选，分片大小；缺省取上传文件实际大小
     * @param file        分片数据（multipart 文件域）
     * @return 空结果；上传失败返回错误信息
     */
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

    /**
     * 完成分片上传（分片上传三步之三）
     * <p>POST /ecu-log/vehicle/complete?uploadId={uploadId}
     * <p>合并全部分片、校验完整性并落盘/入对象存储，生成 ECU 日志记录。
     *
     * @param uploadId init 接口返回的上传 ID
     * @return EcuLogRecord 生成的日志记录元数据
     */
    @PostMapping(value={"/complete"})
    public Result<EcuLogRecord> completeUpload(@RequestParam(value="uploadId") String uploadId) {
        EcuLogRecord record = this.vehicleLogUploadService.completeUpload(uploadId);
        return Result.success(record);
    }

    /**
     * 一次性上报日志文件（小文件直传模式）
     * <p>POST /ecu-log/vehicle/report（multipart/form-data）
     *
     * @param file         日志文件（multipart 文件域）
     * @param vin          车架号
     * @param ecuType      ECU 类型
     * @param logStartTime 日志采集开始时间（格式 yyyy-MM-dd HH:mm:ss）
     * @param logEndTime   日志采集结束时间（格式 yyyy-MM-dd HH:mm:ss）
     * @param fileMd5      可选，文件 MD5 校验值
     * @return EcuLogRecord 生成的日志记录元数据
     */
    @PostMapping(value={"/report"})
    public Result<EcuLogRecord> reportLog(@RequestParam(value="file") MultipartFile file, @RequestParam(value="vin") String vin, @RequestParam(value="ecuType") String ecuType, @RequestParam(value="logStartTime") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime logStartTime, @RequestParam(value="logEndTime") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime logEndTime, @RequestParam(value="fileMd5", required=false) String fileMd5) {
        EcuLogRecord record = this.vehicleLogUploadService.reportLog(file, vin, ecuType, logStartTime, logEndTime, fileMd5);
        return Result.success(record);
    }
}

