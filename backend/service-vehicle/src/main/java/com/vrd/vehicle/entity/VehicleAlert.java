package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vehicle_alert")
public class VehicleAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String vin;

    private Long vehicleId;

    /** 部件英文简称，如 EMS、BCM、ABS */
    private String componentCode;

    private String ecuType;

    private String alertType;

    private String message;

    /** 0-未处理 1-已处理 */
    private Integer status;

    private LocalDateTime alertTime;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
