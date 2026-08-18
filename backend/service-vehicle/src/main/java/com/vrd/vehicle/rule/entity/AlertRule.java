/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.annotation.IdType
 *  com.baomidou.mybatisplus.annotation.TableId
 *  com.baomidou.mybatisplus.annotation.TableName
 */
package com.vrd.vehicle.rule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName(value="alert_rule")
public class AlertRule {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String ruleName;
    private String ruleType;
    private Long modelId;
    private String modelName;
    private String signalName;
    private String messageName;
    private String conditionExpr;
    private Double upperThreshold;
    private Double lowerThreshold;
    private Integer consecutiveCount;
    private String trendDirection;
    private Double trendChangeRate;
    private Integer trendWindowSec;
    private Integer alertLevel;
    private String alertMessage;
    private String componentCode;
    private String ecuType;
    private Integer cooldownSec;
    private Integer status;
    private Integer priority;
    private String description;
    private String createdBy;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return this.id;
    }

    public String getRuleName() {
        return this.ruleName;
    }

    public String getRuleType() {
        return this.ruleType;
    }

    public Long getModelId() {
        return this.modelId;
    }

    public String getModelName() {
        return this.modelName;
    }

    public String getSignalName() {
        return this.signalName;
    }

    public String getMessageName() {
        return this.messageName;
    }

    public String getConditionExpr() {
        return this.conditionExpr;
    }

    public Double getUpperThreshold() {
        return this.upperThreshold;
    }

    public Double getLowerThreshold() {
        return this.lowerThreshold;
    }

    public Integer getConsecutiveCount() {
        return this.consecutiveCount;
    }

    public String getTrendDirection() {
        return this.trendDirection;
    }

    public Double getTrendChangeRate() {
        return this.trendChangeRate;
    }

    public Integer getTrendWindowSec() {
        return this.trendWindowSec;
    }

    public Integer getAlertLevel() {
        return this.alertLevel;
    }

    public String getAlertMessage() {
        return this.alertMessage;
    }

    public String getComponentCode() {
        return this.componentCode;
    }

    public String getEcuType() {
        return this.ecuType;
    }

    public Integer getCooldownSec() {
        return this.cooldownSec;
    }

    public Integer getStatus() {
        return this.status;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public String getDescription() {
        return this.description;
    }

    public String getCreatedBy() {
        return this.createdBy;
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

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public void setConditionExpr(String conditionExpr) {
        this.conditionExpr = conditionExpr;
    }

    public void setUpperThreshold(Double upperThreshold) {
        this.upperThreshold = upperThreshold;
    }

    public void setLowerThreshold(Double lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
    }

    public void setConsecutiveCount(Integer consecutiveCount) {
        this.consecutiveCount = consecutiveCount;
    }

    public void setTrendDirection(String trendDirection) {
        this.trendDirection = trendDirection;
    }

    public void setTrendChangeRate(Double trendChangeRate) {
        this.trendChangeRate = trendChangeRate;
    }

    public void setTrendWindowSec(Integer trendWindowSec) {
        this.trendWindowSec = trendWindowSec;
    }

    public void setAlertLevel(Integer alertLevel) {
        this.alertLevel = alertLevel;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public void setEcuType(String ecuType) {
        this.ecuType = ecuType;
    }

    public void setCooldownSec(Integer cooldownSec) {
        this.cooldownSec = cooldownSec;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
        if (!(o instanceof AlertRule)) {
            return false;
        }
        AlertRule other = (AlertRule)o;
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
        Double this$upperThreshold = this.getUpperThreshold();
        Double other$upperThreshold = other.getUpperThreshold();
        if (this$upperThreshold == null ? other$upperThreshold != null : !((Object)this$upperThreshold).equals(other$upperThreshold)) {
            return false;
        }
        Double this$lowerThreshold = this.getLowerThreshold();
        Double other$lowerThreshold = other.getLowerThreshold();
        if (this$lowerThreshold == null ? other$lowerThreshold != null : !((Object)this$lowerThreshold).equals(other$lowerThreshold)) {
            return false;
        }
        Integer this$consecutiveCount = this.getConsecutiveCount();
        Integer other$consecutiveCount = other.getConsecutiveCount();
        if (this$consecutiveCount == null ? other$consecutiveCount != null : !((Object)this$consecutiveCount).equals(other$consecutiveCount)) {
            return false;
        }
        Double this$trendChangeRate = this.getTrendChangeRate();
        Double other$trendChangeRate = other.getTrendChangeRate();
        if (this$trendChangeRate == null ? other$trendChangeRate != null : !((Object)this$trendChangeRate).equals(other$trendChangeRate)) {
            return false;
        }
        Integer this$trendWindowSec = this.getTrendWindowSec();
        Integer other$trendWindowSec = other.getTrendWindowSec();
        if (this$trendWindowSec == null ? other$trendWindowSec != null : !((Object)this$trendWindowSec).equals(other$trendWindowSec)) {
            return false;
        }
        Integer this$alertLevel = this.getAlertLevel();
        Integer other$alertLevel = other.getAlertLevel();
        if (this$alertLevel == null ? other$alertLevel != null : !((Object)this$alertLevel).equals(other$alertLevel)) {
            return false;
        }
        Integer this$cooldownSec = this.getCooldownSec();
        Integer other$cooldownSec = other.getCooldownSec();
        if (this$cooldownSec == null ? other$cooldownSec != null : !((Object)this$cooldownSec).equals(other$cooldownSec)) {
            return false;
        }
        Integer this$status = this.getStatus();
        Integer other$status = other.getStatus();
        if (this$status == null ? other$status != null : !((Object)this$status).equals(other$status)) {
            return false;
        }
        Integer this$priority = this.getPriority();
        Integer other$priority = other.getPriority();
        if (this$priority == null ? other$priority != null : !((Object)this$priority).equals(other$priority)) {
            return false;
        }
        Integer this$deleted = this.getDeleted();
        Integer other$deleted = other.getDeleted();
        if (this$deleted == null ? other$deleted != null : !((Object)this$deleted).equals(other$deleted)) {
            return false;
        }
        String this$ruleName = this.getRuleName();
        String other$ruleName = other.getRuleName();
        if (this$ruleName == null ? other$ruleName != null : !this$ruleName.equals(other$ruleName)) {
            return false;
        }
        String this$ruleType = this.getRuleType();
        String other$ruleType = other.getRuleType();
        if (this$ruleType == null ? other$ruleType != null : !this$ruleType.equals(other$ruleType)) {
            return false;
        }
        String this$modelName = this.getModelName();
        String other$modelName = other.getModelName();
        if (this$modelName == null ? other$modelName != null : !this$modelName.equals(other$modelName)) {
            return false;
        }
        String this$signalName = this.getSignalName();
        String other$signalName = other.getSignalName();
        if (this$signalName == null ? other$signalName != null : !this$signalName.equals(other$signalName)) {
            return false;
        }
        String this$messageName = this.getMessageName();
        String other$messageName = other.getMessageName();
        if (this$messageName == null ? other$messageName != null : !this$messageName.equals(other$messageName)) {
            return false;
        }
        String this$conditionExpr = this.getConditionExpr();
        String other$conditionExpr = other.getConditionExpr();
        if (this$conditionExpr == null ? other$conditionExpr != null : !this$conditionExpr.equals(other$conditionExpr)) {
            return false;
        }
        String this$trendDirection = this.getTrendDirection();
        String other$trendDirection = other.getTrendDirection();
        if (this$trendDirection == null ? other$trendDirection != null : !this$trendDirection.equals(other$trendDirection)) {
            return false;
        }
        String this$alertMessage = this.getAlertMessage();
        String other$alertMessage = other.getAlertMessage();
        if (this$alertMessage == null ? other$alertMessage != null : !this$alertMessage.equals(other$alertMessage)) {
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
        String this$description = this.getDescription();
        String other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) {
            return false;
        }
        String this$createdBy = this.getCreatedBy();
        String other$createdBy = other.getCreatedBy();
        if (this$createdBy == null ? other$createdBy != null : !this$createdBy.equals(other$createdBy)) {
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
        return other instanceof AlertRule;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $modelId = this.getModelId();
        result = result * 59 + ($modelId == null ? 43 : ((Object)$modelId).hashCode());
        Double $upperThreshold = this.getUpperThreshold();
        result = result * 59 + ($upperThreshold == null ? 43 : ((Object)$upperThreshold).hashCode());
        Double $lowerThreshold = this.getLowerThreshold();
        result = result * 59 + ($lowerThreshold == null ? 43 : ((Object)$lowerThreshold).hashCode());
        Integer $consecutiveCount = this.getConsecutiveCount();
        result = result * 59 + ($consecutiveCount == null ? 43 : ((Object)$consecutiveCount).hashCode());
        Double $trendChangeRate = this.getTrendChangeRate();
        result = result * 59 + ($trendChangeRate == null ? 43 : ((Object)$trendChangeRate).hashCode());
        Integer $trendWindowSec = this.getTrendWindowSec();
        result = result * 59 + ($trendWindowSec == null ? 43 : ((Object)$trendWindowSec).hashCode());
        Integer $alertLevel = this.getAlertLevel();
        result = result * 59 + ($alertLevel == null ? 43 : ((Object)$alertLevel).hashCode());
        Integer $cooldownSec = this.getCooldownSec();
        result = result * 59 + ($cooldownSec == null ? 43 : ((Object)$cooldownSec).hashCode());
        Integer $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : ((Object)$status).hashCode());
        Integer $priority = this.getPriority();
        result = result * 59 + ($priority == null ? 43 : ((Object)$priority).hashCode());
        Integer $deleted = this.getDeleted();
        result = result * 59 + ($deleted == null ? 43 : ((Object)$deleted).hashCode());
        String $ruleName = this.getRuleName();
        result = result * 59 + ($ruleName == null ? 43 : $ruleName.hashCode());
        String $ruleType = this.getRuleType();
        result = result * 59 + ($ruleType == null ? 43 : $ruleType.hashCode());
        String $modelName = this.getModelName();
        result = result * 59 + ($modelName == null ? 43 : $modelName.hashCode());
        String $signalName = this.getSignalName();
        result = result * 59 + ($signalName == null ? 43 : $signalName.hashCode());
        String $messageName = this.getMessageName();
        result = result * 59 + ($messageName == null ? 43 : $messageName.hashCode());
        String $conditionExpr = this.getConditionExpr();
        result = result * 59 + ($conditionExpr == null ? 43 : $conditionExpr.hashCode());
        String $trendDirection = this.getTrendDirection();
        result = result * 59 + ($trendDirection == null ? 43 : $trendDirection.hashCode());
        String $alertMessage = this.getAlertMessage();
        result = result * 59 + ($alertMessage == null ? 43 : $alertMessage.hashCode());
        String $componentCode = this.getComponentCode();
        result = result * 59 + ($componentCode == null ? 43 : $componentCode.hashCode());
        String $ecuType = this.getEcuType();
        result = result * 59 + ($ecuType == null ? 43 : $ecuType.hashCode());
        String $description = this.getDescription();
        result = result * 59 + ($description == null ? 43 : $description.hashCode());
        String $createdBy = this.getCreatedBy();
        result = result * 59 + ($createdBy == null ? 43 : $createdBy.hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        LocalDateTime $updateTime = this.getUpdateTime();
        result = result * 59 + ($updateTime == null ? 43 : ((Object)$updateTime).hashCode());
        return result;
    }

    public String toString() {
        return "AlertRule(id=" + this.getId() + ", ruleName=" + this.getRuleName() + ", ruleType=" + this.getRuleType() + ", modelId=" + this.getModelId() + ", modelName=" + this.getModelName() + ", signalName=" + this.getSignalName() + ", messageName=" + this.getMessageName() + ", conditionExpr=" + this.getConditionExpr() + ", upperThreshold=" + this.getUpperThreshold() + ", lowerThreshold=" + this.getLowerThreshold() + ", consecutiveCount=" + this.getConsecutiveCount() + ", trendDirection=" + this.getTrendDirection() + ", trendChangeRate=" + this.getTrendChangeRate() + ", trendWindowSec=" + this.getTrendWindowSec() + ", alertLevel=" + this.getAlertLevel() + ", alertMessage=" + this.getAlertMessage() + ", componentCode=" + this.getComponentCode() + ", ecuType=" + this.getEcuType() + ", cooldownSec=" + this.getCooldownSec() + ", status=" + this.getStatus() + ", priority=" + this.getPriority() + ", description=" + this.getDescription() + ", createdBy=" + this.getCreatedBy() + ", deleted=" + this.getDeleted() + ", createTime=" + String.valueOf(this.getCreateTime()) + ", updateTime=" + String.valueOf(this.getUpdateTime()) + ")";
    }
}

