package com.vrd.vehicle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vrd.common.result.Result;
import com.vrd.vehicle.dto.VehicleDTO;
import com.vrd.vehicle.dto.VehicleAlertLongTrendVO;
import com.vrd.vehicle.dto.VehicleDashboardStatsVO;
import com.vrd.vehicle.dto.VehicleOnlineTrendVO;
import com.vrd.vehicle.dto.VehicleEcuDTO;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleEcu;
import com.vrd.vehicle.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicle")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/stats")
    public Result<VehicleDashboardStatsVO> stats() {
        return Result.success(vehicleService.getDashboardStats());
    }

    @GetMapping("/stats/online-trend")
    public Result<VehicleOnlineTrendVO> onlineTrend(
            @RequestParam(value = "granularity", defaultValue = "hour") String granularity) {
        return Result.success(vehicleService.getOnlineTrend(granularity));
    }

    @GetMapping("/stats/alert-long-trend")
    public Result<VehicleAlertLongTrendVO> alertLongTrend(
            @RequestParam(value = "granularity", defaultValue = "hour") String granularity,
            @RequestParam(value = "metric", defaultValue = "faultCount") String metric) {
        return Result.success(vehicleService.getAlertLongTrend(granularity, metric));
    }

    @GetMapping("/page")
    public Result<Page<Vehicle>> page(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "modelId", required = false) Long modelId) {
        Page<Vehicle> page = vehicleService.page(current, size, keyword, modelId);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Vehicle> getById(@PathVariable("id") Long id) {
        Vehicle vehicle = vehicleService.getById(id);
        return Result.success(vehicle);
    }

    @GetMapping("/vin/{vin}")
    public Result<Vehicle> getByVin(@PathVariable("vin") String vin) {
        Vehicle vehicle = vehicleService.lambdaQuery()
                .eq(Vehicle::getVin, vin)
                .one();
        return Result.success(vehicle);
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
        
        Vehicle result = vehicleService.create(vehicle);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    public Result<Vehicle> update(@PathVariable("id") Long id, @RequestBody VehicleDTO dto) {
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
        
        Vehicle result = vehicleService.update(vehicle);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        Vehicle vehicle = vehicleService.getById(id);
        if (vehicle != null) {
            vehicle.setDeleted(1);
            vehicleService.updateById(vehicle);
        }
        return Result.success();
    }

    @PostMapping("/sync/kafka")
    public Result<Void> syncFromKafka() {
        vehicleService.syncFromKafka();
        return Result.success();
    }

    @PostMapping("/sync/api")
    public Result<Void> syncFromApi(@RequestParam(value = "apiUrl") String apiUrl) {
        vehicleService.syncFromApi(apiUrl);
        return Result.success();
    }

    @GetMapping("/{id}/ecu")
    public Result<List<VehicleEcu>> getEcus(@PathVariable("id") Long id) {
        List<VehicleEcu> ecus = vehicleService.getEcusByVehicleId(id);
        return Result.success(ecus);
    }

    @PostMapping("/{id}/ecu")
    public Result<Void> addEcu(@PathVariable("id") Long id, @RequestBody VehicleEcuDTO dto) {
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
        
        vehicleService.addEcu(ecu);
        return Result.success();
    }

    @PutMapping("/ecu/{ecuId}")
    public Result<Void> updateEcu(@PathVariable("ecuId") Long ecuId, @RequestBody VehicleEcuDTO dto) {
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
        
        vehicleService.updateEcu(ecu);
        return Result.success();
    }
}
