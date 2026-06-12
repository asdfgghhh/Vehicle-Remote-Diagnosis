package com.vrd.vehicle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.exception.BusinessException;
import com.vrd.common.result.Result;
import com.vrd.vehicle.entity.FaultConfig;
import com.vrd.vehicle.service.FaultConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/vehicle/fault-config")
public class FaultConfigController {

    @Autowired
    private FaultConfigService faultConfigService;

    @GetMapping("/page")
    public Result<Page<FaultConfig>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) Integer alarmLevel) {
        return Result.success(faultConfigService.page(current, size, keyword, modelId, alarmLevel));
    }

    @GetMapping("/{id}")
    public Result<FaultConfig> getById(@PathVariable Long id) {
        FaultConfig config = faultConfigService.getById(id);
        if (config == null || config.getDeleted() == 1) {
            return Result.error("故障配置不存在");
        }
        return Result.success(config);
    }

    @PostMapping
    public Result<FaultConfig> create(@RequestBody FaultConfig config) {
        config.setId(null);
        config.setDeleted(0);
        config.setStatus(config.getStatus() == null ? 1 : config.getStatus());
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        faultConfigService.save(config);
        return Result.success(config);
    }

    @PutMapping("/{id}")
    public Result<FaultConfig> update(@PathVariable Long id, @RequestBody FaultConfig body) {
        FaultConfig config = faultConfigService.getById(id);
        if (config == null || config.getDeleted() == 1) {
            throw new BusinessException("故障配置不存在");
        }
        config.setModelId(body.getModelId());
        config.setFaultCode(body.getFaultCode());
        config.setDtc(body.getDtc());
        config.setAlarmName(body.getAlarmName());
        config.setEcuType(body.getEcuType());
        config.setComponentCode(body.getComponentCode());
        config.setAlarmLevel(body.getAlarmLevel());
        config.setDescription(body.getDescription());
        config.setStatus(body.getStatus());
        config.setUpdateTime(LocalDateTime.now());
        faultConfigService.updateById(config);
        return Result.success(config);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        FaultConfig config = faultConfigService.getById(id);
        if (config != null) {
            config.setDeleted(1);
            config.setUpdateTime(LocalDateTime.now());
            faultConfigService.updateById(config);
        }
        return Result.success();
    }
}
