package com.vrd.vehicle.service;

import com.vrd.vehicle.dto.FaultAnalysisDistributionVO;
import com.vrd.vehicle.dto.RiskVehicleVO;
import com.vrd.vehicle.dto.VehicleAlertLongTrendVO;
import java.util.List;

public interface VehicleAnalysisService {
    VehicleAlertLongTrendVO getFaultTrend(String granularity);

    FaultAnalysisDistributionVO getFaultDistribution();

    List<RiskVehicleVO> listRiskVehicles();
}
