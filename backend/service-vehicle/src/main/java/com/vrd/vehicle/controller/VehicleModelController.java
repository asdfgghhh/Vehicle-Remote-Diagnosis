package com.vrd.vehicle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.vehicle.dto.VehicleModelDTO;
import com.vrd.vehicle.entity.VehicleModel;
import com.vrd.vehicle.service.VehicleModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/vehicle/model")
public class VehicleModelController {

    @Autowired
    private VehicleModelService vehicleModelService;

    @GetMapping("/page")
    public Result<Page<VehicleModel>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<VehicleModel> page = vehicleModelService.page(current, size, keyword);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<VehicleModel> getById(@PathVariable Long id) {
        VehicleModel model = vehicleModelService.getById(id);
        return Result.success(model);
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
        
        vehicleModelService.save(model);
        return Result.success(model);
    }

    @PutMapping("/{id}")
    public Result<VehicleModel> update(@PathVariable Long id, @RequestBody VehicleModelDTO dto) {
        VehicleModel model = vehicleModelService.getById(id);
        if (model == null) {
            return Result.error("车型不存在");
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
        
        vehicleModelService.updateById(model);
        return Result.success(model);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        VehicleModel model = vehicleModelService.getById(id);
        if (model != null) {
            model.setDeleted(1);
            model.setUpdateTime(LocalDateTime.now());
            vehicleModelService.updateById(model);
        }
        return Result.success();
    }
}
