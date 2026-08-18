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
import java.time.LocalDateTime;

@TableName(value="vehicle_ecu")
public class VehicleEcu {
    @TableId(type=IdType.AUTO)
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

    public Long getId() {
        return this.id;
    }

    public Long getVehicleId() {
        return this.vehicleId;
    }

    public String getEcuType() {
        return this.ecuType;
    }

    public String getEcuPartNumber() {
        return this.ecuPartNumber;
    }

    public String getHardwareVersion() {
        return this.hardwareVersion;
    }

    public String getSoftwareVersion() {
        return this.softwareVersion;
    }

    public String getSupplier() {
        return this.supplier;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public LocalDateTime getInstallDate() {
        return this.installDate;
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

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setEcuType(String ecuType) {
        this.ecuType = ecuType;
    }

    public void setEcuPartNumber(String ecuPartNumber) {
        this.ecuPartNumber = ecuPartNumber;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setInstallDate(LocalDateTime installDate) {
        this.installDate = installDate;
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
        if (!(o instanceof VehicleEcu)) {
            return false;
        }
        VehicleEcu other = (VehicleEcu)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Long this$vehicleId = this.getVehicleId();
        Long other$vehicleId = other.getVehicleId();
        if (this$vehicleId == null ? other$vehicleId != null : !((Object)this$vehicleId).equals(other$vehicleId)) {
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
        String this$ecuType = this.getEcuType();
        String other$ecuType = other.getEcuType();
        if (this$ecuType == null ? other$ecuType != null : !this$ecuType.equals(other$ecuType)) {
            return false;
        }
        String this$ecuPartNumber = this.getEcuPartNumber();
        String other$ecuPartNumber = other.getEcuPartNumber();
        if (this$ecuPartNumber == null ? other$ecuPartNumber != null : !this$ecuPartNumber.equals(other$ecuPartNumber)) {
            return false;
        }
        String this$hardwareVersion = this.getHardwareVersion();
        String other$hardwareVersion = other.getHardwareVersion();
        if (this$hardwareVersion == null ? other$hardwareVersion != null : !this$hardwareVersion.equals(other$hardwareVersion)) {
            return false;
        }
        String this$softwareVersion = this.getSoftwareVersion();
        String other$softwareVersion = other.getSoftwareVersion();
        if (this$softwareVersion == null ? other$softwareVersion != null : !this$softwareVersion.equals(other$softwareVersion)) {
            return false;
        }
        String this$supplier = this.getSupplier();
        String other$supplier = other.getSupplier();
        if (this$supplier == null ? other$supplier != null : !this$supplier.equals(other$supplier)) {
            return false;
        }
        String this$serialNumber = this.getSerialNumber();
        String other$serialNumber = other.getSerialNumber();
        if (this$serialNumber == null ? other$serialNumber != null : !this$serialNumber.equals(other$serialNumber)) {
            return false;
        }
        LocalDateTime this$installDate = this.getInstallDate();
        LocalDateTime other$installDate = other.getInstallDate();
        if (this$installDate == null ? other$installDate != null : !((Object)this$installDate).equals(other$installDate)) {
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
        return other instanceof VehicleEcu;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $vehicleId = this.getVehicleId();
        result = result * 59 + ($vehicleId == null ? 43 : ((Object)$vehicleId).hashCode());
        Integer $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : ((Object)$status).hashCode());
        Integer $deleted = this.getDeleted();
        result = result * 59 + ($deleted == null ? 43 : ((Object)$deleted).hashCode());
        String $ecuType = this.getEcuType();
        result = result * 59 + ($ecuType == null ? 43 : $ecuType.hashCode());
        String $ecuPartNumber = this.getEcuPartNumber();
        result = result * 59 + ($ecuPartNumber == null ? 43 : $ecuPartNumber.hashCode());
        String $hardwareVersion = this.getHardwareVersion();
        result = result * 59 + ($hardwareVersion == null ? 43 : $hardwareVersion.hashCode());
        String $softwareVersion = this.getSoftwareVersion();
        result = result * 59 + ($softwareVersion == null ? 43 : $softwareVersion.hashCode());
        String $supplier = this.getSupplier();
        result = result * 59 + ($supplier == null ? 43 : $supplier.hashCode());
        String $serialNumber = this.getSerialNumber();
        result = result * 59 + ($serialNumber == null ? 43 : $serialNumber.hashCode());
        LocalDateTime $installDate = this.getInstallDate();
        result = result * 59 + ($installDate == null ? 43 : ((Object)$installDate).hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        LocalDateTime $updateTime = this.getUpdateTime();
        result = result * 59 + ($updateTime == null ? 43 : ((Object)$updateTime).hashCode());
        return result;
    }

    public String toString() {
        return "VehicleEcu(id=" + this.getId() + ", vehicleId=" + this.getVehicleId() + ", ecuType=" + this.getEcuType() + ", ecuPartNumber=" + this.getEcuPartNumber() + ", hardwareVersion=" + this.getHardwareVersion() + ", softwareVersion=" + this.getSoftwareVersion() + ", supplier=" + this.getSupplier() + ", serialNumber=" + this.getSerialNumber() + ", installDate=" + String.valueOf(this.getInstallDate()) + ", status=" + this.getStatus() + ", deleted=" + this.getDeleted() + ", createTime=" + String.valueOf(this.getCreateTime()) + ", updateTime=" + String.valueOf(this.getUpdateTime()) + ")";
    }
}

