package com.vrd.vehicle.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VehicleHealthDetailVO {
    private String vin;
    private String plateNumber;
    private Long modelId;
    private String modelName;
    private Integer healthScore;
    private String riskLevel;
    private BigDecimal batterySoh;
    private LocalDateTime lastOnlineTime;
    private List<DomainHealth> domains;

    public String getVin() { return this.vin; }
    public String getPlateNumber() { return this.plateNumber; }
    public Long getModelId() { return this.modelId; }
    public String getModelName() { return this.modelName; }
    public Integer getHealthScore() { return this.healthScore; }
    public String getRiskLevel() { return this.riskLevel; }
    public BigDecimal getBatterySoh() { return this.batterySoh; }
    public LocalDateTime getLastOnlineTime() { return this.lastOnlineTime; }
    public List<DomainHealth> getDomains() { return this.domains; }

    public void setVin(String vin) { this.vin = vin; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public void setModelId(Long modelId) { this.modelId = modelId; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setBatterySoh(BigDecimal batterySoh) { this.batterySoh = batterySoh; }
    public void setLastOnlineTime(LocalDateTime lastOnlineTime) { this.lastOnlineTime = lastOnlineTime; }
    public void setDomains(List<DomainHealth> domains) { this.domains = domains; }

    public static class DomainHealth {
        private String domainCode;
        private String domainName;
        private Integer healthScore;
        private String status;
        private Integer alertCount;
        private String riskLevel;
        private LocalDateTime updateTime;
        private List<ComponentHealth> components;

        public String getDomainCode() { return this.domainCode; }
        public String getDomainName() { return this.domainName; }
        public Integer getHealthScore() { return this.healthScore; }
        public String getStatus() { return this.status; }
        public Integer getAlertCount() { return this.alertCount; }
        public String getRiskLevel() { return this.riskLevel; }
        public LocalDateTime getUpdateTime() { return this.updateTime; }
        public List<ComponentHealth> getComponents() { return this.components; }

        public void setDomainCode(String domainCode) { this.domainCode = domainCode; }
        public void setDomainName(String domainName) { this.domainName = domainName; }
        public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
        public void setStatus(String status) { this.status = status; }
        public void setAlertCount(Integer alertCount) { this.alertCount = alertCount; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
        public void setComponents(List<ComponentHealth> components) { this.components = components; }
    }

    public static class ComponentHealth {
        private String name;
        private Integer score;
        private String status;

        public String getName() { return this.name; }
        public Integer getScore() { return this.score; }
        public String getStatus() { return this.status; }

        public void setName(String name) { this.name = name; }
        public void setScore(Integer score) { this.score = score; }
        public void setStatus(String status) { this.status = status; }
    }
}
