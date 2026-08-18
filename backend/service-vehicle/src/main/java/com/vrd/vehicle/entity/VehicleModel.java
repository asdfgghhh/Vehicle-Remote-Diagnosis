/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.annotation.IdType
 *  com.baomidou.mybatisplus.annotation.TableId
 *  com.baomidou.mybatisplus.annotation.TableName
 */
package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName(value="vehicle_model")
public class VehicleModel {
    @TableId(type=IdType.AUTO)
    private Long id;
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
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return this.id;
    }

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

    public Integer getStatus() {
        return this.status;
    }

    public Integer getDeleted() {
        return this.deleted;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public LocalDateTime getUpdateTime() {
        return this.updateTime;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VehicleModel)) {
            return false;
        }
        VehicleModel other = (VehicleModel)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Integer this$year = this.getYear();
        Integer other$year = other.getYear();
        if (this$year == null ? other$year != null : !((Object)this$year).equals(other$year)) {
            return false;
        }
        Integer this$status = this.getStatus();
        Integer other$status = other.getStatus();
        if (this$status == null ? other$status != null : !((Object)this$status).equals(other$status)) {
            return false;
        }
        Integer this$deleted = this.getDeleted();
        Integer other$deleted = other.getDeleted();
        if (this$deleted == null ? other$deleted != null : !((Object)this$deleted).equals(other$deleted)) {
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
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) {
            return false;
        }
        LocalDateTime this$createTime = this.getCreateTime();
        LocalDateTime other$createTime = other.getCreateTime();
        if (this$createTime == null ? other$createTime != null : !((Object)this$createTime).equals(other$createTime)) {
            return false;
        }
        LocalDateTime this$updateTime = this.getUpdateTime();
        LocalDateTime other$updateTime = other.getUpdateTime();
        return !(this$updateTime == null ? other$updateTime != null : !((Object)this$updateTime).equals(other$updateTime));
    }

    protected boolean canEqual(Object other) {
        return other instanceof VehicleModel;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Integer $year = this.getYear();
        result = result * 59 + ($year == null ? 43 : ((Object)$year).hashCode());
        Integer $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : ((Object)$status).hashCode());
        Integer $deleted = this.getDeleted();
        result = result * 59 + ($deleted == null ? 43 : ((Object)$deleted).hashCode());
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
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        LocalDateTime $updateTime = this.getUpdateTime();
        result = result * 59 + ($updateTime == null ? 43 : ((Object)$updateTime).hashCode());
        return result;
    }

    public String toString() {
        return "VehicleModel(id=" + this.getId() + ", modelCode=" + this.getModelCode() + ", modelName=" + this.getModelName() + ", brand=" + this.getBrand() + ", manufacturer=" + this.getManufacturer() + ", vehicleType=" + this.getVehicleType() + ", enginePower=" + String.valueOf(this.getEnginePower()) + ", transmissionType=" + this.getTransmissionType() + ", fuelType=" + this.getFuelType() + ", emissionStandard=" + this.getEmissionStandard() + ", year=" + this.getYear() + ", description=" + this.getDescription() + ", status=" + this.getStatus() + ", deleted=" + this.getDeleted() + ", createTime=" + String.valueOf(this.getCreateTime()) + ", updateTime=" + String.valueOf(this.getUpdateTime()) + ")";
    }
}

