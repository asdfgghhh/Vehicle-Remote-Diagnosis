package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vehicle_online_stat")
public class VehicleOnlineStat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDateTime statTime;

    /** hour / day */
    private String statGranularity;

    private Integer onlineCount;

    private LocalDateTime createTime;
}
