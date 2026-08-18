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

@RestController
@RequestMapping(value={"/vehicle/model"})
public class VehicleModelController {
    @Autowired
    private VehicleModelService vehicleModelService;

    @GetMapping(value={"/page"})
    public Result<Page<VehicleModel>> page(@RequestParam(value="current", defaultValue="1") Integer current, @RequestParam(value="size", defaultValue="10") Integer size, @RequestParam(value="keyword", required=false) String keyword) {
        Page<VehicleModel> page = this.vehicleModelService.page(current, size, keyword);
        return Result.success(page);
    }

    @GetMapping(value={"/{id}"})
    public Result<VehicleModel> getById(@PathVariable(value="id") Long id) {
        VehicleModel model = (VehicleModel)this.vehicleModelService.getById(id);
        return Result.success((Object)model);
    }

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
        return Result.success((Object)model);
    }

    @PutMapping(value={"/{id}"})
    public Result<VehicleModel> update(@PathVariable(value="id") Long id, @RequestBody VehicleModelDTO dto) {
        VehicleModel model = (VehicleModel)this.vehicleModelService.getById(id);
        if (model == null) {
            return Result.error((String)"\u8f66\u578b\u4e0d\u5b58\u5728");
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
        return Result.success((Object)model);
    }

    @DeleteMapping(value={"/{id}"})
    public Result<Void> delete(@PathVariable(value="id") Long id) {
        VehicleModel model = (VehicleModel)this.vehicleModelService.getById(id);
        if (model != null) {
            model.setDeleted(1);
            model.setUpdateTime(LocalDateTime.now());
            this.vehicleModelService.updateById(model);
        }
        return Result.success();
    }
}

