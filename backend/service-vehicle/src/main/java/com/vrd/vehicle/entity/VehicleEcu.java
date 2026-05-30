package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vehicle_ecu")
public class VehicleEcu {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vehicleId;

    private String ecuType;

    private String ecuPartNumber;

    private String hardwareVersion;

    private String softwareVersion;

    private String supplier;

    private String serialNumber;

    private LocalDateTime installDate;

    private Integer status;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
