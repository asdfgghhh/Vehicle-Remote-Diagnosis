/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.vehicle.rule.engine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SignalDataContext {
    private String vin;
    private Long vehicleId;
    private String signalName;
    private BigDecimal currentValue;
    private String unit;
    private String messageName;
    private Integer messageId;
    private LocalDateTime timestamp;
    private BigDecimal upperThreshold;
    private BigDecimal lowerThreshold;
    private BigDecimal trendChangeRate;
    private List<BigDecimal> historyValues;

    public boolean isAboveUpperThreshold() {
        return this.upperThreshold != null && this.currentValue != null && this.currentValue.compareTo(this.upperThreshold) > 0;
    }

    public boolean isBelowLowerThreshold() {
        return this.lowerThreshold != null && this.currentValue != null && this.currentValue.compareTo(this.lowerThreshold) < 0;
    }

    public boolean isWithinThreshold() {
        return !this.isAboveUpperThreshold() && !this.isBelowLowerThreshold();
    }

    public boolean isTrendRising() {
        return this.trendChangeRate != null && this.trendChangeRate.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isTrendFalling() {
        return this.trendChangeRate != null && this.trendChangeRate.compareTo(BigDecimal.ZERO) < 0;
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

    public BigDecimal getCurrentValue() {
        return this.currentValue;
    }

    public String getUnit() {
        return this.unit;
    }

    public String getMessageName() {
        return this.messageName;
    }

    public Integer getMessageId() {
        return this.messageId;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public BigDecimal getUpperThreshold() {
        return this.upperThreshold;
    }

    public BigDecimal getLowerThreshold() {
        return this.lowerThreshold;
    }

    public BigDecimal getTrendChangeRate() {
        return this.trendChangeRate;
    }

    public List<BigDecimal> getHistoryValues() {
        return this.historyValues;
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

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setUpperThreshold(BigDecimal upperThreshold) {
        this.upperThreshold = upperThreshold;
    }

    public void setLowerThreshold(BigDecimal lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
    }

    public void setTrendChangeRate(BigDecimal trendChangeRate) {
        this.trendChangeRate = trendChangeRate;
    }

    public void setHistoryValues(List<BigDecimal> historyValues) {
        this.historyValues = historyValues;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SignalDataContext)) {
            return false;
        }
        SignalDataContext other = (SignalDataContext)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$vehicleId = this.getVehicleId();
        Long other$vehicleId = other.getVehicleId();
        if (this$vehicleId == null ? other$vehicleId != null : !((Object)this$vehicleId).equals(other$vehicleId)) {
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
        BigDecimal this$currentValue = this.getCurrentValue();
        BigDecimal other$currentValue = other.getCurrentValue();
        if (this$currentValue == null ? other$currentValue != null : !((Object)this$currentValue).equals(other$currentValue)) {
            return false;
        }
        String this$unit = this.getUnit();
        String other$unit = other.getUnit();
        if (this$unit == null ? other$unit != null : !this$unit.equals(other$unit)) {
            return false;
        }
        String this$messageName = this.getMessageName();
        String other$messageName = other.getMessageName();
        if (this$messageName == null ? other$messageName != null : !this$messageName.equals(other$messageName)) {
            return false;
        }
        LocalDateTime this$timestamp = this.getTimestamp();
        LocalDateTime other$timestamp = other.getTimestamp();
        if (this$timestamp == null ? other$timestamp != null : !((Object)this$timestamp).equals(other$timestamp)) {
            return false;
        }
        BigDecimal this$upperThreshold = this.getUpperThreshold();
        BigDecimal other$upperThreshold = other.getUpperThreshold();
        if (this$upperThreshold == null ? other$upperThreshold != null : !((Object)this$upperThreshold).equals(other$upperThreshold)) {
            return false;
        }
        BigDecimal this$lowerThreshold = this.getLowerThreshold();
        BigDecimal other$lowerThreshold = other.getLowerThreshold();
        if (this$lowerThreshold == null ? other$lowerThreshold != null : !((Object)this$lowerThreshold).equals(other$lowerThreshold)) {
            return false;
        }
        BigDecimal this$trendChangeRate = this.getTrendChangeRate();
        BigDecimal other$trendChangeRate = other.getTrendChangeRate();
        if (this$trendChangeRate == null ? other$trendChangeRate != null : !((Object)this$trendChangeRate).equals(other$trendChangeRate)) {
            return false;
        }
        List<BigDecimal> this$historyValues = this.getHistoryValues();
        List<BigDecimal> other$historyValues = other.getHistoryValues();
        return !(this$historyValues == null ? other$historyValues != null : !((Object)this$historyValues).equals(other$historyValues));
    }

    protected boolean canEqual(Object other) {
        return other instanceof SignalDataContext;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $vehicleId = this.getVehicleId();
        result = result * 59 + ($vehicleId == null ? 43 : ((Object)$vehicleId).hashCode());
        Integer $messageId = this.getMessageId();
        result = result * 59 + ($messageId == null ? 43 : ((Object)$messageId).hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $signalName = this.getSignalName();
        result = result * 59 + ($signalName == null ? 43 : $signalName.hashCode());
        BigDecimal $currentValue = this.getCurrentValue();
        result = result * 59 + ($currentValue == null ? 43 : ((Object)$currentValue).hashCode());
        String $unit = this.getUnit();
        result = result * 59 + ($unit == null ? 43 : $unit.hashCode());
        String $messageName = this.getMessageName();
        result = result * 59 + ($messageName == null ? 43 : $messageName.hashCode());
        LocalDateTime $timestamp = this.getTimestamp();
        result = result * 59 + ($timestamp == null ? 43 : ((Object)$timestamp).hashCode());
        BigDecimal $upperThreshold = this.getUpperThreshold();
        result = result * 59 + ($upperThreshold == null ? 43 : ((Object)$upperThreshold).hashCode());
        BigDecimal $lowerThreshold = this.getLowerThreshold();
        result = result * 59 + ($lowerThreshold == null ? 43 : ((Object)$lowerThreshold).hashCode());
        BigDecimal $trendChangeRate = this.getTrendChangeRate();
        result = result * 59 + ($trendChangeRate == null ? 43 : ((Object)$trendChangeRate).hashCode());
        List<BigDecimal> $historyValues = this.getHistoryValues();
        result = result * 59 + ($historyValues == null ? 43 : ((Object)$historyValues).hashCode());
        return result;
    }

    public String toString() {
        return "SignalDataContext(vin=" + this.getVin() + ", vehicleId=" + this.getVehicleId() + ", signalName=" + this.getSignalName() + ", currentValue=" + String.valueOf(this.getCurrentValue()) + ", unit=" + this.getUnit() + ", messageName=" + this.getMessageName() + ", messageId=" + this.getMessageId() + ", timestamp=" + String.valueOf(this.getTimestamp()) + ", upperThreshold=" + String.valueOf(this.getUpperThreshold()) + ", lowerThreshold=" + String.valueOf(this.getLowerThreshold()) + ", trendChangeRate=" + String.valueOf(this.getTrendChangeRate()) + ", historyValues=" + String.valueOf(this.getHistoryValues()) + ")";
    }
}

