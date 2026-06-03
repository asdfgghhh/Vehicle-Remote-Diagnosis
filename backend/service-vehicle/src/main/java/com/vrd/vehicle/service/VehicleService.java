package com.vrd.vehicle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.vehicle.dto.VehicleAlertLongTrendVO;
import com.vrd.vehicle.dto.VehicleDashboardStatsVO;
import com.vrd.vehicle.dto.VehicleOnlineTrendVO;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleEcu;

import java.util.List;

public interface VehicleService extends IService<Vehicle> {
    VehicleDashboardStatsVO getDashboardStats();

    VehicleOnlineTrendVO getOnlineTrend(String granularity);

    VehicleAlertLongTrendVO getAlertLongTrend(String granularity, String metric);

    Page<Vehicle> page(Integer current, Integer size, String keyword, Long modelId);
    
    Vehicle create(Vehicle vehicle);
    
    Vehicle update(Vehicle vehicle);
    
    void syncFromKafka();
    
    void syncFromApi(String apiUrl);

    List<VehicleEcu> getEcusByVehicleId(Long vehicleId);

    void addEcu(VehicleEcu ecu);

    void updateEcu(VehicleEcu ecu);
}
