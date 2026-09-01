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

/**
 * 故障配置接口（服务端口 9082，网关路由前缀 /api/vehicle/fault-config）
 * <p>
 * 维护车型维度的故障码（DTC）与告警映射配置：故障码、告警名称、
 * ECU 类型、部件码、告警级别等，供告警引擎与前端故障展示使用。
 */
@RestController
@RequestMapping(value={"/vehicle/fault-config"})
public class FaultConfigController {
    @Autowired
    private FaultConfigService faultConfigService;

    /**
     * 分页查询故障配置列表
     * <p>GET /vehicle/fault-config/page
     *
     * @param current    当前页码，默认 1
     * @param size       每页条数，默认 10
     * @param keyword    可选，按故障码/告警名称模糊过滤
     * @param modelId    可选，按车型 ID 过滤
     * @param alarmLevel 可选，按告警级别过滤
     * @return 分页结果 Page&lt;FaultConfig&gt;
     */
    @GetMapping(value={"/page"})
    public Result<Page<FaultConfig>> page(@RequestParam(defaultValue="1") Integer current, @RequestParam(defaultValue="10") Integer size, @RequestParam(required=false) String keyword, @RequestParam(required=false) Long modelId, @RequestParam(required=false) Integer alarmLevel) {
        return Result.success(this.faultConfigService.page(current, size, keyword, modelId, alarmLevel));
    }

    /**
     * 按 ID 查询故障配置详情
     * <p>GET /vehicle/fault-config/{id}
     *
     * @param id 故障配置 ID
     * @return FaultConfig 详情；不存在或已删除时返回错误
     */
    @GetMapping(value={"/{id}"})
    public Result<FaultConfig> getById(@PathVariable Long id) {
        FaultConfig config = this.faultConfigService.getById(id);
        if (config == null || config.getDeleted() == 1) {
            return Result.error("\u6545\u969c\u914d\u7f6e\u4e0d\u5b58\u5728");
        }
        return Result.success(config);
    }

    /**
     * 新增故障配置
     * <p>POST /vehicle/fault-config
     *
     * @param config 故障配置（modelId、faultCode、dtc、alarmName、ecuType、
     *               componentCode、alarmLevel、description、status）；status 缺省为 1（启用）
     * @return 创建成功后的 FaultConfig
     */
    @PostMapping
    public Result<FaultConfig> create(@RequestBody FaultConfig config) {
        config.setId(null);
        config.setDeleted(0);
        config.setStatus(config.getStatus() == null ? 1 : config.getStatus());
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        this.faultConfigService.save(config);
        return Result.success(config);
    }

    /**
     * 更新故障配置
     * <p>PUT /vehicle/fault-config/{id}
     *
     * @param id   故障配置 ID
     * @param body 待更新的故障配置字段
     * @return 更新后的 FaultConfig；配置不存在时抛 BusinessException
     */
    @PutMapping(value={"/{id}"})
    public Result<FaultConfig> update(@PathVariable Long id, @RequestBody FaultConfig body) {
        FaultConfig config = this.faultConfigService.getById(id);
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
        return Result.success(config);
    }

    /**
     * 删除故障配置（逻辑删除，置 deleted=1）
     * <p>DELETE /vehicle/fault-config/{id}
     *
     * @param id 故障配置 ID
     * @return 空结果；配置不存在时静默成功
     */
    @DeleteMapping(value={"/{id}"})
    public Result<Void> delete(@PathVariable Long id) {
        FaultConfig config = this.faultConfigService.getById(id);
        if (config != null) {
            config.setDeleted(1);
            config.setUpdateTime(LocalDateTime.now());
            this.faultConfigService.updateById(config);
        }
        return Result.success();
    }
}

