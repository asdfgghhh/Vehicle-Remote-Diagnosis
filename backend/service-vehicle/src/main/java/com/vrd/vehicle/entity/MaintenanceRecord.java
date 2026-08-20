package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName(value="maintenance_record")
public class MaintenanceRecord {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String vin;
    private Integer recordType;
    private String title;
    private String content;
    private Integer mileage;
    private LocalDate recordDate;
    private BigDecimal cost;
    private String operator;
    private LocalDateTime createTime;

    public Long getId() { return this.id; }
    public String getVin() { return this.vin; }
    public Integer getRecordType() { return this.recordType; }
    public String getTitle() { return this.title; }
    public String getContent() { return this.content; }
    public Integer getMileage() { return this.mileage; }
    public LocalDate getRecordDate() { return this.recordDate; }
    public BigDecimal getCost() { return this.cost; }
    public String getOperator() { return this.operator; }
    public LocalDateTime getCreateTime() { return this.createTime; }

    public void setId(Long id) { this.id = id; }
    public void setVin(String vin) { this.vin = vin; }
    public void setRecordType(Integer recordType) { this.recordType = recordType; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    public void setOperator(String operator) { this.operator = operator; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
