/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.core.mapper.BaseMapper
 *  org.apache.ibatis.annotations.Mapper
 *  org.apache.ibatis.annotations.Select
 */
package com.vrd.vehicle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vrd.vehicle.entity.VehicleOnlineStat;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VehicleOnlineStatMapper
extends BaseMapper<VehicleOnlineStat> {
    @Select(value={"SELECT stat_time AS statTime, online_count AS onlineCount FROM vehicle_online_stat WHERE stat_granularity = 'hour' AND stat_time >= DATE_SUB(NOW(), INTERVAL 23 HOUR) ORDER BY stat_time ASC"})
    public List<Map<String, Object>> listHourlyTrend();

    @Select(value={"SELECT stat_time AS statTime, online_count AS onlineCount FROM vehicle_online_stat WHERE stat_granularity = 'day' AND stat_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) ORDER BY stat_time ASC"})
    public List<Map<String, Object>> listDailyTrend();
}

