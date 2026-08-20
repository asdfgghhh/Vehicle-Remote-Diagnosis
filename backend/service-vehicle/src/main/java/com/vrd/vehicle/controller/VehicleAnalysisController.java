package com.vrd.vehicle.controller;

import com.vrd.common.result.Result;
import com.vrd.vehicle.dto.FaultAnalysisDistributionVO;
import com.vrd.vehicle.dto.RiskVehicleVO;
import com.vrd.vehicle.dto.VehicleAlertLongTrendVO;
import com.vrd.vehicle.service.VehicleAnalysisService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/vehicle"})
public class VehicleAnalysisController {
    @Autowired
    private VehicleAnalysisService vehicleAnalysisService;

    @GetMapping(value={"/fault-analysis/trend"})
    public Result<VehicleAlertLongTrendVO> faultTrend(@RequestParam(value="granularity", defaultValue="day") String granularity) {
        return Result.success(this.vehicleAnalysisService.getFaultTrend(granularity));
    }

    @GetMapping(value={"/fault-analysis/distribution"})
    public Result<FaultAnalysisDistributionVO> faultDistribution() {
        return Result.success(this.vehicleAnalysisService.getFaultDistribution());
    }

    @GetMapping(value={"/risk/list"})
    public Result<List<RiskVehicleVO>> riskList() {
        return Result.success(this.vehicleAnalysisService.listRiskVehicles());
    }
}
