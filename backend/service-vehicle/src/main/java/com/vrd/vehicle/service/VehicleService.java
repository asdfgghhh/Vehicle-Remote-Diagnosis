package com.vrd.vehicle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.vehicle.entity.Vehicle;

public interface VehicleService extends IService<Vehicle> {
    Page<Vehicle> page(Integer current, Integer size, String keyword, Long modelId);
    
    Vehicle create(Vehicle vehicle);
    
    Vehicle update(Vehicle vehicle);
    
    void syncFromKafka();
    
    void syncFromApi(String apiUrl);
}
