package com.vrd.vehicle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.vehicle.entity.SyncLog;

public interface SyncLogService extends IService<SyncLog> {

    Page<SyncLog> page(Integer current, Integer size, String syncType, String status, String keyword);
}
