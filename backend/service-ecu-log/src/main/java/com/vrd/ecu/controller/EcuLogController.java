package com.vrd.ecu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.ecu.entity.EcuLogFile;
import com.vrd.ecu.service.EcuLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/ecu-log")
public class EcuLogController {

    @Autowired
    private EcuLogService ecuLogService;

    @GetMapping("/page")
    public Result<Page<EcuLogFile>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long vehicleId) {
        Page<EcuLogFile> page = ecuLogService.page(current, size, keyword, vehicleId);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<EcuLogFile> getById(@PathVariable Long id) {
        EcuLogFile logFile = ecuLogService.getById(id);
        return Result.success(logFile);
    }

    @PostMapping("/init-upload")
    public Result<String> initUpload(
            @RequestParam String fileName,
            @RequestParam Long fileSize,
            @RequestParam String md5,
            @RequestParam Long vehicleId,
            @RequestParam String vin,
            @RequestParam String ecuType) {
        String chunkId = ecuLogService.initUpload(fileName, fileSize, md5, vehicleId, vin, ecuType);
        return Result.success(chunkId);
    }

    @PostMapping("/upload-chunk")
    public Result<String> uploadChunk(
            @RequestParam String chunkId,
            @RequestParam String fileMd5,
            @RequestParam Integer chunkNumber,
            @RequestParam Long chunkSize,
            @RequestParam("file") MultipartFile file) {
        try {
            String result = ecuLogService.uploadChunk(chunkId, fileMd5, chunkNumber, chunkSize, file.getInputStream());
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/merge-chunks")
    public Result<EcuLogFile> mergeChunks(
            @RequestParam String fileMd5,
            @RequestParam String fileName,
            @RequestParam Long vehicleId,
            @RequestParam String vin,
            @RequestParam String ecuType) {
        EcuLogFile result = ecuLogService.mergeChunks(fileMd5, fileName, vehicleId, vin, ecuType);
        return Result.success(result);
    }

    @GetMapping("/check-upload")
    public Result<EcuLogService.CheckUploadResult> checkUpload(@RequestParam String fileMd5) {
        EcuLogService.CheckUploadResult result = ecuLogService.checkUpload(fileMd5);
        return Result.success(result);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        try {
            File file = ecuLogService.downloadLog(id);
            Resource resource = new FileSystemResource(file);
            
            EcuLogFile logFile = ecuLogService.getById(id);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + logFile.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
