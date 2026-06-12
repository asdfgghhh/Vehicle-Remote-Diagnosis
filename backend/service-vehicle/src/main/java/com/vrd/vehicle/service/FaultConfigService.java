package com.vrd.vehicle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.vehicle.entity.FaultConfig;

public interface FaultConfigService extends IService<FaultConfig> {

    Page<FaultConfig> page(Integer current, Integer size, String keyword, Long modelId, Integer alarmLevel);
}
