/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.annotation.IdType
 *  com.baomidou.mybatisplus.annotation.TableField
 *  com.baomidou.mybatisplus.annotation.TableId
 *  com.baomidou.mybatisplus.annotation.TableName
 */
package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName(value="fault_config")
public class FaultConfig {
    @TableId(type=IdType.AUTO)
    private Long id;
    private Long modelId;
    private String faultCode;
    private String dtc;
    private String alarmName;
    private String ecuType;
    private String componentCode;
    private Integer alarmLevel;
    private String description;
    private Integer status;
    private Long faultSceneId;
    private Integer aiBadge;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(exist=false)
    private String modelName;
    @TableField(exist=false)
    private String faultSceneName;

    public Long getId() {
        return this.id;
    }

    public Long getModelId() {
        return this.modelId;
    }

    public String getFaultCode() {
        return this.faultCode;
    }

    public String getDtc() {
        return this.dtc;
    }

    public String getAlarmName() {
        return this.alarmName;
    }

    public String getEcuType() {
        return this.ecuType;
    }

    public String getComponentCode() {
        return this.componentCode;
    }

    public Integer getAlarmLevel() {
        return this.alarmLevel;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getStatus() {
        return this.status;
    }

    public Long getFaultSceneId() {
        return this.faultSceneId;
    }

    public Integer getAiBadge() {
        return this.aiBadge;
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

    public String getModelName() {
        return this.modelName;
    }

    public String getFaultSceneName() {
        return this.faultSceneName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    public void setDtc(String dtc) {
        this.dtc = dtc;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public void setEcuType(String ecuType) {
        this.ecuType = ecuType;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public void setAlarmLevel(Integer alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setFaultSceneId(Long faultSceneId) {
        this.faultSceneId = faultSceneId;
    }

    public void setAiBadge(Integer aiBadge) {
        this.aiBadge = aiBadge;
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

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setFaultSceneName(String faultSceneName) {
        this.faultSceneName = faultSceneName;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof FaultConfig)) {
            return false;
        }
        FaultConfig other = (FaultConfig)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Long this$modelId = this.getModelId();
        Long other$modelId = other.getModelId();
        if (this$modelId == null ? other$modelId != null : !((Object)this$modelId).equals(other$modelId)) {
            return false;
        }
        Integer this$alarmLevel = this.getAlarmLevel();
        Integer other$alarmLevel = other.getAlarmLevel();
        if (this$alarmLevel == null ? other$alarmLevel != null : !((Object)this$alarmLevel).equals(other$alarmLevel)) {
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
        String this$faultCode = this.getFaultCode();
        String other$faultCode = other.getFaultCode();
        if (this$faultCode == null ? other$faultCode != null : !this$faultCode.equals(other$faultCode)) {
            return false;
        }
        String this$dtc = this.getDtc();
        String other$dtc = other.getDtc();
        if (this$dtc == null ? other$dtc != null : !this$dtc.equals(other$dtc)) {
            return false;
        }
        String this$alarmName = this.getAlarmName();
        String other$alarmName = other.getAlarmName();
        if (this$alarmName == null ? other$alarmName != null : !this$alarmName.equals(other$alarmName)) {
            return false;
        }
        String this$ecuType = this.getEcuType();
        String other$ecuType = other.getEcuType();
        if (this$ecuType == null ? other$ecuType != null : !this$ecuType.equals(other$ecuType)) {
            return false;
        }
        String this$componentCode = this.getComponentCode();
        String other$componentCode = other.getComponentCode();
        if (this$componentCode == null ? other$componentCode != null : !this$componentCode.equals(other$componentCode)) {
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
        if (this$updateTime == null ? other$updateTime != null : !((Object)this$updateTime).equals(other$updateTime)) {
            return false;
        }
        String this$modelName = this.getModelName();
        String other$modelName = other.getModelName();
        return !(this$modelName == null ? other$modelName != null : !this$modelName.equals(other$modelName));
    }

    protected boolean canEqual(Object other) {
        return other instanceof FaultConfig;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $modelId = this.getModelId();
        result = result * 59 + ($modelId == null ? 43 : ((Object)$modelId).hashCode());
        Integer $alarmLevel = this.getAlarmLevel();
        result = result * 59 + ($alarmLevel == null ? 43 : ((Object)$alarmLevel).hashCode());
        Integer $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : ((Object)$status).hashCode());
        Integer $deleted = this.getDeleted();
        result = result * 59 + ($deleted == null ? 43 : ((Object)$deleted).hashCode());
        String $faultCode = this.getFaultCode();
        result = result * 59 + ($faultCode == null ? 43 : $faultCode.hashCode());
        String $dtc = this.getDtc();
        result = result * 59 + ($dtc == null ? 43 : $dtc.hashCode());
        String $alarmName = this.getAlarmName();
        result = result * 59 + ($alarmName == null ? 43 : $alarmName.hashCode());
        String $ecuType = this.getEcuType();
        result = result * 59 + ($ecuType == null ? 43 : $ecuType.hashCode());
        String $componentCode = this.getComponentCode();
        result = result * 59 + ($componentCode == null ? 43 : $componentCode.hashCode());
        String $description = this.getDescription();
        result = result * 59 + ($description == null ? 43 : $description.hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        LocalDateTime $updateTime = this.getUpdateTime();
        result = result * 59 + ($updateTime == null ? 43 : ((Object)$updateTime).hashCode());
        String $modelName = this.getModelName();
        result = result * 59 + ($modelName == null ? 43 : $modelName.hashCode());
        return result;
    }

    public String toString() {
        return "FaultConfig(id=" + this.getId() + ", modelId=" + this.getModelId() + ", faultCode=" + this.getFaultCode() + ", dtc=" + this.getDtc() + ", alarmName=" + this.getAlarmName() + ", ecuType=" + this.getEcuType() + ", componentCode=" + this.getComponentCode() + ", alarmLevel=" + this.getAlarmLevel() + ", description=" + this.getDescription() + ", status=" + this.getStatus() + ", deleted=" + this.getDeleted() + ", createTime=" + String.valueOf(this.getCreateTime()) + ", updateTime=" + String.valueOf(this.getUpdateTime()) + ", modelName=" + this.getModelName() + ")";
    }
}

