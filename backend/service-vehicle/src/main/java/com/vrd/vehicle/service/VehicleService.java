/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.extension.plugins.pagination.Page
 *  com.baomidou.mybatisplus.extension.service.IService
 */
package com.vrd.vehicle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.vehicle.dto.VehicleAlertLongTrendVO;
import com.vrd.vehicle.dto.VehicleDashboardStatsVO;
import com.vrd.vehicle.dto.VehicleOnlineTrendVO;
import com.vrd.vehicle.entity.Vehicle;
import com.vrd.vehicle.entity.VehicleEcu;
import java.util.List;

public interface VehicleService
extends IService<Vehicle> {
    public VehicleDashboardStatsVO getDashboardStats();

    public VehicleOnlineTrendVO getOnlineTrend(String var1);

    public VehicleAlertLongTrendVO getAlertLongTrend(String var1, String var2);

    public Page<Vehicle> page(Integer var1, Integer var2, String var3, Long var4);

    public Vehicle create(Vehicle var1);

    public Vehicle update(Vehicle var1);

    public void syncFromKafka();

    public void processKafkaMessage(String var1);

    public void syncFromApi(String var1);

    public List<VehicleEcu> getEcusByVehicleId(Long var1);

    public void addEcu(VehicleEcu var1);

    public void updateEcu(VehicleEcu var1);
}

