package com.vrd.vehicle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.vehicle.entity.SyncLog;
import com.vrd.vehicle.mapper.SyncLogMapper;
import com.vrd.vehicle.service.SyncLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SyncLogServiceImpl extends ServiceImpl<SyncLogMapper, SyncLog> implements SyncLogService {

    @Override
    public Page<SyncLog> page(Integer current, Integer size, String syncType, String status, String keyword) {
        Page<SyncLog> page = new Page<>(current, size);
        LambdaQueryWrapper<SyncLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(syncType)) {
            wrapper.eq(SyncLog::getSyncType, syncType.toUpperCase());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SyncLog::getStatus, status.toUpperCase());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SyncLog::getVin, keyword)
                    .or().like(SyncLog::getSource, keyword)
                    .or().like(SyncLog::getMessage, keyword));
        }

        wrapper.orderByDesc(SyncLog::getCreateTime);
        IPage<SyncLog> result = page(page, wrapper);
        return (Page<SyncLog>) result;
    }
}
