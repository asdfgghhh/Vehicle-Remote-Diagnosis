package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName(value="maintenance_plan")
public class MaintenancePlan {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String vin;
    private String planName;
    private Integer planType;
    private Integer dueMileage;
    private LocalDate dueDate;
    private Integer lastDoneMileage;
    private LocalDate lastDoneDate;
    private Integer status;
    private String advice;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return this.id; }
    public String getVin() { return this.vin; }
    public String getPlanName() { return this.planName; }
    public Integer getPlanType() { return this.planType; }
    public Integer getDueMileage() { return this.dueMileage; }
    public LocalDate getDueDate() { return this.dueDate; }
    public Integer getLastDoneMileage() { return this.lastDoneMileage; }
    public LocalDate getLastDoneDate() { return this.lastDoneDate; }
    public Integer getStatus() { return this.status; }
    public String getAdvice() { return this.advice; }
    public LocalDateTime getCreateTime() { return this.createTime; }
    public LocalDateTime getUpdateTime() { return this.updateTime; }

    public void setId(Long id) { this.id = id; }
    public void setVin(String vin) { this.vin = vin; }
    public void setPlanName(String planName) { this.planName = planName; }
    public void setPlanType(Integer planType) { this.planType = planType; }
    public void setDueMileage(Integer dueMileage) { this.dueMileage = dueMileage; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setLastDoneMileage(Integer lastDoneMileage) { this.lastDoneMileage = lastDoneMileage; }
    public void setLastDoneDate(LocalDate lastDoneDate) { this.lastDoneDate = lastDoneDate; }
    public void setStatus(Integer status) { this.status = status; }
    public void setAdvice(String advice) { this.advice = advice; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
