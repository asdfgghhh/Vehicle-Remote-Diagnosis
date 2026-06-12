package com.vrd.dbc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.common.storage.StorageKeyUtils;
import com.vrd.common.storage.StorageService;
import com.vrd.dbc.entity.DbcFile;
import com.vrd.dbc.service.DbcFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dbc")
public class DbcFileController {

    @Autowired
    private DbcFileService dbcFileService;

    @Autowired
    private StorageService storageService;

    @GetMapping("/page")
    public Result<Page<DbcFile>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long modelId) {
        Page<DbcFile> page = dbcFileService.page(current, size, keyword, modelId);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<DbcFile> getById(@PathVariable Long id) {
        DbcFile dbcFile = dbcFileService.getById(id);
        return Result.success(dbcFile);
    }

    @PostMapping("/upload")
    public Result<DbcFile> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long modelId,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String description) {
        DbcFile result = dbcFileService.uploadAndParse(file, modelId, modelName, version, description);
        return Result.success(result);
    }

    @GetMapping("/{id}/messages")
    public Result<List<String>> getMessages(@PathVariable Long id) {
        DbcFile dbcFile = dbcFileService.getById(id);
        if (dbcFile == null) {
            return Result.error("DBC文件不存在");
        }
        List<String> messages = dbcFileService.getMessageNames(dbcFile.getParseResult());
        return Result.success(messages);
    }

    @GetMapping("/{id}/signals")
    public Result<List<Map<String, String>>> getSignals(@PathVariable Long id) {
        DbcFile dbcFile = dbcFileService.getById(id);
        if (dbcFile == null) {
            return Result.error("DBC文件不存在");
        }
        return Result.success(dbcFileService.getSignalDefinitions(dbcFile.getParseResult()));
    }

    @GetMapping("/{id}/signal-details")
    public Result<List<Map<String, String>>> getSignalDetails(@PathVariable Long id) {
        DbcFile dbcFile = dbcFileService.getById(id);
        if (dbcFile == null) {
            return Result.error("DBC文件不存在");
        }
        return Result.success(dbcFileService.getSignalDetailsByFileId(id));
    }

    @PutMapping("/{id}")
    public Result<DbcFile> update(
            @PathVariable Long id,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String description) {
        dbcFileService.updateMetadata(id, version, description);
        return Result.success(dbcFileService.getById(id));
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        dbcFileService.publish(id);
        return Result.success();
    }

    @PostMapping("/{id}/revoke")
    public Result<Void> revoke(@PathVariable Long id) {
        dbcFileService.revoke(id);
        return Result.success();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DbcFile dbcFile = dbcFileService.getById(id);
        if (dbcFile == null) {
            return ResponseEntity.notFound().build();
        }

        String objectKey = StorageKeyUtils.resolveObjectKey(
                dbcFile.getStorageKey(), dbcFile.getFilePath(), dbcFile.getStorageAddress(), storageService);
        Resource resource;
        long contentLength = dbcFile.getFileSize() != null ? dbcFile.getFileSize() : -1;

        if (objectKey != null) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                storageService.download(objectKey, outputStream);
                byte[] data = outputStream.toByteArray();
                resource = new ByteArrayResource(data);
                contentLength = data.length;
            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        } else {
            File file = StorageKeyUtils.resolveLegacyLocalFile(dbcFile.getFilePath());
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            resource = new FileSystemResource(file);
            contentLength = file.length();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbcFile.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(contentLength)
                .body(resource);
    }

    @PostMapping("/{id}/dispatch/{vehicleId}")
    public Result<Void> dispatchToVehicle(@PathVariable Long id, @PathVariable Long vehicleId) {
        dbcFileService.dispatchToVehicle(id, vehicleId);
        return Result.success();
    }

    @PostMapping("/{id}/dispatch")
    public Result<Void> dispatchToVehicles(@PathVariable Long id, @RequestBody List<Long> vehicleIds) {
        dbcFileService.dispatchToVehicles(id, vehicleIds);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        DbcFile dbcFile = dbcFileService.getById(id);
        if (dbcFile != null) {
            dbcFile.setDeleted(1);
            dbcFileService.updateById(dbcFile);
        }
        return Result.success();
    }
}
