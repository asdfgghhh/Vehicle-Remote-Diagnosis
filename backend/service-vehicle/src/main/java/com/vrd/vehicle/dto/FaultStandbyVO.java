package com.vrd.vehicle.dto;

import java.util.List;

public class FaultStandbyVO {
    private String vin;
    private String plateNumber;
    private String modelName;
    private Integer healthScore;
    private String riskLevel;
    private Integer pendingFaultCount;
    private List<MaintenanceItem> maintenanceRecords;
    private List<FaultItem> pendingFaults;
    private List<PriorityItem> aiPriority;

    public String getVin() { return this.vin; }
    public String getPlateNumber() { return this.plateNumber; }
    public String getModelName() { return this.modelName; }
    public Integer getHealthScore() { return this.healthScore; }
    public String getRiskLevel() { return this.riskLevel; }
    public Integer getPendingFaultCount() { return this.pendingFaultCount; }
    public List<MaintenanceItem> getMaintenanceRecords() { return this.maintenanceRecords; }
    public List<FaultItem> getPendingFaults() { return this.pendingFaults; }
    public List<PriorityItem> getAiPriority() { return this.aiPriority; }

    public void setVin(String vin) { this.vin = vin; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setPendingFaultCount(Integer pendingFaultCount) { this.pendingFaultCount = pendingFaultCount; }
    public void setMaintenanceRecords(List<MaintenanceItem> maintenanceRecords) { this.maintenanceRecords = maintenanceRecords; }
    public void setPendingFaults(List<FaultItem> pendingFaults) { this.pendingFaults = pendingFaults; }
    public void setAiPriority(List<PriorityItem> aiPriority) { this.aiPriority = aiPriority; }

    public static class MaintenanceItem {
        private Long id;
        private String title;
        private Integer recordType;
        private java.time.LocalDate recordDate;
        private Integer mileage;
        private String operator;
        private String content;

        public Long getId() { return this.id; }
        public String getTitle() { return this.title; }
        public Integer getRecordType() { return this.recordType; }
        public java.time.LocalDate getRecordDate() { return this.recordDate; }
        public Integer getMileage() { return this.mileage; }
        public String getOperator() { return this.operator; }
        public String getContent() { return this.content; }

        public void setId(Long id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setRecordType(Integer recordType) { this.recordType = recordType; }
        public void setRecordDate(java.time.LocalDate recordDate) { this.recordDate = recordDate; }
        public void setMileage(Integer mileage) { this.mileage = mileage; }
        public void setOperator(String operator) { this.operator = operator; }
        public void setContent(String content) { this.content = content; }
    }

    public static class FaultItem {
        private Long id;
        private String faultCode;
        private String faultName;
        private String componentCode;
        private Integer status;
        private java.time.LocalDateTime faultTime;

        public Long getId() { return this.id; }
        public String getFaultCode() { return this.faultCode; }
        public String getFaultName() { return this.faultName; }
        public String getComponentCode() { return this.componentCode; }
        public Integer getStatus() { return this.status; }
        public java.time.LocalDateTime getFaultTime() { return this.faultTime; }

        public void setId(Long id) { this.id = id; }
        public void setFaultCode(String faultCode) { this.faultCode = faultCode; }
        public void setFaultName(String faultName) { this.faultName = faultName; }
        public void setComponentCode(String componentCode) { this.componentCode = componentCode; }
        public void setStatus(Integer status) { this.status = status; }
        public void setFaultTime(java.time.LocalDateTime faultTime) { this.faultTime = faultTime; }
    }

    public static class PriorityItem {
        private Integer priority;
        private String sceneName;
        private String reason;
        private String action;

        public Integer getPriority() { return this.priority; }
        public String getSceneName() { return this.sceneName; }
        public String getReason() { return this.reason; }
        public String getAction() { return this.action; }

        public void setPriority(Integer priority) { this.priority = priority; }
        public void setSceneName(String sceneName) { this.sceneName = sceneName; }
        public void setReason(String reason) { this.reason = reason; }
        public void setAction(String action) { this.action = action; }
    }
}
