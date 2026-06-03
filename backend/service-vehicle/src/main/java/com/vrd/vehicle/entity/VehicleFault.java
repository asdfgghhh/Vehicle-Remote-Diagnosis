package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vehicle_fault")
public class VehicleFault {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String vin;

    private Long vehicleId;

    private String faultCode;

    private String faultName;

    private String componentCode;

    private String ecuType;

    /** 0-未处理 1-已处理 */
    private Integer status;

    private LocalDateTime faultTime;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
