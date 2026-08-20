package com.vrd.vehicle.controller;

import com.vrd.common.result.Result;
import com.vrd.vehicle.dto.VehicleDomainHealthVO;
import com.vrd.vehicle.dto.VehicleHealthDetailVO;
import com.vrd.vehicle.dto.VehicleHealthTrendVO;
import com.vrd.vehicle.service.VehicleHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/vehicle/health"})
public class VehicleHealthController {
    @Autowired
    private VehicleHealthService vehicleHealthService;

    @GetMapping(value={"/{vin}"})
    public Result<VehicleHealthDetailVO> healthDetail(@PathVariable(value="vin") String vin) {
        return Result.success(this.vehicleHealthService.getHealthDetail(vin));
    }

    @GetMapping(value={"/{vin}/trend"})
    public Result<VehicleHealthTrendVO> healthTrend(@PathVariable(value="vin") String vin, @RequestParam(value="days", required=false) Integer days) {
        return Result.success(this.vehicleHealthService.getHealthTrend(vin, days));
    }

    @GetMapping(value={"/domain/{vin}/{domainCode}"})
    public Result<VehicleDomainHealthVO> domainDetail(@PathVariable(value="vin") String vin, @PathVariable(value="domainCode") String domainCode) {
        return Result.success(this.vehicleHealthService.getDomainDetail(vin, domainCode));
    }
}
