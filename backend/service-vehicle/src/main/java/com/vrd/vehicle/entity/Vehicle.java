package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vehicle")
public class Vehicle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String vin;

    private Long modelId;

    private String plateNumber;

    private String color;

    private Integer productionYear;

    private String engineNumber;

    private String bodyNumber;

    private String configWord;

    private Integer status;

    private String currentEcuVersion;

    private Integer dataSource;

    private String externalId;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
