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

@TableName(value="alert_trigger_log")
public class AlertTriggerLog {
    @TableId(type=IdType.AUTO)
    private Long id;
    private Long ruleId;
    private String ruleName;
    private String vin;
    private Long vehicleId;
    private String signalName;
    private String signalValue;
    private String conditionMatched;
    private Integer alertLevel;
    private String alertMessage;
    private Integer notified;
    private LocalDateTime triggerTime;
    private LocalDateTime createTime;

    public Long getId() {
        return this.id;
    }

    public Long getRuleId() {
        return this.ruleId;
    }

    public String getRuleName() {
        return this.ruleName;
    }

    public String getVin() {
        return this.vin;
    }

    public Long getVehicleId() {
        return this.vehicleId;
    }

    public String getSignalName() {
        return this.signalName;
    }

    public String getSignalValue() {
        return this.signalValue;
    }

    public String getConditionMatched() {
        return this.conditionMatched;
    }

    public Integer getAlertLevel() {
        return this.alertLevel;
    }

    public String getAlertMessage() {
        return this.alertMessage;
    }

    public Integer getNotified() {
        return this.notified;
    }

    public LocalDateTime getTriggerTime() {
        return this.triggerTime;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public void setSignalValue(String signalValue) {
        this.signalValue = signalValue;
    }

    public void setConditionMatched(String conditionMatched) {
        this.conditionMatched = conditionMatched;
    }

    public void setAlertLevel(Integer alertLevel) {
        this.alertLevel = alertLevel;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public void setNotified(Integer notified) {
        this.notified = notified;
    }

    public void setTriggerTime(LocalDateTime triggerTime) {
        this.triggerTime = triggerTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AlertTriggerLog)) {
            return false;
        }
        AlertTriggerLog other = (AlertTriggerLog)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Long this$ruleId = this.getRuleId();
        Long other$ruleId = other.getRuleId();
        if (this$ruleId == null ? other$ruleId != null : !((Object)this$ruleId).equals(other$ruleId)) {
            return false;
        }
        Long this$vehicleId = this.getVehicleId();
        Long other$vehicleId = other.getVehicleId();
        if (this$vehicleId == null ? other$vehicleId != null : !((Object)this$vehicleId).equals(other$vehicleId)) {
            return false;
        }
        Integer this$alertLevel = this.getAlertLevel();
        Integer other$alertLevel = other.getAlertLevel();
        if (this$alertLevel == null ? other$alertLevel != null : !((Object)this$alertLevel).equals(other$alertLevel)) {
            return false;
        }
        Integer this$notified = this.getNotified();
        Integer other$notified = other.getNotified();
        if (this$notified == null ? other$notified != null : !((Object)this$notified).equals(other$notified)) {
            return false;
        }
        String this$ruleName = this.getRuleName();
        String other$ruleName = other.getRuleName();
        if (this$ruleName == null ? other$ruleName != null : !this$ruleName.equals(other$ruleName)) {
            return false;
        }
        String this$vin = this.getVin();
        String other$vin = other.getVin();
        if (this$vin == null ? other$vin != null : !this$vin.equals(other$vin)) {
            return false;
        }
        String this$signalName = this.getSignalName();
        String other$signalName = other.getSignalName();
        if (this$signalName == null ? other$signalName != null : !this$signalName.equals(other$signalName)) {
            return false;
        }
        String this$signalValue = this.getSignalValue();
        String other$signalValue = other.getSignalValue();
        if (this$signalValue == null ? other$signalValue != null : !this$signalValue.equals(other$signalValue)) {
            return false;
        }
        String this$conditionMatched = this.getConditionMatched();
        String other$conditionMatched = other.getConditionMatched();
        if (this$conditionMatched == null ? other$conditionMatched != null : !this$conditionMatched.equals(other$conditionMatched)) {
            return false;
        }
        String this$alertMessage = this.getAlertMessage();
        String other$alertMessage = other.getAlertMessage();
        if (this$alertMessage == null ? other$alertMessage != null : !this$alertMessage.equals(other$alertMessage)) {
            return false;
        }
        LocalDateTime this$triggerTime = this.getTriggerTime();
        LocalDateTime other$triggerTime = other.getTriggerTime();
        if (this$triggerTime == null ? other$triggerTime != null : !((Object)this$triggerTime).equals(other$triggerTime)) {
            return false;
        }
        LocalDateTime this$createTime = this.getCreateTime();
        LocalDateTime other$createTime = other.getCreateTime();
        return !(this$createTime == null ? other$createTime != null : !((Object)this$createTime).equals(other$createTime));
    }

    protected boolean canEqual(Object other) {
        return other instanceof AlertTriggerLog;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $ruleId = this.getRuleId();
        result = result * 59 + ($ruleId == null ? 43 : ((Object)$ruleId).hashCode());
        Long $vehicleId = this.getVehicleId();
        result = result * 59 + ($vehicleId == null ? 43 : ((Object)$vehicleId).hashCode());
        Integer $alertLevel = this.getAlertLevel();
        result = result * 59 + ($alertLevel == null ? 43 : ((Object)$alertLevel).hashCode());
        Integer $notified = this.getNotified();
        result = result * 59 + ($notified == null ? 43 : ((Object)$notified).hashCode());
        String $ruleName = this.getRuleName();
        result = result * 59 + ($ruleName == null ? 43 : $ruleName.hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $signalName = this.getSignalName();
        result = result * 59 + ($signalName == null ? 43 : $signalName.hashCode());
        String $signalValue = this.getSignalValue();
        result = result * 59 + ($signalValue == null ? 43 : $signalValue.hashCode());
        String $conditionMatched = this.getConditionMatched();
        result = result * 59 + ($conditionMatched == null ? 43 : $conditionMatched.hashCode());
        String $alertMessage = this.getAlertMessage();
        result = result * 59 + ($alertMessage == null ? 43 : $alertMessage.hashCode());
        LocalDateTime $triggerTime = this.getTriggerTime();
        result = result * 59 + ($triggerTime == null ? 43 : ((Object)$triggerTime).hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        return result;
    }

    public String toString() {
        return "AlertTriggerLog(id=" + this.getId() + ", ruleId=" + this.getRuleId() + ", ruleName=" + this.getRuleName() + ", vin=" + this.getVin() + ", vehicleId=" + this.getVehicleId() + ", signalName=" + this.getSignalName() + ", signalValue=" + this.getSignalValue() + ", conditionMatched=" + this.getConditionMatched() + ", alertLevel=" + this.getAlertLevel() + ", alertMessage=" + this.getAlertMessage() + ", notified=" + this.getNotified() + ", triggerTime=" + String.valueOf(this.getTriggerTime()) + ", createTime=" + String.valueOf(this.getCreateTime()) + ")";
    }
}

