package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName(value="fault_scene")
public class FaultScene {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String sceneCode;
    private String sceneName;
    private String description;
    private String faultCodes;
    private String diagSequence;
    private Integer priority;
    private BigDecimal aiConfidence;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return this.id; }
    public String getSceneCode() { return this.sceneCode; }
    public String getSceneName() { return this.sceneName; }
    public String getDescription() { return this.description; }
    public String getFaultCodes() { return this.faultCodes; }
    public String getDiagSequence() { return this.diagSequence; }
    public Integer getPriority() { return this.priority; }
    public BigDecimal getAiConfidence() { return this.aiConfidence; }
    public Integer getStatus() { return this.status; }
    public Integer getDeleted() { return this.deleted; }
    public LocalDateTime getCreateTime() { return this.createTime; }
    public LocalDateTime getUpdateTime() { return this.updateTime; }

    public void setId(Long id) { this.id = id; }
    public void setSceneCode(String sceneCode) { this.sceneCode = sceneCode; }
    public void setSceneName(String sceneName) { this.sceneName = sceneName; }
    public void setDescription(String description) { this.description = description; }
    public void setFaultCodes(String faultCodes) { this.faultCodes = faultCodes; }
    public void setDiagSequence(String diagSequence) { this.diagSequence = diagSequence; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public void setAiConfidence(BigDecimal aiConfidence) { this.aiConfidence = aiConfidence; }
    public void setStatus(Integer status) { this.status = status; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
