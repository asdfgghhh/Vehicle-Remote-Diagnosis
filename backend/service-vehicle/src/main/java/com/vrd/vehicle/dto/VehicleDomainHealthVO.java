package com.vrd.vehicle.dto;

import java.time.LocalDateTime;
import java.util.List;

public class VehicleDomainHealthVO {
    private String vin;
    private String domainCode;
    private String domainName;
    private Integer healthScore;
    private String status;
    private Integer alertCount;
    private String riskLevel;
    private LocalDateTime updateTime;
    private List<ComponentHealth> components;

    public String getVin() { return this.vin; }
    public String getDomainCode() { return this.domainCode; }
    public String getDomainName() { return this.domainName; }
    public Integer getHealthScore() { return this.healthScore; }
    public String getStatus() { return this.status; }
    public Integer getAlertCount() { return this.alertCount; }
    public String getRiskLevel() { return this.riskLevel; }
    public LocalDateTime getUpdateTime() { return this.updateTime; }
    public List<ComponentHealth> getComponents() { return this.components; }

    public void setVin(String vin) { this.vin = vin; }
    public void setDomainCode(String domainCode) { this.domainCode = domainCode; }
    public void setDomainName(String domainName) { this.domainName = domainName; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
    public void setStatus(String status) { this.status = status; }
    public void setAlertCount(Integer alertCount) { this.alertCount = alertCount; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public void setComponents(List<ComponentHealth> components) { this.components = components; }

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
