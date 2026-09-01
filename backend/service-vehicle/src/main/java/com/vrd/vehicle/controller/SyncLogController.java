/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.vrd.common.result.Result
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
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

/**
 * 车辆同步记录接口（服务端口 9082，网关路由前缀 /api/vehicle/sync-record）
 * <p>
 * 查询车辆数据同步（Kafka / 外部 API 渠道）的历史执行记录与结果。
 */
@RestController
@RequestMapping(value={"/vehicle/sync-record"})
public class SyncLogController {
    @Autowired
    private SyncLogService syncLogService;

    /**
     * 分页查询同步记录
     * <p>GET /vehicle/sync-record/page
     *
     * @param current  当前页码，默认 1
     * @param size     每页条数，默认 10
     * @param syncType 可选，同步类型（如 kafka / api）
     * @param status   可选，执行状态（成功/失败）
     * @param keyword  可选，关键字模糊过滤
     * @return 分页结果 Page&lt;SyncLog&gt;
     */
    @GetMapping(value={"/page"})
    public Result<Page<SyncLog>> page(@RequestParam(value="current", defaultValue="1") Integer current, @RequestParam(value="size", defaultValue="10") Integer size, @RequestParam(value="syncType", required=false) String syncType, @RequestParam(value="status", required=false) String status, @RequestParam(value="keyword", required=false) String keyword) {
        Page<SyncLog> page = this.syncLogService.page(current, size, syncType, status, keyword);
        return Result.success(page);
    }

    /**
     * 按 ID 查询同步记录详情
     * <p>GET /vehicle/sync-record/{id}
     *
     * @param id 同步记录 ID
     * @return SyncLog 同步记录详情；不存在时 data 为 null
     */
    @GetMapping(value={"/{id}"})
    public Result<SyncLog> getById(@PathVariable(value="id") Long id) {
        SyncLog syncLog = this.syncLogService.getById(id);
        return Result.success(syncLog);
    }
}

