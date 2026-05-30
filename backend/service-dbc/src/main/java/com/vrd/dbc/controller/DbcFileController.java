package com.vrd.dbc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.dbc.entity.DbcFile;
import com.vrd.dbc.entity.DispatchLog;
import com.vrd.dbc.service.DbcFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/dbc")
public class DbcFileController {

    @Autowired
    private DbcFileService dbcFileService;

    @GetMapping("/page")
    public Result<Page<DbcFile>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<DbcFile> page = dbcFileService.page(current, size, keyword);
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
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String description) {
        DbcFile result = dbcFileService.uploadAndParse(file, version, description);
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

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DbcFile dbcFile = dbcFileService.getById(id);
        if (dbcFile == null) {
            return ResponseEntity.notFound().build();
        }
        
        File file = new File(dbcFile.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(file);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbcFile.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
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
