/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.core.metadata.IPage
 *  com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
 *  org.springframework.stereotype.Service
 */
package com.vrd.vehicle.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.vehicle.entity.VehicleModel;
import com.vrd.vehicle.mapper.VehicleModelMapper;
import com.vrd.vehicle.service.VehicleModelService;
import org.springframework.stereotype.Service;

@Service
public class VehicleModelServiceImpl
extends ServiceImpl<VehicleModelMapper, VehicleModel>
implements VehicleModelService {
    @Override
    public Page<VehicleModel> page(Integer current, Integer size, String keyword) {
        Page page = new Page((long)current.intValue(), (long)size.intValue());
        IPage result = ((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)((LambdaQueryChainWrapper)this.lambdaQuery().like(keyword != null, VehicleModel::getModelName, (Object)keyword)).or()).like(keyword != null, VehicleModel::getModelCode, (Object)keyword)).page((IPage)page);
        return (Page)result;
    }
}

