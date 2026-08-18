/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.signal.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VehicleSignal {
    private Long id;
    private String vin;
    private Long vehicleId;
    private String signalName;
    private String signalValue;
    private BigDecimal numericValue;
    private String unit;
    private Long timestamp;
    private LocalDateTime signalTime;
    private String messageName;
    private Integer messageId;
    private LocalDateTime createTime;

    public Long getId() {
        return this.id;
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

    public BigDecimal getNumericValue() {
        return this.numericValue;
    }

    public String getUnit() {
        return this.unit;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public LocalDateTime getSignalTime() {
        return this.signalTime;
    }

    public String getMessageName() {
        return this.messageName;
    }

    public Integer getMessageId() {
        return this.messageId;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
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

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public void setSignalValue(String signalValue) {
        this.signalValue = signalValue;
    }

    public void setNumericValue(BigDecimal numericValue) {
        this.numericValue = numericValue;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSignalTime(LocalDateTime signalTime) {
        this.signalTime = signalTime;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VehicleSignal)) {
            return false;
        }
        VehicleSignal other = (VehicleSignal)o;
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
        Long this$timestamp = this.getTimestamp();
        Long other$timestamp = other.getTimestamp();
        if (this$timestamp == null ? other$timestamp != null : !((Object)this$timestamp).equals(other$timestamp)) {
            return false;
        }
        Integer this$messageId = this.getMessageId();
        Integer other$messageId = other.getMessageId();
        if (this$messageId == null ? other$messageId != null : !((Object)this$messageId).equals(other$messageId)) {
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
        BigDecimal this$numericValue = this.getNumericValue();
        BigDecimal other$numericValue = other.getNumericValue();
        if (this$numericValue == null ? other$numericValue != null : !((Object)this$numericValue).equals(other$numericValue)) {
            return false;
        }
        String this$unit = this.getUnit();
        String other$unit = other.getUnit();
        if (this$unit == null ? other$unit != null : !this$unit.equals(other$unit)) {
            return false;
        }
        LocalDateTime this$signalTime = this.getSignalTime();
        LocalDateTime other$signalTime = other.getSignalTime();
        if (this$signalTime == null ? other$signalTime != null : !((Object)this$signalTime).equals(other$signalTime)) {
            return false;
        }
        String this$messageName = this.getMessageName();
        String other$messageName = other.getMessageName();
        if (this$messageName == null ? other$messageName != null : !this$messageName.equals(other$messageName)) {
            return false;
        }
        LocalDateTime this$createTime = this.getCreateTime();
        LocalDateTime other$createTime = other.getCreateTime();
        return !(this$createTime == null ? other$createTime != null : !((Object)this$createTime).equals(other$createTime));
    }

    protected boolean canEqual(Object other) {
        return other instanceof VehicleSignal;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $vehicleId = this.getVehicleId();
        result = result * 59 + ($vehicleId == null ? 43 : ((Object)$vehicleId).hashCode());
        Long $timestamp = this.getTimestamp();
        result = result * 59 + ($timestamp == null ? 43 : ((Object)$timestamp).hashCode());
        Integer $messageId = this.getMessageId();
        result = result * 59 + ($messageId == null ? 43 : ((Object)$messageId).hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $signalName = this.getSignalName();
        result = result * 59 + ($signalName == null ? 43 : $signalName.hashCode());
        String $signalValue = this.getSignalValue();
        result = result * 59 + ($signalValue == null ? 43 : $signalValue.hashCode());
        BigDecimal $numericValue = this.getNumericValue();
        result = result * 59 + ($numericValue == null ? 43 : ((Object)$numericValue).hashCode());
        String $unit = this.getUnit();
        result = result * 59 + ($unit == null ? 43 : $unit.hashCode());
        LocalDateTime $signalTime = this.getSignalTime();
        result = result * 59 + ($signalTime == null ? 43 : ((Object)$signalTime).hashCode());
        String $messageName = this.getMessageName();
        result = result * 59 + ($messageName == null ? 43 : $messageName.hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        return result;
    }

    public String toString() {
        return "VehicleSignal(id=" + this.getId() + ", vin=" + this.getVin() + ", vehicleId=" + this.getVehicleId() + ", signalName=" + this.getSignalName() + ", signalValue=" + this.getSignalValue() + ", numericValue=" + String.valueOf(this.getNumericValue()) + ", unit=" + this.getUnit() + ", timestamp=" + this.getTimestamp() + ", signalTime=" + String.valueOf(this.getSignalTime()) + ", messageName=" + this.getMessageName() + ", messageId=" + this.getMessageId() + ", createTime=" + String.valueOf(this.getCreateTime()) + ")";
    }
}

