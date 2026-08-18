/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.vehicle.dto;

import java.time.LocalDate;

public class VehicleEcuDTO {
    private String ecuType;
    private String ecuPartNumber;
    private String hardwareVersion;
    private String softwareVersion;
    private String supplier;
    private String serialNumber;
    private LocalDate installDate;

    public String getEcuType() {
        return this.ecuType;
    }

    public String getEcuPartNumber() {
        return this.ecuPartNumber;
    }

    public String getHardwareVersion() {
        return this.hardwareVersion;
    }

    public String getSoftwareVersion() {
        return this.softwareVersion;
    }

    public String getSupplier() {
        return this.supplier;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public LocalDate getInstallDate() {
        return this.installDate;
    }

    public void setEcuType(String ecuType) {
        this.ecuType = ecuType;
    }

    public void setEcuPartNumber(String ecuPartNumber) {
        this.ecuPartNumber = ecuPartNumber;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setInstallDate(LocalDate installDate) {
        this.installDate = installDate;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VehicleEcuDTO)) {
            return false;
        }
        VehicleEcuDTO other = (VehicleEcuDTO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$ecuType = this.getEcuType();
        String other$ecuType = other.getEcuType();
        if (this$ecuType == null ? other$ecuType != null : !this$ecuType.equals(other$ecuType)) {
            return false;
        }
        String this$ecuPartNumber = this.getEcuPartNumber();
        String other$ecuPartNumber = other.getEcuPartNumber();
        if (this$ecuPartNumber == null ? other$ecuPartNumber != null : !this$ecuPartNumber.equals(other$ecuPartNumber)) {
            return false;
        }
        String this$hardwareVersion = this.getHardwareVersion();
        String other$hardwareVersion = other.getHardwareVersion();
        if (this$hardwareVersion == null ? other$hardwareVersion != null : !this$hardwareVersion.equals(other$hardwareVersion)) {
            return false;
        }
        String this$softwareVersion = this.getSoftwareVersion();
        String other$softwareVersion = other.getSoftwareVersion();
        if (this$softwareVersion == null ? other$softwareVersion != null : !this$softwareVersion.equals(other$softwareVersion)) {
            return false;
        }
        String this$supplier = this.getSupplier();
        String other$supplier = other.getSupplier();
        if (this$supplier == null ? other$supplier != null : !this$supplier.equals(other$supplier)) {
            return false;
        }
        String this$serialNumber = this.getSerialNumber();
        String other$serialNumber = other.getSerialNumber();
        if (this$serialNumber == null ? other$serialNumber != null : !this$serialNumber.equals(other$serialNumber)) {
            return false;
        }
        LocalDate this$installDate = this.getInstallDate();
        LocalDate other$installDate = other.getInstallDate();
        return !(this$installDate == null ? other$installDate != null : !((Object)this$installDate).equals(other$installDate));
    }

    protected boolean canEqual(Object other) {
        return other instanceof VehicleEcuDTO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $ecuType = this.getEcuType();
        result = result * 59 + ($ecuType == null ? 43 : $ecuType.hashCode());
        String $ecuPartNumber = this.getEcuPartNumber();
        result = result * 59 + ($ecuPartNumber == null ? 43 : $ecuPartNumber.hashCode());
        String $hardwareVersion = this.getHardwareVersion();
        result = result * 59 + ($hardwareVersion == null ? 43 : $hardwareVersion.hashCode());
        String $softwareVersion = this.getSoftwareVersion();
        result = result * 59 + ($softwareVersion == null ? 43 : $softwareVersion.hashCode());
        String $supplier = this.getSupplier();
        result = result * 59 + ($supplier == null ? 43 : $supplier.hashCode());
        String $serialNumber = this.getSerialNumber();
        result = result * 59 + ($serialNumber == null ? 43 : $serialNumber.hashCode());
        LocalDate $installDate = this.getInstallDate();
        result = result * 59 + ($installDate == null ? 43 : ((Object)$installDate).hashCode());
        return result;
    }

    public String toString() {
        return "VehicleEcuDTO(ecuType=" + this.getEcuType() + ", ecuPartNumber=" + this.getEcuPartNumber() + ", hardwareVersion=" + this.getHardwareVersion() + ", softwareVersion=" + this.getSoftwareVersion() + ", supplier=" + this.getSupplier() + ", serialNumber=" + this.getSerialNumber() + ", installDate=" + String.valueOf(this.getInstallDate()) + ")";
    }
}

