package com.vrd.vehicle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.vehicle.entity.SyncLog;
import com.vrd.vehicle.service.SyncLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehicle/sync-record")
public class SyncLogController {

    @Autowired
    private SyncLogService syncLogService;

    @GetMapping("/page")
    public Result<Page<SyncLog>> page(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "syncType", required = false) String syncType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<SyncLog> page = syncLogService.page(current, size, syncType, status, keyword);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<SyncLog> getById(@PathVariable("id") Long id) {
        SyncLog syncLog = syncLogService.getById(id);
        return Result.success(syncLog);
    }
}
