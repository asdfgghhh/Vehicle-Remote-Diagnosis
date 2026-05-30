package com.vrd.signal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("vehicle_signal")
public class VehicleSignal {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String vin;

    private Long vehicleId;

    private String signalName;

    private String signalValue;

    private BigDecimal numericValue;

    private String unit;

    private Long timestamp;

    private LocalDateTime signalTime;

    private String messageName;

    private Integer messageId;

    private Integer deleted;

    private LocalDateTime createTime;
}
