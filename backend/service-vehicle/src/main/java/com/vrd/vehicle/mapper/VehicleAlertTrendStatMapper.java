package com.vrd.vehicle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vrd.vehicle.entity.VehicleAlertTrendStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VehicleAlertTrendStatMapper extends BaseMapper<VehicleAlertTrendStat> {

    @Select("SELECT stat_time AS statTime, fault_count AS faultCount, fault_vehicle_count AS faultVehicleCount " +
            "FROM vehicle_alert_trend_stat WHERE stat_granularity = #{granularity} ORDER BY stat_time ASC")
    List<Map<String, Object>> listTrend(@Param("granularity") String granularity);
}
