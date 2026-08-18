/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.annotation.IdType
 *  com.baomidou.mybatisplus.annotation.TableId
 *  com.baomidou.mybatisplus.annotation.TableName
 */
package com.vrd.dbc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName(value="dispatch_log")
public class DispatchLog {
    @TableId(type=IdType.AUTO)
    private Long id;
    private Long dbcFileId;
    private Long vehicleId;
    private String vin;
    private String dispatchType;
    private Integer status;
    private String result;
    private LocalDateTime dispatchTime;
    private LocalDateTime createTime;

    public Long getId() {
        return this.id;
    }

    public Long getDbcFileId() {
        return this.dbcFileId;
    }

    public Long getVehicleId() {
        return this.vehicleId;
    }

    public String getVin() {
        return this.vin;
    }

    public String getDispatchType() {
        return this.dispatchType;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getResult() {
        return this.result;
    }

    public LocalDateTime getDispatchTime() {
        return this.dispatchTime;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDbcFileId(Long dbcFileId) {
        this.dbcFileId = dbcFileId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public void setDispatchType(String dispatchType) {
        this.dispatchType = dispatchType;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setDispatchTime(LocalDateTime dispatchTime) {
        this.dispatchTime = dispatchTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DispatchLog)) {
            return false;
        }
        DispatchLog other = (DispatchLog)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Long this$dbcFileId = this.getDbcFileId();
        Long other$dbcFileId = other.getDbcFileId();
        if (this$dbcFileId == null ? other$dbcFileId != null : !((Object)this$dbcFileId).equals(other$dbcFileId)) {
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
        String this$vin = this.getVin();
        String other$vin = other.getVin();
        if (this$vin == null ? other$vin != null : !this$vin.equals(other$vin)) {
            return false;
        }
        String this$dispatchType = this.getDispatchType();
        String other$dispatchType = other.getDispatchType();
        if (this$dispatchType == null ? other$dispatchType != null : !this$dispatchType.equals(other$dispatchType)) {
            return false;
        }
        String this$result = this.getResult();
        String other$result = other.getResult();
        if (this$result == null ? other$result != null : !this$result.equals(other$result)) {
            return false;
        }
        LocalDateTime this$dispatchTime = this.getDispatchTime();
        LocalDateTime other$dispatchTime = other.getDispatchTime();
        if (this$dispatchTime == null ? other$dispatchTime != null : !((Object)this$dispatchTime).equals(other$dispatchTime)) {
            return false;
        }
        LocalDateTime this$createTime = this.getCreateTime();
        LocalDateTime other$createTime = other.getCreateTime();
        return !(this$createTime == null ? other$createTime != null : !((Object)this$createTime).equals(other$createTime));
    }

    protected boolean canEqual(Object other) {
        return other instanceof DispatchLog;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $dbcFileId = this.getDbcFileId();
        result = result * 59 + ($dbcFileId == null ? 43 : ((Object)$dbcFileId).hashCode());
        Long $vehicleId = this.getVehicleId();
        result = result * 59 + ($vehicleId == null ? 43 : ((Object)$vehicleId).hashCode());
        Integer $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : ((Object)$status).hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $dispatchType = this.getDispatchType();
        result = result * 59 + ($dispatchType == null ? 43 : $dispatchType.hashCode());
        String $result = this.getResult();
        result = result * 59 + ($result == null ? 43 : $result.hashCode());
        LocalDateTime $dispatchTime = this.getDispatchTime();
        result = result * 59 + ($dispatchTime == null ? 43 : ((Object)$dispatchTime).hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        return result;
    }

    public String toString() {
        return "DispatchLog(id=" + this.getId() + ", dbcFileId=" + this.getDbcFileId() + ", vehicleId=" + this.getVehicleId() + ", vin=" + this.getVin() + ", dispatchType=" + this.getDispatchType() + ", status=" + this.getStatus() + ", result=" + this.getResult() + ", dispatchTime=" + String.valueOf(this.getDispatchTime()) + ", createTime=" + String.valueOf(this.getCreateTime()) + ")";
    }
}

