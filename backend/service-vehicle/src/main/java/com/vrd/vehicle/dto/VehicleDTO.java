package com.vrd.vehicle.dto;

import lombok.Data;

@Data
public class VehicleDTO {
    private String vin;
    private Long modelId;
    private String plateNumber;
    private String color;
    private Integer productionYear;
    private String engineNumber;
    private String bodyNumber;
    private String configWord;
    private String currentEcuVersion;
}
