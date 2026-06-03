package com.vrd.vehicle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vrd.vehicle.entity.VehicleFault;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VehicleFaultMapper extends BaseMapper<VehicleFault> {

    @Select("SELECT fault_code AS faultCode, MAX(fault_name) AS faultName, " +
            "MAX(COALESCE(NULLIF(component_code, ''), ecu_type)) AS componentCode, COUNT(*) AS faultCount " +
            "FROM vehicle_fault WHERE deleted = 0 GROUP BY fault_code ORDER BY faultCount DESC")
    List<Map<String, Object>> countGroupByFaultCode();
}
