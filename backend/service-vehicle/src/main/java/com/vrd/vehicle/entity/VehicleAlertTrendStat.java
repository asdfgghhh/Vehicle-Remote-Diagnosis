package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vehicle_alert_trend_stat")
public class VehicleAlertTrendStat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDateTime statTime;

    /** hour / day / week / month */
    private String statGranularity;

    private Integer faultCount;

    private Integer faultVehicleCount;

    private LocalDateTime createTime;
}
