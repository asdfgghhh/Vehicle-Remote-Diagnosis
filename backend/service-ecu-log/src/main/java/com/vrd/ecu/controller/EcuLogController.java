/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.result.Result
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.core.io.FileSystemResource
 *  org.springframework.core.io.Resource
 *  org.springframework.format.annotation.DateTimeFormat
 *  org.springframework.http.MediaType
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.vrd.ecu.controller;

import com.vrd.common.result.Result;
import com.vrd.ecu.dto.EcuLogRecord;
import com.vrd.ecu.dto.PageResult;
import com.vrd.ecu.service.EcuLogService;
import java.io.File;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/ecu-log"})
public class EcuLogController {
    @Autowired
    private EcuLogService ecuLogService;

    @GetMapping(value={"/page"})
    public Result<PageResult<EcuLogRecord>> page(@RequestParam(value="current", defaultValue="1") Integer current, @RequestParam(value="size", defaultValue="10") Integer size, @RequestParam(value="vin", required=false) String vin, @RequestParam(value="ecuType", required=false) String ecuType, @RequestParam(value="startTime", required=false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime startTime, @RequestParam(value="endTime", required=false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        PageResult<EcuLogRecord> page = this.ecuLogService.page(current, size, vin, ecuType, startTime, endTime);
        return Result.success(page);
    }

    @GetMapping(value={"/download/{id}"})
    public ResponseEntity<Resource> download(@PathVariable(value="id") Long id) {
        try {
            EcuLogRecord record = this.ecuLogService.getById(id);
            if (record == null) {
                return ResponseEntity.notFound().build();
            }
            File file = this.ecuLogService.downloadLog(id);
            FileSystemResource resource = new FileSystemResource(file);
            return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{"attachment; filename=\"" + record.getFileName() + "\""})).contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(file.length()).body((Object)resource);
        }
        catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

