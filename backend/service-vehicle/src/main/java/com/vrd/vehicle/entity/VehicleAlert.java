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

@TableName(value="vehicle_alert")
public class VehicleAlert {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String vin;
    private Long vehicleId;
    private String componentCode;
    private String ecuType;
    private String alertType;
    private String message;
    private Integer status;
    private LocalDateTime alertTime;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return this.id;
    }

    public String getVin() {
        return this.vin;
    }

    public Long getVehicleId() {
        return this.vehicleId;
    }

    public String getComponentCode() {
        return this.componentCode;
    }

    public String getEcuType() {
        return this.ecuType;
    }

    public String getAlertType() {
        return this.alertType;
    }

    public String getMessage() {
        return this.message;
    }

    public Integer getStatus() {
        return this.status;
    }

    public LocalDateTime getAlertTime() {
        return this.alertTime;
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

    public void setVin(String vin) {
        this.vin = vin;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public void setEcuType(String ecuType) {
        this.ecuType = ecuType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setAlertTime(LocalDateTime alertTime) {
        this.alertTime = alertTime;
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
        if (!(o instanceof VehicleAlert)) {
            return false;
        }
        VehicleAlert other = (VehicleAlert)o;
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
        String this$vin = this.getVin();
        String other$vin = other.getVin();
        if (this$vin == null ? other$vin != null : !this$vin.equals(other$vin)) {
            return false;
        }
        String this$componentCode = this.getComponentCode();
        String other$componentCode = other.getComponentCode();
        if (this$componentCode == null ? other$componentCode != null : !this$componentCode.equals(other$componentCode)) {
            return false;
        }
        String this$ecuType = this.getEcuType();
        String other$ecuType = other.getEcuType();
        if (this$ecuType == null ? other$ecuType != null : !this$ecuType.equals(other$ecuType)) {
            return false;
        }
        String this$alertType = this.getAlertType();
        String other$alertType = other.getAlertType();
        if (this$alertType == null ? other$alertType != null : !this$alertType.equals(other$alertType)) {
            return false;
        }
        String this$message = this.getMessage();
        String other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) {
            return false;
        }
        LocalDateTime this$alertTime = this.getAlertTime();
        LocalDateTime other$alertTime = other.getAlertTime();
        if (this$alertTime == null ? other$alertTime != null : !((Object)this$alertTime).equals(other$alertTime)) {
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
        return other instanceof VehicleAlert;
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
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $componentCode = this.getComponentCode();
        result = result * 59 + ($componentCode == null ? 43 : $componentCode.hashCode());
        String $ecuType = this.getEcuType();
        result = result * 59 + ($ecuType == null ? 43 : $ecuType.hashCode());
        String $alertType = this.getAlertType();
        result = result * 59 + ($alertType == null ? 43 : $alertType.hashCode());
        String $message = this.getMessage();
        result = result * 59 + ($message == null ? 43 : $message.hashCode());
        LocalDateTime $alertTime = this.getAlertTime();
        result = result * 59 + ($alertTime == null ? 43 : ((Object)$alertTime).hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        LocalDateTime $updateTime = this.getUpdateTime();
        result = result * 59 + ($updateTime == null ? 43 : ((Object)$updateTime).hashCode());
        return result;
    }

    public String toString() {
        return "VehicleAlert(id=" + this.getId() + ", vin=" + this.getVin() + ", vehicleId=" + this.getVehicleId() + ", componentCode=" + this.getComponentCode() + ", ecuType=" + this.getEcuType() + ", alertType=" + this.getAlertType() + ", message=" + this.getMessage() + ", status=" + this.getStatus() + ", alertTime=" + String.valueOf(this.getAlertTime()) + ", deleted=" + this.getDeleted() + ", createTime=" + String.valueOf(this.getCreateTime()) + ", updateTime=" + String.valueOf(this.getUpdateTime()) + ")";
    }
}

