/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.core.conditions.Wrapper
 *  com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
 *  com.baomidou.mybatisplus.core.metadata.IPage
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
 *  org.springframework.stereotype.Service
 *  org.springframework.util.StringUtils
 */
package com.vrd.vehicle.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
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
public class SyncLogServiceImpl
extends ServiceImpl<SyncLogMapper, SyncLog>
implements SyncLogService {
    @Override
    public Page<SyncLog> page(Integer current, Integer size, String syncType, String status, String keyword) {
        Page page = new Page((long)current.intValue(), (long)size.intValue());
        LambdaQueryWrapper wrapper = new LambdaQueryWrapper();
        if (StringUtils.hasText((String)syncType)) {
            wrapper.eq(SyncLog::getSyncType, (Object)syncType.toUpperCase());
        }
        if (StringUtils.hasText((String)status)) {
            wrapper.eq(SyncLog::getStatus, (Object)status.toUpperCase());
        }
        if (StringUtils.hasText((String)keyword)) {
            wrapper.and(w -> ((LambdaQueryWrapper)((LambdaQueryWrapper)((LambdaQueryWrapper)((LambdaQueryWrapper)w.like(SyncLog::getVin, (Object)keyword)).or()).like(SyncLog::getSource, (Object)keyword)).or()).like(SyncLog::getMessage, (Object)keyword));
        }
        wrapper.orderByDesc(SyncLog::getCreateTime);
        IPage result = this.page((IPage)page, (Wrapper)wrapper);
        return (Page)result;
    }
}

