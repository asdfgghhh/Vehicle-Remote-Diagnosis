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

@TableName(value="vehicle_alert_trend_stat")
public class VehicleAlertTrendStat {
    @TableId(type=IdType.AUTO)
    private Long id;
    private LocalDateTime statTime;
    private String statGranularity;
    private Integer faultCount;
    private Integer faultVehicleCount;
    private LocalDateTime createTime;

    public Long getId() {
        return this.id;
    }

    public LocalDateTime getStatTime() {
        return this.statTime;
    }

    public String getStatGranularity() {
        return this.statGranularity;
    }

    public Integer getFaultCount() {
        return this.faultCount;
    }

    public Integer getFaultVehicleCount() {
        return this.faultVehicleCount;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatTime(LocalDateTime statTime) {
        this.statTime = statTime;
    }

    public void setStatGranularity(String statGranularity) {
        this.statGranularity = statGranularity;
    }

    public void setFaultCount(Integer faultCount) {
        this.faultCount = faultCount;
    }

    public void setFaultVehicleCount(Integer faultVehicleCount) {
        this.faultVehicleCount = faultVehicleCount;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VehicleAlertTrendStat)) {
            return false;
        }
        VehicleAlertTrendStat other = (VehicleAlertTrendStat)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Integer this$faultCount = this.getFaultCount();
        Integer other$faultCount = other.getFaultCount();
        if (this$faultCount == null ? other$faultCount != null : !((Object)this$faultCount).equals(other$faultCount)) {
            return false;
        }
        Integer this$faultVehicleCount = this.getFaultVehicleCount();
        Integer other$faultVehicleCount = other.getFaultVehicleCount();
        if (this$faultVehicleCount == null ? other$faultVehicleCount != null : !((Object)this$faultVehicleCount).equals(other$faultVehicleCount)) {
            return false;
        }
        LocalDateTime this$statTime = this.getStatTime();
        LocalDateTime other$statTime = other.getStatTime();
        if (this$statTime == null ? other$statTime != null : !((Object)this$statTime).equals(other$statTime)) {
            return false;
        }
        String this$statGranularity = this.getStatGranularity();
        String other$statGranularity = other.getStatGranularity();
        if (this$statGranularity == null ? other$statGranularity != null : !this$statGranularity.equals(other$statGranularity)) {
            return false;
        }
        LocalDateTime this$createTime = this.getCreateTime();
        LocalDateTime other$createTime = other.getCreateTime();
        return !(this$createTime == null ? other$createTime != null : !((Object)this$createTime).equals(other$createTime));
    }

    protected boolean canEqual(Object other) {
        return other instanceof VehicleAlertTrendStat;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Integer $faultCount = this.getFaultCount();
        result = result * 59 + ($faultCount == null ? 43 : ((Object)$faultCount).hashCode());
        Integer $faultVehicleCount = this.getFaultVehicleCount();
        result = result * 59 + ($faultVehicleCount == null ? 43 : ((Object)$faultVehicleCount).hashCode());
        LocalDateTime $statTime = this.getStatTime();
        result = result * 59 + ($statTime == null ? 43 : ((Object)$statTime).hashCode());
        String $statGranularity = this.getStatGranularity();
        result = result * 59 + ($statGranularity == null ? 43 : $statGranularity.hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        return result;
    }

    public String toString() {
        return "VehicleAlertTrendStat(id=" + this.getId() + ", statTime=" + String.valueOf(this.getStatTime()) + ", statGranularity=" + this.getStatGranularity() + ", faultCount=" + this.getFaultCount() + ", faultVehicleCount=" + this.getFaultVehicleCount() + ", createTime=" + String.valueOf(this.getCreateTime()) + ")";
    }
}

