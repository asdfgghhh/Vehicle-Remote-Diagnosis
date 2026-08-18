/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.vrd.common.exception.BusinessException
 *  com.vrd.common.result.Result
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.vrd.vehicle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.exception.BusinessException;
import com.vrd.common.result.Result;
import com.vrd.vehicle.entity.FaultConfig;
import com.vrd.vehicle.service.FaultConfigService;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/vehicle/fault-config"})
public class FaultConfigController {
    @Autowired
    private FaultConfigService faultConfigService;

    @GetMapping(value={"/page"})
    public Result<Page<FaultConfig>> page(@RequestParam(defaultValue="1") Integer current, @RequestParam(defaultValue="10") Integer size, @RequestParam(required=false) String keyword, @RequestParam(required=false) Long modelId, @RequestParam(required=false) Integer alarmLevel) {
        return Result.success(this.faultConfigService.page(current, size, keyword, modelId, alarmLevel));
    }

    @GetMapping(value={"/{id}"})
    public Result<FaultConfig> getById(@PathVariable Long id) {
        FaultConfig config = (FaultConfig)this.faultConfigService.getById(id);
        if (config == null || config.getDeleted() == 1) {
            return Result.error((String)"\u6545\u969c\u914d\u7f6e\u4e0d\u5b58\u5728");
        }
        return Result.success((Object)config);
    }

    @PostMapping
    public Result<FaultConfig> create(@RequestBody FaultConfig config) {
        config.setId(null);
        config.setDeleted(0);
        config.setStatus(config.getStatus() == null ? 1 : config.getStatus());
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        this.faultConfigService.save(config);
        return Result.success((Object)config);
    }

    @PutMapping(value={"/{id}"})
    public Result<FaultConfig> update(@PathVariable Long id, @RequestBody FaultConfig body) {
        FaultConfig config = (FaultConfig)this.faultConfigService.getById(id);
        if (config == null || config.getDeleted() == 1) {
            throw new BusinessException("\u6545\u969c\u914d\u7f6e\u4e0d\u5b58\u5728");
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
        this.faultConfigService.updateById(config);
        return Result.success((Object)config);
    }

    @DeleteMapping(value={"/{id}"})
    public Result<Void> delete(@PathVariable Long id) {
        FaultConfig config = (FaultConfig)this.faultConfigService.getById(id);
        if (config != null) {
            config.setDeleted(1);
            config.setUpdateTime(LocalDateTime.now());
            this.faultConfigService.updateById(config);
        }
        return Result.success();
    }
}

