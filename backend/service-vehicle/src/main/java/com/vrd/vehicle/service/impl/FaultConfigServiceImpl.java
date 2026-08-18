/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.core.conditions.Wrapper
 *  com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
 *  com.baomidou.mybatisplus.core.metadata.IPage
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Service
 *  org.springframework.util.StringUtils
 */
package com.vrd.vehicle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.vehicle.entity.FaultConfig;
import com.vrd.vehicle.entity.VehicleModel;
import com.vrd.vehicle.mapper.FaultConfigMapper;
import com.vrd.vehicle.mapper.VehicleModelMapper;
import com.vrd.vehicle.service.FaultConfigService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FaultConfigServiceImpl
extends ServiceImpl<FaultConfigMapper, FaultConfig>
implements FaultConfigService {
    @Autowired
    private VehicleModelMapper vehicleModelMapper;

    @Override
    public Page<FaultConfig> page(Integer current, Integer size, String keyword, Long modelId, Integer alarmLevel) {
        Page<FaultConfig> page = new Page<>(current, size);
        LambdaQueryWrapper<FaultConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FaultConfig::getDeleted, 0);
        if (modelId != null) {
            wrapper.eq(FaultConfig::getModelId, modelId);
        }
        if (alarmLevel != null) {
            wrapper.eq(FaultConfig::getAlarmLevel, alarmLevel);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(FaultConfig::getFaultCode, keyword)
                    .or().like(FaultConfig::getDtc, keyword)
                    .or().like(FaultConfig::getAlarmName, keyword)
                    .or().like(FaultConfig::getEcuType, keyword));
        }
        wrapper.orderByDesc(FaultConfig::getUpdateTime);
        Page<FaultConfig> result = this.page(page, wrapper);
        this.fillModelNames(result.getRecords());
        return result;
    }

    private void fillModelNames(List<FaultConfig> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> modelIds = records.stream().map(FaultConfig::getModelId).filter(id -> id != null).distinct().collect(Collectors.toList());
        if (modelIds.isEmpty()) {
            return;
        }
        List<VehicleModel> models = this.vehicleModelMapper.selectBatchIds(modelIds);
        Map<Long, String> modelNameMap = models.stream().collect(Collectors.toMap(VehicleModel::getId, VehicleModel::getModelName, (a, b) -> a));
        for (FaultConfig config : records) {
            if (config.getModelId() == null) continue;
            config.setModelName(modelNameMap.get(config.getModelId()));
        }
    }
}

