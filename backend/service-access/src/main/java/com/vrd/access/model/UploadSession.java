/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.access.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UploadSession {
    private String uploadId;
    private String vin;
    private String ecuType;
    private String fileName;
    private Long fileSize;
    private String fileMd5;
    private LocalDateTime logStartTime;
    private LocalDateTime logEndTime;
    private LocalDateTime uploadStartTime;
    private final Set<Integer> uploadedChunks = ConcurrentHashMap.newKeySet();

    public String getUploadId() {
        return this.uploadId;
    }

    public String getVin() {
        return this.vin;
    }

    public String getEcuType() {
        return this.ecuType;
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

    public LocalDateTime getLogStartTime() {
        return this.logStartTime;
    }

    public LocalDateTime getLogEndTime() {
        return this.logEndTime;
    }

    public LocalDateTime getUploadStartTime() {
        return this.uploadStartTime;
    }

    public Set<Integer> getUploadedChunks() {
        return this.uploadedChunks;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public void setEcuType(String ecuType) {
        this.ecuType = ecuType;
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

    public void setLogStartTime(LocalDateTime logStartTime) {
        this.logStartTime = logStartTime;
    }

    public void setLogEndTime(LocalDateTime logEndTime) {
        this.logEndTime = logEndTime;
    }

    public void setUploadStartTime(LocalDateTime uploadStartTime) {
        this.uploadStartTime = uploadStartTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UploadSession)) {
            return false;
        }
        UploadSession other = (UploadSession)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$fileSize = this.getFileSize();
        Long other$fileSize = other.getFileSize();
        if (this$fileSize == null ? other$fileSize != null : !((Object)this$fileSize).equals(other$fileSize)) {
            return false;
        }
        String this$uploadId = this.getUploadId();
        String other$uploadId = other.getUploadId();
        if (this$uploadId == null ? other$uploadId != null : !this$uploadId.equals(other$uploadId)) {
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
        String this$fileName = this.getFileName();
        String other$fileName = other.getFileName();
        if (this$fileName == null ? other$fileName != null : !this$fileName.equals(other$fileName)) {
            return false;
        }
        String this$fileMd5 = this.getFileMd5();
        String other$fileMd5 = other.getFileMd5();
        if (this$fileMd5 == null ? other$fileMd5 != null : !this$fileMd5.equals(other$fileMd5)) {
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
        Set<Integer> this$uploadedChunks = this.getUploadedChunks();
        Set<Integer> other$uploadedChunks = other.getUploadedChunks();
        return !(this$uploadedChunks == null ? other$uploadedChunks != null : !((Object)this$uploadedChunks).equals(other$uploadedChunks));
    }

    protected boolean canEqual(Object other) {
        return other instanceof UploadSession;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $fileSize = this.getFileSize();
        result = result * 59 + ($fileSize == null ? 43 : ((Object)$fileSize).hashCode());
        String $uploadId = this.getUploadId();
        result = result * 59 + ($uploadId == null ? 43 : $uploadId.hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $ecuType = this.getEcuType();
        result = result * 59 + ($ecuType == null ? 43 : $ecuType.hashCode());
        String $fileName = this.getFileName();
        result = result * 59 + ($fileName == null ? 43 : $fileName.hashCode());
        String $fileMd5 = this.getFileMd5();
        result = result * 59 + ($fileMd5 == null ? 43 : $fileMd5.hashCode());
        LocalDateTime $logStartTime = this.getLogStartTime();
        result = result * 59 + ($logStartTime == null ? 43 : ((Object)$logStartTime).hashCode());
        LocalDateTime $logEndTime = this.getLogEndTime();
        result = result * 59 + ($logEndTime == null ? 43 : ((Object)$logEndTime).hashCode());
        LocalDateTime $uploadStartTime = this.getUploadStartTime();
        result = result * 59 + ($uploadStartTime == null ? 43 : ((Object)$uploadStartTime).hashCode());
        Set<Integer> $uploadedChunks = this.getUploadedChunks();
        result = result * 59 + ($uploadedChunks == null ? 43 : ((Object)$uploadedChunks).hashCode());
        return result;
    }

    public String toString() {
        return "UploadSession(uploadId=" + this.getUploadId() + ", vin=" + this.getVin() + ", ecuType=" + this.getEcuType() + ", fileName=" + this.getFileName() + ", fileSize=" + this.getFileSize() + ", fileMd5=" + this.getFileMd5() + ", logStartTime=" + String.valueOf(this.getLogStartTime()) + ", logEndTime=" + String.valueOf(this.getLogEndTime()) + ", uploadStartTime=" + String.valueOf(this.getUploadStartTime()) + ", uploadedChunks=" + String.valueOf(this.getUploadedChunks()) + ")";
    }
}

