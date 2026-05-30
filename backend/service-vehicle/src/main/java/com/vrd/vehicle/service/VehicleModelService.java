package com.vrd.vehicle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleModel;

public interface VehicleModelService extends IService<VehicleModel> {
    Page<VehicleModel> page(Integer current, Integer size, String keyword);
}
