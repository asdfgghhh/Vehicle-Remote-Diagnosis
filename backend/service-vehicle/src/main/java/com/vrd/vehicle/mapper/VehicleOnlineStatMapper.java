package com.vrd.vehicle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vrd.vehicle.entity.VehicleOnlineStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VehicleOnlineStatMapper extends BaseMapper<VehicleOnlineStat> {

    @Select("SELECT stat_time AS statTime, online_count AS onlineCount FROM vehicle_online_stat " +
            "WHERE stat_granularity = 'hour' AND stat_time >= DATE_SUB(NOW(), INTERVAL 23 HOUR) " +
            "ORDER BY stat_time ASC")
    List<Map<String, Object>> listHourlyTrend();

    @Select("SELECT stat_time AS statTime, online_count AS onlineCount FROM vehicle_online_stat " +
            "WHERE stat_granularity = 'day' AND stat_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "ORDER BY stat_time ASC")
    List<Map<String, Object>> listDailyTrend();
}
