/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.vehicle.dto;

import java.math.BigDecimal;

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

    public String getModelCode() {
        return this.modelCode;
    }

    public String getModelName() {
        return this.modelName;
    }

    public String getBrand() {
        return this.brand;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public String getVehicleType() {
        return this.vehicleType;
    }

    public BigDecimal getEnginePower() {
        return this.enginePower;
    }

    public String getTransmissionType() {
        return this.transmissionType;
    }

    public String getFuelType() {
        return this.fuelType;
    }

    public String getEmissionStandard() {
        return this.emissionStandard;
    }

    public Integer getYear() {
        return this.year;
    }

    public String getDescription() {
        return this.description;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setEnginePower(BigDecimal enginePower) {
        this.enginePower = enginePower;
    }

    public void setTransmissionType(String transmissionType) {
        this.transmissionType = transmissionType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public void setEmissionStandard(String emissionStandard) {
        this.emissionStandard = emissionStandard;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VehicleModelDTO)) {
            return false;
        }
        VehicleModelDTO other = (VehicleModelDTO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Integer this$year = this.getYear();
        Integer other$year = other.getYear();
        if (this$year == null ? other$year != null : !((Object)this$year).equals(other$year)) {
            return false;
        }
        String this$modelCode = this.getModelCode();
        String other$modelCode = other.getModelCode();
        if (this$modelCode == null ? other$modelCode != null : !this$modelCode.equals(other$modelCode)) {
            return false;
        }
        String this$modelName = this.getModelName();
        String other$modelName = other.getModelName();
        if (this$modelName == null ? other$modelName != null : !this$modelName.equals(other$modelName)) {
            return false;
        }
        String this$brand = this.getBrand();
        String other$brand = other.getBrand();
        if (this$brand == null ? other$brand != null : !this$brand.equals(other$brand)) {
            return false;
        }
        String this$manufacturer = this.getManufacturer();
        String other$manufacturer = other.getManufacturer();
        if (this$manufacturer == null ? other$manufacturer != null : !this$manufacturer.equals(other$manufacturer)) {
            return false;
        }
        String this$vehicleType = this.getVehicleType();
        String other$vehicleType = other.getVehicleType();
        if (this$vehicleType == null ? other$vehicleType != null : !this$vehicleType.equals(other$vehicleType)) {
            return false;
        }
        BigDecimal this$enginePower = this.getEnginePower();
        BigDecimal other$enginePower = other.getEnginePower();
        if (this$enginePower == null ? other$enginePower != null : !((Object)this$enginePower).equals(other$enginePower)) {
            return false;
        }
        String this$transmissionType = this.getTransmissionType();
        String other$transmissionType = other.getTransmissionType();
        if (this$transmissionType == null ? other$transmissionType != null : !this$transmissionType.equals(other$transmissionType)) {
            return false;
        }
        String this$fuelType = this.getFuelType();
        String other$fuelType = other.getFuelType();
        if (this$fuelType == null ? other$fuelType != null : !this$fuelType.equals(other$fuelType)) {
            return false;
        }
        String this$emissionStandard = this.getEmissionStandard();
        String other$emissionStandard = other.getEmissionStandard();
        if (this$emissionStandard == null ? other$emissionStandard != null : !this$emissionStandard.equals(other$emissionStandard)) {
            return false;
        }
        String this$description = this.getDescription();
        String other$description = other.getDescription();
        return !(this$description == null ? other$description != null : !this$description.equals(other$description));
    }

    protected boolean canEqual(Object other) {
        return other instanceof VehicleModelDTO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Integer $year = this.getYear();
        result = result * 59 + ($year == null ? 43 : ((Object)$year).hashCode());
        String $modelCode = this.getModelCode();
        result = result * 59 + ($modelCode == null ? 43 : $modelCode.hashCode());
        String $modelName = this.getModelName();
        result = result * 59 + ($modelName == null ? 43 : $modelName.hashCode());
        String $brand = this.getBrand();
        result = result * 59 + ($brand == null ? 43 : $brand.hashCode());
        String $manufacturer = this.getManufacturer();
        result = result * 59 + ($manufacturer == null ? 43 : $manufacturer.hashCode());
        String $vehicleType = this.getVehicleType();
        result = result * 59 + ($vehicleType == null ? 43 : $vehicleType.hashCode());
        BigDecimal $enginePower = this.getEnginePower();
        result = result * 59 + ($enginePower == null ? 43 : ((Object)$enginePower).hashCode());
        String $transmissionType = this.getTransmissionType();
        result = result * 59 + ($transmissionType == null ? 43 : $transmissionType.hashCode());
        String $fuelType = this.getFuelType();
        result = result * 59 + ($fuelType == null ? 43 : $fuelType.hashCode());
        String $emissionStandard = this.getEmissionStandard();
        result = result * 59 + ($emissionStandard == null ? 43 : $emissionStandard.hashCode());
        String $description = this.getDescription();
        result = result * 59 + ($description == null ? 43 : $description.hashCode());
        return result;
    }

    public String toString() {
        return "VehicleModelDTO(modelCode=" + this.getModelCode() + ", modelName=" + this.getModelName() + ", brand=" + this.getBrand() + ", manufacturer=" + this.getManufacturer() + ", vehicleType=" + this.getVehicleType() + ", enginePower=" + String.valueOf(this.getEnginePower()) + ", transmissionType=" + this.getTransmissionType() + ", fuelType=" + this.getFuelType() + ", emissionStandard=" + this.getEmissionStandard() + ", year=" + this.getYear() + ", description=" + this.getDescription() + ")";
    }
}

