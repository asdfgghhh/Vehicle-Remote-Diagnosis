package com.vrd.access.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VehicleSignal {

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
    private LocalDateTime createTime;
}
