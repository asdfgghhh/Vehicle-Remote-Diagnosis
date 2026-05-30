package com.vrd.vehicle.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.vehicle.entity.VehicleModel;
import com.vrd.vehicle.mapper.VehicleModelMapper;
import com.vrd.vehicle.service.VehicleModelService;
import org.springframework.stereotype.Service;

@Service
public class VehicleModelServiceImpl extends ServiceImpl<VehicleModelMapper, VehicleModel> implements VehicleModelService {

    @Override
    public Page<VehicleModel> page(Integer current, Integer size, String keyword) {
        Page<VehicleModel> page = new Page<>(current, size);
        IPage<VehicleModel> result = lambdaQuery()
                .like(keyword != null, VehicleModel::getModelName, keyword)
                .or()
                .like(keyword != null, VehicleModel::getModelCode, keyword)
                .page(page);
        return (Page<VehicleModel>) result;
    }
}
