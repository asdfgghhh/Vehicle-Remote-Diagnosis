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
import com.vrd.vehicle.entity.VehicleAlert;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VehicleAlertMapper
extends BaseMapper<VehicleAlert> {
    @Select(value={"SELECT COALESCE(NULLIF(component_code, ''), ecu_type) AS componentCode, COUNT(*) AS alertCount FROM vehicle_alert WHERE deleted = 0 GROUP BY COALESCE(NULLIF(component_code, ''), ecu_type) ORDER BY alertCount DESC"})
    public List<Map<String, Object>> countGroupByComponent();
}

