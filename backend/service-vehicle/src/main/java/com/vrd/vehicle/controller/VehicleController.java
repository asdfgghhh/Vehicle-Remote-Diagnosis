/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper
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

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.vehicle.dto.VehicleAlertLongTrendVO;
import com.vrd.vehicle.dto.VehicleDTO;
import com.vrd.vehicle.dto.VehicleDashboardStatsVO;
import com.vrd.vehicle.dto.VehicleEcuDTO;
import com.vrd.vehicle.dto.VehicleOnlineTrendVO;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleEcu;
import com.vrd.vehicle.service.VehicleService;
import java.util.List;
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
@RequestMapping(value={"/vehicle"})
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;

    @GetMapping(value={"/stats"})
    public Result<VehicleDashboardStatsVO> stats() {
        return Result.success((Object)this.vehicleService.getDashboardStats());
    }

    @GetMapping(value={"/stats/online-trend"})
    public Result<VehicleOnlineTrendVO> onlineTrend(@RequestParam(value="granularity", defaultValue="hour") String granularity) {
        return Result.success((Object)this.vehicleService.getOnlineTrend(granularity));
    }

    @GetMapping(value={"/stats/alert-long-trend"})
    public Result<VehicleAlertLongTrendVO> alertLongTrend(@RequestParam(value="granularity", defaultValue="hour") String granularity, @RequestParam(value="metric", defaultValue="faultCount") String metric) {
        return Result.success((Object)this.vehicleService.getAlertLongTrend(granularity, metric));
    }

    @GetMapping(value={"/page"})
    public Result<Page<Vehicle>> page(@RequestParam(value="current", defaultValue="1") Integer current, @RequestParam(value="size", defaultValue="10") Integer size, @RequestParam(value="keyword", required=false) String keyword, @RequestParam(value="modelId", required=false) Long modelId) {
        Page<Vehicle> page = this.vehicleService.page(current, size, keyword, modelId);
        return Result.success(page);
    }

    @GetMapping(value={"/{id}"})
    public Result<Vehicle> getById(@PathVariable(value="id") Long id) {
        Vehicle vehicle = (Vehicle)this.vehicleService.getById(id);
        return Result.success((Object)vehicle);
    }

    @GetMapping(value={"/vin/{vin}"})
    public Result<Vehicle> getByVin(@PathVariable(value="vin") String vin) {
        Vehicle vehicle = (Vehicle)((LambdaQueryChainWrapper)this.vehicleService.lambdaQuery().eq(Vehicle::getVin, (Object)vin)).one();
        return Result.success((Object)vehicle);
    }

    @PostMapping
    public Result<Vehicle> create(@RequestBody VehicleDTO dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVin(dto.getVin());
        vehicle.setModelId(dto.getModelId());
        vehicle.setPlateNumber(dto.getPlateNumber());
        vehicle.setColor(dto.getColor());
        vehicle.setProductionYear(dto.getProductionYear());
        vehicle.setEngineNumber(dto.getEngineNumber());
        vehicle.setBodyNumber(dto.getBodyNumber());
        vehicle.setConfigWord(dto.getConfigWord());
        vehicle.setCurrentEcuVersion(dto.getCurrentEcuVersion());
        Vehicle result = this.vehicleService.create(vehicle);
        return Result.success((Object)result);
    }

    @PutMapping(value={"/{id}"})
    public Result<Vehicle> update(@PathVariable(value="id") Long id, @RequestBody VehicleDTO dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setVin(dto.getVin());
        vehicle.setModelId(dto.getModelId());
        vehicle.setPlateNumber(dto.getPlateNumber());
        vehicle.setColor(dto.getColor());
        vehicle.setProductionYear(dto.getProductionYear());
        vehicle.setEngineNumber(dto.getEngineNumber());
        vehicle.setBodyNumber(dto.getBodyNumber());
        vehicle.setConfigWord(dto.getConfigWord());
        vehicle.setCurrentEcuVersion(dto.getCurrentEcuVersion());
        Vehicle result = this.vehicleService.update(vehicle);
        return Result.success((Object)result);
    }

    @DeleteMapping(value={"/{id}"})
    public Result<Void> delete(@PathVariable(value="id") Long id) {
        Vehicle vehicle = (Vehicle)this.vehicleService.getById(id);
        if (vehicle != null) {
            vehicle.setDeleted(1);
            this.vehicleService.updateById(vehicle);
        }
        return Result.success();
    }

    @PostMapping(value={"/sync/kafka"})
    public Result<Void> syncFromKafka() {
        this.vehicleService.syncFromKafka();
        return Result.success();
    }

    @PostMapping(value={"/sync/api"})
    public Result<Void> syncFromApi(@RequestParam(value="apiUrl") String apiUrl) {
        this.vehicleService.syncFromApi(apiUrl);
        return Result.success();
    }

    @GetMapping(value={"/{id}/ecu"})
    public Result<List<VehicleEcu>> getEcus(@PathVariable(value="id") Long id) {
        List<VehicleEcu> ecus = this.vehicleService.getEcusByVehicleId(id);
        return Result.success(ecus);
    }

    @PostMapping(value={"/{id}/ecu"})
    public Result<Void> addEcu(@PathVariable(value="id") Long id, @RequestBody VehicleEcuDTO dto) {
        VehicleEcu ecu = new VehicleEcu();
        ecu.setVehicleId(id);
        ecu.setEcuType(dto.getEcuType());
        ecu.setEcuPartNumber(dto.getEcuPartNumber());
        ecu.setHardwareVersion(dto.getHardwareVersion());
        ecu.setSoftwareVersion(dto.getSoftwareVersion());
        ecu.setSupplier(dto.getSupplier());
        ecu.setSerialNumber(dto.getSerialNumber());
        if (dto.getInstallDate() != null) {
            ecu.setInstallDate(dto.getInstallDate().atStartOfDay());
        }
        ecu.setStatus(1);
        this.vehicleService.addEcu(ecu);
        return Result.success();
    }

    @PutMapping(value={"/ecu/{ecuId}"})
    public Result<Void> updateEcu(@PathVariable(value="ecuId") Long ecuId, @RequestBody VehicleEcuDTO dto) {
        VehicleEcu ecu = new VehicleEcu();
        ecu.setId(ecuId);
        ecu.setEcuType(dto.getEcuType());
        ecu.setEcuPartNumber(dto.getEcuPartNumber());
        ecu.setHardwareVersion(dto.getHardwareVersion());
        ecu.setSoftwareVersion(dto.getSoftwareVersion());
        ecu.setSupplier(dto.getSupplier());
        ecu.setSerialNumber(dto.getSerialNumber());
        if (dto.getInstallDate() != null) {
            ecu.setInstallDate(dto.getInstallDate().atStartOfDay());
        }
        this.vehicleService.updateEcu(ecu);
        return Result.success();
    }
}

