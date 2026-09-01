/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
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
import com.vrd.common.result.Result;
import com.vrd.vehicle.dto.VehicleModelDTO;
import com.vrd.vehicle.entity.VehicleModel;
import com.vrd.vehicle.service.VehicleModelService;
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
 * 车型管理接口（服务端口 9082，网关路由前缀 /api/vehicle/model）
 * <p>
 * 维护车型主数据（品牌、厂商、动力、变速器、燃料类型、排放标准等），
 * 车辆档案通过 modelId 关联车型。
 */
@RestController
@RequestMapping(value={"/vehicle/model"})
public class VehicleModelController {
    @Autowired
    private VehicleModelService vehicleModelService;

    /**
     * 分页查询车型列表
     * <p>GET /vehicle/model/page
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @param keyword 可选，按车型名称/编码模糊过滤
     * @return 分页结果 Page&lt;VehicleModel&gt;
     */
    @GetMapping(value={"/page"})
    public Result<Page<VehicleModel>> page(@RequestParam(value="current", defaultValue="1") Integer current, @RequestParam(value="size", defaultValue="10") Integer size, @RequestParam(value="keyword", required=false) String keyword) {
        Page<VehicleModel> page = this.vehicleModelService.page(current, size, keyword);
        return Result.success(page);
    }

    /**
     * 按 ID 查询车型详情
     * <p>GET /vehicle/model/{id}
     *
     * @param id 车型 ID
     * @return VehicleModel 车型详情；不存在时 data 为 null
     */
    @GetMapping(value={"/{id}"})
    public Result<VehicleModel> getById(@PathVariable(value="id") Long id) {
        VehicleModel model = this.vehicleModelService.getById(id);
        return Result.success(model);
    }

    /**
     * 新增车型
     * <p>POST /vehicle/model
     *
     * @param dto 车型信息（modelCode、modelName、brand、manufacturer、vehicleType、
     *            enginePower、transmissionType、fuelType、emissionStandard、year、description）
     * @return 创建成功后的 VehicleModel（状态默认启用）
     */
    @PostMapping
    public Result<VehicleModel> create(@RequestBody VehicleModelDTO dto) {
        VehicleModel model = new VehicleModel();
        model.setModelCode(dto.getModelCode());
        model.setModelName(dto.getModelName());
        model.setBrand(dto.getBrand());
        model.setManufacturer(dto.getManufacturer());
        model.setVehicleType(dto.getVehicleType());
        model.setEnginePower(dto.getEnginePower());
        model.setTransmissionType(dto.getTransmissionType());
        model.setFuelType(dto.getFuelType());
        model.setEmissionStandard(dto.getEmissionStandard());
        model.setYear(dto.getYear());
        model.setDescription(dto.getDescription());
        model.setStatus(1);
        model.setDeleted(0);
        model.setCreateTime(LocalDateTime.now());
        model.setUpdateTime(LocalDateTime.now());
        this.vehicleModelService.save(model);
        return Result.success(model);
    }

    /**
     * 更新车型信息
     * <p>PUT /vehicle/model/{id}
     *
     * @param id  车型 ID
     * @param dto 待更新的车型字段
     * @return 更新后的 VehicleModel；车型不存在时返回错误
     */
    @PutMapping(value={"/{id}"})
    public Result<VehicleModel> update(@PathVariable(value="id") Long id, @RequestBody VehicleModelDTO dto) {
        VehicleModel model = this.vehicleModelService.getById(id);
        if (model == null) {
            return Result.error("\u8f66\u578b\u4e0d\u5b58\u5728");
        }
        model.setModelCode(dto.getModelCode());
        model.setModelName(dto.getModelName());
        model.setBrand(dto.getBrand());
        model.setManufacturer(dto.getManufacturer());
        model.setVehicleType(dto.getVehicleType());
        model.setEnginePower(dto.getEnginePower());
        model.setTransmissionType(dto.getTransmissionType());
        model.setFuelType(dto.getFuelType());
        model.setEmissionStandard(dto.getEmissionStandard());
        model.setYear(dto.getYear());
        model.setDescription(dto.getDescription());
        model.setUpdateTime(LocalDateTime.now());
        this.vehicleModelService.updateById(model);
        return Result.success(model);
    }

    /**
     * 删除车型（逻辑删除，置 deleted=1）
     * <p>DELETE /vehicle/model/{id}
     *
     * @param id 车型 ID
     * @return 空结果；车型不存在时静默成功
     */
    @DeleteMapping(value={"/{id}"})
    public Result<Void> delete(@PathVariable(value="id") Long id) {
        VehicleModel model = this.vehicleModelService.getById(id);
        if (model != null) {
            model.setDeleted(1);
            model.setUpdateTime(LocalDateTime.now());
            this.vehicleModelService.updateById(model);
        }
        return Result.success();
    }
}

