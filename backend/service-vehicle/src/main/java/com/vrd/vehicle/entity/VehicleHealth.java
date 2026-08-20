package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName(value="vehicle_health")
public class VehicleHealth {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String vin;
    private String domainCode;
    private String domainName;
    private Integer healthScore;
    private String status;
    private String componentJson;
    private Integer alertCount;
    private String riskLevel;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;

    public Long getId() { return this.id; }
    public String getVin() { return this.vin; }
    public String getDomainCode() { return this.domainCode; }
    public String getDomainName() { return this.domainName; }
    public Integer getHealthScore() { return this.healthScore; }
    public String getStatus() { return this.status; }
    public String getComponentJson() { return this.componentJson; }
    public Integer getAlertCount() { return this.alertCount; }
    public String getRiskLevel() { return this.riskLevel; }
    public LocalDateTime getUpdateTime() { return this.updateTime; }
    public LocalDateTime getCreateTime() { return this.createTime; }

    public void setId(Long id) { this.id = id; }
    public void setVin(String vin) { this.vin = vin; }
    public void setDomainCode(String domainCode) { this.domainCode = domainCode; }
    public void setDomainName(String domainName) { this.domainName = domainName; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
    public void setStatus(String status) { this.status = status; }
    public void setComponentJson(String componentJson) { this.componentJson = componentJson; }
    public void setAlertCount(Integer alertCount) { this.alertCount = alertCount; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
