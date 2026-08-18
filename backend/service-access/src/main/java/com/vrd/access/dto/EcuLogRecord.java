/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.access.dto;

import java.time.LocalDateTime;

public class EcuLogRecord {
    private Long id;
    private String vin;
    private String ecuType;
    private LocalDateTime logStartTime;
    private LocalDateTime logEndTime;
    private LocalDateTime uploadStartTime;
    private LocalDateTime uploadEndTime;
    private String storageAddress;
    private String storageKey;
    private String storageType;
    private String fileName;
    private Long fileSize;
    private String fileMd5;

    public Long getId() {
        return this.id;
    }

    public String getVin() {
        return this.vin;
    }

    public String getEcuType() {
        return this.ecuType;
    }

    public LocalDateTime getLogStartTime() {
        return this.logStartTime;
    }

    public LocalDateTime getLogEndTime() {
        return this.logEndTime;
    }

    public LocalDateTime getUploadStartTime() {
        return this.uploadStartTime;
    }

    public LocalDateTime getUploadEndTime() {
        return this.uploadEndTime;
    }

    public String getStorageAddress() {
        return this.storageAddress;
    }

    public String getStorageKey() {
        return this.storageKey;
    }

    public String getStorageType() {
        return this.storageType;
    }

    public String getFileName() {
        return this.fileName;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public String getFileMd5() {
        return this.fileMd5;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public void setEcuType(String ecuType) {
        this.ecuType = ecuType;
    }

    public void setLogStartTime(LocalDateTime logStartTime) {
        this.logStartTime = logStartTime;
    }

    public void setLogEndTime(LocalDateTime logEndTime) {
        this.logEndTime = logEndTime;
    }

    public void setUploadStartTime(LocalDateTime uploadStartTime) {
        this.uploadStartTime = uploadStartTime;
    }

    public void setUploadEndTime(LocalDateTime uploadEndTime) {
        this.uploadEndTime = uploadEndTime;
    }

    public void setStorageAddress(String storageAddress) {
        this.storageAddress = storageAddress;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EcuLogRecord)) {
            return false;
        }
        EcuLogRecord other = (EcuLogRecord)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Long this$fileSize = this.getFileSize();
        Long other$fileSize = other.getFileSize();
        if (this$fileSize == null ? other$fileSize != null : !((Object)this$fileSize).equals(other$fileSize)) {
            return false;
        }
        String this$vin = this.getVin();
        String other$vin = other.getVin();
        if (this$vin == null ? other$vin != null : !this$vin.equals(other$vin)) {
            return false;
        }
        String this$ecuType = this.getEcuType();
        String other$ecuType = other.getEcuType();
        if (this$ecuType == null ? other$ecuType != null : !this$ecuType.equals(other$ecuType)) {
            return false;
        }
        LocalDateTime this$logStartTime = this.getLogStartTime();
        LocalDateTime other$logStartTime = other.getLogStartTime();
        if (this$logStartTime == null ? other$logStartTime != null : !((Object)this$logStartTime).equals(other$logStartTime)) {
            return false;
        }
        LocalDateTime this$logEndTime = this.getLogEndTime();
        LocalDateTime other$logEndTime = other.getLogEndTime();
        if (this$logEndTime == null ? other$logEndTime != null : !((Object)this$logEndTime).equals(other$logEndTime)) {
            return false;
        }
        LocalDateTime this$uploadStartTime = this.getUploadStartTime();
        LocalDateTime other$uploadStartTime = other.getUploadStartTime();
        if (this$uploadStartTime == null ? other$uploadStartTime != null : !((Object)this$uploadStartTime).equals(other$uploadStartTime)) {
            return false;
        }
        LocalDateTime this$uploadEndTime = this.getUploadEndTime();
        LocalDateTime other$uploadEndTime = other.getUploadEndTime();
        if (this$uploadEndTime == null ? other$uploadEndTime != null : !((Object)this$uploadEndTime).equals(other$uploadEndTime)) {
            return false;
        }
        String this$storageAddress = this.getStorageAddress();
        String other$storageAddress = other.getStorageAddress();
        if (this$storageAddress == null ? other$storageAddress != null : !this$storageAddress.equals(other$storageAddress)) {
            return false;
        }
        String this$storageKey = this.getStorageKey();
        String other$storageKey = other.getStorageKey();
        if (this$storageKey == null ? other$storageKey != null : !this$storageKey.equals(other$storageKey)) {
            return false;
        }
        String this$storageType = this.getStorageType();
        String other$storageType = other.getStorageType();
        if (this$storageType == null ? other$storageType != null : !this$storageType.equals(other$storageType)) {
            return false;
        }
        String this$fileName = this.getFileName();
        String other$fileName = other.getFileName();
        if (this$fileName == null ? other$fileName != null : !this$fileName.equals(other$fileName)) {
            return false;
        }
        String this$fileMd5 = this.getFileMd5();
        String other$fileMd5 = other.getFileMd5();
        return !(this$fileMd5 == null ? other$fileMd5 != null : !this$fileMd5.equals(other$fileMd5));
    }

    protected boolean canEqual(Object other) {
        return other instanceof EcuLogRecord;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $fileSize = this.getFileSize();
        result = result * 59 + ($fileSize == null ? 43 : ((Object)$fileSize).hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $ecuType = this.getEcuType();
        result = result * 59 + ($ecuType == null ? 43 : $ecuType.hashCode());
        LocalDateTime $logStartTime = this.getLogStartTime();
        result = result * 59 + ($logStartTime == null ? 43 : ((Object)$logStartTime).hashCode());
        LocalDateTime $logEndTime = this.getLogEndTime();
        result = result * 59 + ($logEndTime == null ? 43 : ((Object)$logEndTime).hashCode());
        LocalDateTime $uploadStartTime = this.getUploadStartTime();
        result = result * 59 + ($uploadStartTime == null ? 43 : ((Object)$uploadStartTime).hashCode());
        LocalDateTime $uploadEndTime = this.getUploadEndTime();
        result = result * 59 + ($uploadEndTime == null ? 43 : ((Object)$uploadEndTime).hashCode());
        String $storageAddress = this.getStorageAddress();
        result = result * 59 + ($storageAddress == null ? 43 : $storageAddress.hashCode());
        String $storageKey = this.getStorageKey();
        result = result * 59 + ($storageKey == null ? 43 : $storageKey.hashCode());
        String $storageType = this.getStorageType();
        result = result * 59 + ($storageType == null ? 43 : $storageType.hashCode());
        String $fileName = this.getFileName();
        result = result * 59 + ($fileName == null ? 43 : $fileName.hashCode());
        String $fileMd5 = this.getFileMd5();
        result = result * 59 + ($fileMd5 == null ? 43 : $fileMd5.hashCode());
        return result;
    }

    public String toString() {
        return "EcuLogRecord(id=" + this.getId() + ", vin=" + this.getVin() + ", ecuType=" + this.getEcuType() + ", logStartTime=" + String.valueOf(this.getLogStartTime()) + ", logEndTime=" + String.valueOf(this.getLogEndTime()) + ", uploadStartTime=" + String.valueOf(this.getUploadStartTime()) + ", uploadEndTime=" + String.valueOf(this.getUploadEndTime()) + ", storageAddress=" + this.getStorageAddress() + ", storageKey=" + this.getStorageKey() + ", storageType=" + this.getStorageType() + ", fileName=" + this.getFileName() + ", fileSize=" + this.getFileSize() + ", fileMd5=" + this.getFileMd5() + ")";
    }
}

