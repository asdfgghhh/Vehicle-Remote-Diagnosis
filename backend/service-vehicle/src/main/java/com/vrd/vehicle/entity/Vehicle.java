/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.annotation.IdType
 *  com.baomidou.mybatisplus.annotation.TableId
 *  com.baomidou.mybatisplus.annotation.TableName
 */
package com.vrd.vehicle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName(value="vehicle")
public class Vehicle {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String vin;
    private Long modelId;
    private String plateNumber;
    private String color;
    private Integer productionYear;
    private String engineNumber;
    private String bodyNumber;
    private String configWord;
    private Integer status;
    private String currentEcuVersion;
    private Integer dataSource;
    private String externalId;
    private Integer healthScore;
    private String riskLevel;
    private BigDecimal batterySoh;
    private LocalDateTime lastOnlineTime;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return this.id;
    }

    public String getVin() {
        return this.vin;
    }

    public Long getModelId() {
        return this.modelId;
    }

    public String getPlateNumber() {
        return this.plateNumber;
    }

    public String getColor() {
        return this.color;
    }

    public Integer getProductionYear() {
        return this.productionYear;
    }

    public String getEngineNumber() {
        return this.engineNumber;
    }

    public String getBodyNumber() {
        return this.bodyNumber;
    }

    public String getConfigWord() {
        return this.configWord;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getCurrentEcuVersion() {
        return this.currentEcuVersion;
    }

    public Integer getDataSource() {
        return this.dataSource;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public Integer getHealthScore() {
        return this.healthScore;
    }

    public String getRiskLevel() {
        return this.riskLevel;
    }

    public BigDecimal getBatterySoh() {
        return this.batterySoh;
    }

    public LocalDateTime getLastOnlineTime() {
        return this.lastOnlineTime;
    }

    public Integer getDeleted() {
        return this.deleted;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public LocalDateTime getUpdateTime() {
        return this.updateTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setProductionYear(Integer productionYear) {
        this.productionYear = productionYear;
    }

    public void setEngineNumber(String engineNumber) {
        this.engineNumber = engineNumber;
    }

    public void setBodyNumber(String bodyNumber) {
        this.bodyNumber = bodyNumber;
    }

    public void setConfigWord(String configWord) {
        this.configWord = configWord;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setCurrentEcuVersion(String currentEcuVersion) {
        this.currentEcuVersion = currentEcuVersion;
    }

    public void setDataSource(Integer dataSource) {
        this.dataSource = dataSource;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setBatterySoh(BigDecimal batterySoh) {
        this.batterySoh = batterySoh;
    }

    public void setLastOnlineTime(LocalDateTime lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Vehicle)) {
            return false;
        }
        Vehicle other = (Vehicle)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Long this$modelId = this.getModelId();
        Long other$modelId = other.getModelId();
        if (this$modelId == null ? other$modelId != null : !((Object)this$modelId).equals(other$modelId)) {
            return false;
        }
        Integer this$productionYear = this.getProductionYear();
        Integer other$productionYear = other.getProductionYear();
        if (this$productionYear == null ? other$productionYear != null : !((Object)this$productionYear).equals(other$productionYear)) {
            return false;
        }
        Integer this$status = this.getStatus();
        Integer other$status = other.getStatus();
        if (this$status == null ? other$status != null : !((Object)this$status).equals(other$status)) {
            return false;
        }
        Integer this$dataSource = this.getDataSource();
        Integer other$dataSource = other.getDataSource();
        if (this$dataSource == null ? other$dataSource != null : !((Object)this$dataSource).equals(other$dataSource)) {
            return false;
        }
        Integer this$deleted = this.getDeleted();
        Integer other$deleted = other.getDeleted();
        if (this$deleted == null ? other$deleted != null : !((Object)this$deleted).equals(other$deleted)) {
            return false;
        }
        String this$vin = this.getVin();
        String other$vin = other.getVin();
        if (this$vin == null ? other$vin != null : !this$vin.equals(other$vin)) {
            return false;
        }
        String this$plateNumber = this.getPlateNumber();
        String other$plateNumber = other.getPlateNumber();
        if (this$plateNumber == null ? other$plateNumber != null : !this$plateNumber.equals(other$plateNumber)) {
            return false;
        }
        String this$color = this.getColor();
        String other$color = other.getColor();
        if (this$color == null ? other$color != null : !this$color.equals(other$color)) {
            return false;
        }
        String this$engineNumber = this.getEngineNumber();
        String other$engineNumber = other.getEngineNumber();
        if (this$engineNumber == null ? other$engineNumber != null : !this$engineNumber.equals(other$engineNumber)) {
            return false;
        }
        String this$bodyNumber = this.getBodyNumber();
        String other$bodyNumber = other.getBodyNumber();
        if (this$bodyNumber == null ? other$bodyNumber != null : !this$bodyNumber.equals(other$bodyNumber)) {
            return false;
        }
        String this$configWord = this.getConfigWord();
        String other$configWord = other.getConfigWord();
        if (this$configWord == null ? other$configWord != null : !this$configWord.equals(other$configWord)) {
            return false;
        }
        String this$currentEcuVersion = this.getCurrentEcuVersion();
        String other$currentEcuVersion = other.getCurrentEcuVersion();
        if (this$currentEcuVersion == null ? other$currentEcuVersion != null : !this$currentEcuVersion.equals(other$currentEcuVersion)) {
            return false;
        }
        String this$externalId = this.getExternalId();
        String other$externalId = other.getExternalId();
        if (this$externalId == null ? other$externalId != null : !this$externalId.equals(other$externalId)) {
            return false;
        }
        LocalDateTime this$createTime = this.getCreateTime();
        LocalDateTime other$createTime = other.getCreateTime();
        if (this$createTime == null ? other$createTime != null : !((Object)this$createTime).equals(other$createTime)) {
            return false;
        }
        LocalDateTime this$updateTime = this.getUpdateTime();
        LocalDateTime other$updateTime = other.getUpdateTime();
        return !(this$updateTime == null ? other$updateTime != null : !((Object)this$updateTime).equals(other$updateTime));
    }

    protected boolean canEqual(Object other) {
        return other instanceof Vehicle;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $modelId = this.getModelId();
        result = result * 59 + ($modelId == null ? 43 : ((Object)$modelId).hashCode());
        Integer $productionYear = this.getProductionYear();
        result = result * 59 + ($productionYear == null ? 43 : ((Object)$productionYear).hashCode());
        Integer $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : ((Object)$status).hashCode());
        Integer $dataSource = this.getDataSource();
        result = result * 59 + ($dataSource == null ? 43 : ((Object)$dataSource).hashCode());
        Integer $deleted = this.getDeleted();
        result = result * 59 + ($deleted == null ? 43 : ((Object)$deleted).hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $plateNumber = this.getPlateNumber();
        result = result * 59 + ($plateNumber == null ? 43 : $plateNumber.hashCode());
        String $color = this.getColor();
        result = result * 59 + ($color == null ? 43 : $color.hashCode());
        String $engineNumber = this.getEngineNumber();
        result = result * 59 + ($engineNumber == null ? 43 : $engineNumber.hashCode());
        String $bodyNumber = this.getBodyNumber();
        result = result * 59 + ($bodyNumber == null ? 43 : $bodyNumber.hashCode());
        String $configWord = this.getConfigWord();
        result = result * 59 + ($configWord == null ? 43 : $configWord.hashCode());
        String $currentEcuVersion = this.getCurrentEcuVersion();
        result = result * 59 + ($currentEcuVersion == null ? 43 : $currentEcuVersion.hashCode());
        String $externalId = this.getExternalId();
        result = result * 59 + ($externalId == null ? 43 : $externalId.hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        LocalDateTime $updateTime = this.getUpdateTime();
        result = result * 59 + ($updateTime == null ? 43 : ((Object)$updateTime).hashCode());
        return result;
    }

    public String toString() {
        return "Vehicle(id=" + this.getId() + ", vin=" + this.getVin() + ", modelId=" + this.getModelId() + ", plateNumber=" + this.getPlateNumber() + ", color=" + this.getColor() + ", productionYear=" + this.getProductionYear() + ", engineNumber=" + this.getEngineNumber() + ", bodyNumber=" + this.getBodyNumber() + ", configWord=" + this.getConfigWord() + ", status=" + this.getStatus() + ", currentEcuVersion=" + this.getCurrentEcuVersion() + ", dataSource=" + this.getDataSource() + ", externalId=" + this.getExternalId() + ", deleted=" + this.getDeleted() + ", createTime=" + String.valueOf(this.getCreateTime()) + ", updateTime=" + String.valueOf(this.getUpdateTime()) + ")";
    }
}

