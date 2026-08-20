package com.vrd.vehicle.dto;

import java.time.LocalDateTime;

public class RiskVehicleVO {
    private Long id;
    private String vin;
    private String plateNumber;
    private Long modelId;
    private String modelName;
    private Integer healthScore;
    private String riskLevel;
    private Integer activeFaultCount;
    private String riskReason;
    private LocalDateTime lastOnlineTime;

    public Long getId() { return this.id; }
    public String getVin() { return this.vin; }
    public String getPlateNumber() { return this.plateNumber; }
    public Long getModelId() { return this.modelId; }
    public String getModelName() { return this.modelName; }
    public Integer getHealthScore() { return this.healthScore; }
    public String getRiskLevel() { return this.riskLevel; }
    public Integer getActiveFaultCount() { return this.activeFaultCount; }
    public String getRiskReason() { return this.riskReason; }
    public LocalDateTime getLastOnlineTime() { return this.lastOnlineTime; }

    public void setId(Long id) { this.id = id; }
    public void setVin(String vin) { this.vin = vin; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public void setModelId(Long modelId) { this.modelId = modelId; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setActiveFaultCount(Integer activeFaultCount) { this.activeFaultCount = activeFaultCount; }
    public void setRiskReason(String riskReason) { this.riskReason = riskReason; }
    public void setLastOnlineTime(LocalDateTime lastOnlineTime) { this.lastOnlineTime = lastOnlineTime; }
}
