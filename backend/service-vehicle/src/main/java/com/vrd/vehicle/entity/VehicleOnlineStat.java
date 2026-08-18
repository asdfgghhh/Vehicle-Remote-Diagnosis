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

@TableName(value="vehicle_online_stat")
public class VehicleOnlineStat {
    @TableId(type=IdType.AUTO)
    private Long id;
    private LocalDateTime statTime;
    private String statGranularity;
    private Integer onlineCount;
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

    public Integer getOnlineCount() {
        return this.onlineCount;
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

    public void setOnlineCount(Integer onlineCount) {
        this.onlineCount = onlineCount;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VehicleOnlineStat)) {
            return false;
        }
        VehicleOnlineStat other = (VehicleOnlineStat)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Integer this$onlineCount = this.getOnlineCount();
        Integer other$onlineCount = other.getOnlineCount();
        if (this$onlineCount == null ? other$onlineCount != null : !((Object)this$onlineCount).equals(other$onlineCount)) {
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
        return other instanceof VehicleOnlineStat;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Integer $onlineCount = this.getOnlineCount();
        result = result * 59 + ($onlineCount == null ? 43 : ((Object)$onlineCount).hashCode());
        LocalDateTime $statTime = this.getStatTime();
        result = result * 59 + ($statTime == null ? 43 : ((Object)$statTime).hashCode());
        String $statGranularity = this.getStatGranularity();
        result = result * 59 + ($statGranularity == null ? 43 : $statGranularity.hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        return result;
    }

    public String toString() {
        return "VehicleOnlineStat(id=" + this.getId() + ", statTime=" + String.valueOf(this.getStatTime()) + ", statGranularity=" + this.getStatGranularity() + ", onlineCount=" + this.getOnlineCount() + ", createTime=" + String.valueOf(this.getCreateTime()) + ")";
    }
}

