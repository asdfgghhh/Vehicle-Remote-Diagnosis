package com.vrd.vehicle.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class VehicleModelDTO {
    private String modelCode;
    private String modelName;
    private String brand;
    private String manufacturer;
    private String vehicleType;
    private BigDecimal enginePower;
    private String transmissionType;
    private String fuelType;
    private String emissionStandard;
    private Integer year;
    private String description;
}
