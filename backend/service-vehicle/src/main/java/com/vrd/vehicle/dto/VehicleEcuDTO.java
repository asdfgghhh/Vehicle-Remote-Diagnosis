package com.vrd.vehicle.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VehicleEcuDTO {
    private String ecuType;
    private String ecuPartNumber;
    private String hardwareVersion;
    private String softwareVersion;
    private String supplier;
    private String serialNumber;
    private LocalDate installDate;
}
