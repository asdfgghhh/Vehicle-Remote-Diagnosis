package com.vrd.vehicle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vrd.vehicle.entity.VehicleAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VehicleAlertMapper extends BaseMapper<VehicleAlert> {

    @Select("SELECT COALESCE(NULLIF(component_code, ''), ecu_type) AS componentCode, COUNT(*) AS alertCount " +
            "FROM vehicle_alert WHERE deleted = 0 " +
            "GROUP BY COALESCE(NULLIF(component_code, ''), ecu_type) ORDER BY alertCount DESC")
    List<Map<String, Object>> countGroupByComponent();
}
