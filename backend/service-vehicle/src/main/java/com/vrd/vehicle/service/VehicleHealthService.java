package com.vrd.vehicle.service;

import com.vrd.vehicle.dto.VehicleDomainHealthVO;
import com.vrd.vehicle.dto.VehicleHealthDetailVO;
import com.vrd.vehicle.dto.VehicleHealthTrendVO;

public interface VehicleHealthService {
    VehicleHealthDetailVO getHealthDetail(String vin);

    VehicleHealthTrendVO getHealthTrend(String vin, Integer days);

    VehicleDomainHealthVO getDomainDetail(String vin, String domainCode);
}
