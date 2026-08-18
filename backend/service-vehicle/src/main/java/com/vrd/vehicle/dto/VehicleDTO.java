/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.vehicle.dto;

public class VehicleDTO {
    private String vin;
    private Long modelId;
    private String plateNumber;
    private String color;
    private Integer productionYear;
    private String engineNumber;
    private String bodyNumber;
    private String configWord;
    private String currentEcuVersion;

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

    public String getCurrentEcuVersion() {
        return this.currentEcuVersion;
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

    public void setCurrentEcuVersion(String currentEcuVersion) {
        this.currentEcuVersion = currentEcuVersion;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VehicleDTO)) {
            return false;
        }
        VehicleDTO other = (VehicleDTO)o;
        if (!other.canEqual(this)) {
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
        return !(this$currentEcuVersion == null ? other$currentEcuVersion != null : !this$currentEcuVersion.equals(other$currentEcuVersion));
    }

    protected boolean canEqual(Object other) {
        return other instanceof VehicleDTO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $modelId = this.getModelId();
        result = result * 59 + ($modelId == null ? 43 : ((Object)$modelId).hashCode());
        Integer $productionYear = this.getProductionYear();
        result = result * 59 + ($productionYear == null ? 43 : ((Object)$productionYear).hashCode());
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
        return result;
    }

    public String toString() {
        return "VehicleDTO(vin=" + this.getVin() + ", modelId=" + this.getModelId() + ", plateNumber=" + this.getPlateNumber() + ", color=" + this.getColor() + ", productionYear=" + this.getProductionYear() + ", engineNumber=" + this.getEngineNumber() + ", bodyNumber=" + this.getBodyNumber() + ", configWord=" + this.getConfigWord() + ", currentEcuVersion=" + this.getCurrentEcuVersion() + ")";
    }
}

