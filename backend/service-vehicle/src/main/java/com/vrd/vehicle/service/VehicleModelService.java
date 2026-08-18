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
import com.vrd.vehicle.entity.VehicleModel;

public interface VehicleModelService
extends IService<VehicleModel> {
    public Page<VehicleModel> page(Integer var1, Integer var2, String var3);
}

