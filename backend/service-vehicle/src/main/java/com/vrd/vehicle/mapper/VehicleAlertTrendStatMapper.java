/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.core.mapper.BaseMapper
 *  org.apache.ibatis.annotations.Mapper
 *  org.apache.ibatis.annotations.Param
 *  org.apache.ibatis.annotations.Select
 */
package com.vrd.vehicle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vrd.vehicle.entity.VehicleAlertTrendStat;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VehicleAlertTrendStatMapper
extends BaseMapper<VehicleAlertTrendStat> {
    @Select(value={"SELECT stat_time AS statTime, fault_count AS faultCount, fault_vehicle_count AS faultVehicleCount FROM vehicle_alert_trend_stat WHERE stat_granularity = #{granularity} ORDER BY stat_time ASC"})
    public List<Map<String, Object>> listTrend(@Param(value="granularity") String var1);
}

