/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.annotation.IdType
 *  com.baomidou.mybatisplus.annotation.TableId
 *  com.baomidou.mybatisplus.annotation.TableName
 */
package com.vrd.dbc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName(value="dbc_file")
public class DbcFile {
    @TableId(type=IdType.AUTO)
    private Long id;
    private Long modelId;
    private String modelName;
    private String fileName;
    private String storageKey;
    private String storageAddress;
    private String storageType;
    private String filePath;
    private Long fileSize;
    private String version;
    private String description;
    private String parseResult;
    private Integer messageCount;
    private Integer signalCount;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return this.id;
    }

    public Long getModelId() {
        return this.modelId;
    }

    public String getModelName() {
        return this.modelName;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getStorageKey() {
        return this.storageKey;
    }

    public String getStorageAddress() {
        return this.storageAddress;
    }

    public String getStorageType() {
        return this.storageType;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public String getVersion() {
        return this.version;
    }

    public String getDescription() {
        return this.description;
    }

    public String getParseResult() {
        return this.parseResult;
    }

    public Integer getMessageCount() {
        return this.messageCount;
    }

    public Integer getSignalCount() {
        return this.signalCount;
    }

    public Integer getStatus() {
        return this.status;
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

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void setStorageAddress(String storageAddress) {
        this.storageAddress = storageAddress;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setParseResult(String parseResult) {
        this.parseResult = parseResult;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public void setSignalCount(Integer signalCount) {
        this.signalCount = signalCount;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
        if (!(o instanceof DbcFile)) {
            return false;
        }
        DbcFile other = (DbcFile)o;
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
        Long this$fileSize = this.getFileSize();
        Long other$fileSize = other.getFileSize();
        if (this$fileSize == null ? other$fileSize != null : !((Object)this$fileSize).equals(other$fileSize)) {
            return false;
        }
        Integer this$messageCount = this.getMessageCount();
        Integer other$messageCount = other.getMessageCount();
        if (this$messageCount == null ? other$messageCount != null : !((Object)this$messageCount).equals(other$messageCount)) {
            return false;
        }
        Integer this$signalCount = this.getSignalCount();
        Integer other$signalCount = other.getSignalCount();
        if (this$signalCount == null ? other$signalCount != null : !((Object)this$signalCount).equals(other$signalCount)) {
            return false;
        }
        Integer this$status = this.getStatus();
        Integer other$status = other.getStatus();
        if (this$status == null ? other$status != null : !((Object)this$status).equals(other$status)) {
            return false;
        }
        Integer this$deleted = this.getDeleted();
        Integer other$deleted = other.getDeleted();
        if (this$deleted == null ? other$deleted != null : !((Object)this$deleted).equals(other$deleted)) {
            return false;
        }
        String this$modelName = this.getModelName();
        String other$modelName = other.getModelName();
        if (this$modelName == null ? other$modelName != null : !this$modelName.equals(other$modelName)) {
            return false;
        }
        String this$fileName = this.getFileName();
        String other$fileName = other.getFileName();
        if (this$fileName == null ? other$fileName != null : !this$fileName.equals(other$fileName)) {
            return false;
        }
        String this$storageKey = this.getStorageKey();
        String other$storageKey = other.getStorageKey();
        if (this$storageKey == null ? other$storageKey != null : !this$storageKey.equals(other$storageKey)) {
            return false;
        }
        String this$storageAddress = this.getStorageAddress();
        String other$storageAddress = other.getStorageAddress();
        if (this$storageAddress == null ? other$storageAddress != null : !this$storageAddress.equals(other$storageAddress)) {
            return false;
        }
        String this$storageType = this.getStorageType();
        String other$storageType = other.getStorageType();
        if (this$storageType == null ? other$storageType != null : !this$storageType.equals(other$storageType)) {
            return false;
        }
        String this$filePath = this.getFilePath();
        String other$filePath = other.getFilePath();
        if (this$filePath == null ? other$filePath != null : !this$filePath.equals(other$filePath)) {
            return false;
        }
        String this$version = this.getVersion();
        String other$version = other.getVersion();
        if (this$version == null ? other$version != null : !this$version.equals(other$version)) {
            return false;
        }
        String this$description = this.getDescription();
        String other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) {
            return false;
        }
        String this$parseResult = this.getParseResult();
        String other$parseResult = other.getParseResult();
        if (this$parseResult == null ? other$parseResult != null : !this$parseResult.equals(other$parseResult)) {
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
        return other instanceof DbcFile;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $modelId = this.getModelId();
        result = result * 59 + ($modelId == null ? 43 : ((Object)$modelId).hashCode());
        Long $fileSize = this.getFileSize();
        result = result * 59 + ($fileSize == null ? 43 : ((Object)$fileSize).hashCode());
        Integer $messageCount = this.getMessageCount();
        result = result * 59 + ($messageCount == null ? 43 : ((Object)$messageCount).hashCode());
        Integer $signalCount = this.getSignalCount();
        result = result * 59 + ($signalCount == null ? 43 : ((Object)$signalCount).hashCode());
        Integer $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : ((Object)$status).hashCode());
        Integer $deleted = this.getDeleted();
        result = result * 59 + ($deleted == null ? 43 : ((Object)$deleted).hashCode());
        String $modelName = this.getModelName();
        result = result * 59 + ($modelName == null ? 43 : $modelName.hashCode());
        String $fileName = this.getFileName();
        result = result * 59 + ($fileName == null ? 43 : $fileName.hashCode());
        String $storageKey = this.getStorageKey();
        result = result * 59 + ($storageKey == null ? 43 : $storageKey.hashCode());
        String $storageAddress = this.getStorageAddress();
        result = result * 59 + ($storageAddress == null ? 43 : $storageAddress.hashCode());
        String $storageType = this.getStorageType();
        result = result * 59 + ($storageType == null ? 43 : $storageType.hashCode());
        String $filePath = this.getFilePath();
        result = result * 59 + ($filePath == null ? 43 : $filePath.hashCode());
        String $version = this.getVersion();
        result = result * 59 + ($version == null ? 43 : $version.hashCode());
        String $description = this.getDescription();
        result = result * 59 + ($description == null ? 43 : $description.hashCode());
        String $parseResult = this.getParseResult();
        result = result * 59 + ($parseResult == null ? 43 : $parseResult.hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        LocalDateTime $updateTime = this.getUpdateTime();
        result = result * 59 + ($updateTime == null ? 43 : ((Object)$updateTime).hashCode());
        return result;
    }

    public String toString() {
        return "DbcFile(id=" + this.getId() + ", modelId=" + this.getModelId() + ", modelName=" + this.getModelName() + ", fileName=" + this.getFileName() + ", storageKey=" + this.getStorageKey() + ", storageAddress=" + this.getStorageAddress() + ", storageType=" + this.getStorageType() + ", filePath=" + this.getFilePath() + ", fileSize=" + this.getFileSize() + ", version=" + this.getVersion() + ", description=" + this.getDescription() + ", parseResult=" + this.getParseResult() + ", messageCount=" + this.getMessageCount() + ", signalCount=" + this.getSignalCount() + ", status=" + this.getStatus() + ", deleted=" + this.getDeleted() + ", createTime=" + String.valueOf(this.getCreateTime()) + ", updateTime=" + String.valueOf(this.getUpdateTime()) + ")";
    }
}

