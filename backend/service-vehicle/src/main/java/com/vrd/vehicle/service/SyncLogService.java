/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.baomidou.mybatisplus.extension.service.IService
 */
package com.vrd.vehicle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.vehicle.entity.SyncLog;

public interface SyncLogService
extends IService<SyncLog> {
    public Page<SyncLog> page(Integer var1, Integer var2, String var3, String var4, String var5);
}

