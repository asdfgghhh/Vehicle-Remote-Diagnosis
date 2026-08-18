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
import java.time.LocalDateTime;

@TableName(value="sync_log")
public class SyncLog {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String syncType;
    private String source;
    private String target;
    private String vin;
    private String action;
    private Integer recordCount;
    private String status;
    private String message;
    private String payload;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;

    public Long getId() {
        return this.id;
    }

    public String getSyncType() {
        return this.syncType;
    }

    public String getSource() {
        return this.source;
    }

    public String getTarget() {
        return this.target;
    }

    public String getVin() {
        return this.vin;
    }

    public String getAction() {
        return this.action;
    }

    public Integer getRecordCount() {
        return this.recordCount;
    }

    public String getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

    public String getPayload() {
        return this.payload;
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSyncType(String syncType) {
        this.syncType = syncType;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SyncLog)) {
            return false;
        }
        SyncLog other = (SyncLog)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Integer this$recordCount = this.getRecordCount();
        Integer other$recordCount = other.getRecordCount();
        if (this$recordCount == null ? other$recordCount != null : !((Object)this$recordCount).equals(other$recordCount)) {
            return false;
        }
        String this$syncType = this.getSyncType();
        String other$syncType = other.getSyncType();
        if (this$syncType == null ? other$syncType != null : !this$syncType.equals(other$syncType)) {
            return false;
        }
        String this$source = this.getSource();
        String other$source = other.getSource();
        if (this$source == null ? other$source != null : !this$source.equals(other$source)) {
            return false;
        }
        String this$target = this.getTarget();
        String other$target = other.getTarget();
        if (this$target == null ? other$target != null : !this$target.equals(other$target)) {
            return false;
        }
        String this$vin = this.getVin();
        String other$vin = other.getVin();
        if (this$vin == null ? other$vin != null : !this$vin.equals(other$vin)) {
            return false;
        }
        String this$action = this.getAction();
        String other$action = other.getAction();
        if (this$action == null ? other$action != null : !this$action.equals(other$action)) {
            return false;
        }
        String this$status = this.getStatus();
        String other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) {
            return false;
        }
        String this$message = this.getMessage();
        String other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) {
            return false;
        }
        String this$payload = this.getPayload();
        String other$payload = other.getPayload();
        if (this$payload == null ? other$payload != null : !this$payload.equals(other$payload)) {
            return false;
        }
        LocalDateTime this$startTime = this.getStartTime();
        LocalDateTime other$startTime = other.getStartTime();
        if (this$startTime == null ? other$startTime != null : !((Object)this$startTime).equals(other$startTime)) {
            return false;
        }
        LocalDateTime this$endTime = this.getEndTime();
        LocalDateTime other$endTime = other.getEndTime();
        if (this$endTime == null ? other$endTime != null : !((Object)this$endTime).equals(other$endTime)) {
            return false;
        }
        LocalDateTime this$createTime = this.getCreateTime();
        LocalDateTime other$createTime = other.getCreateTime();
        return !(this$createTime == null ? other$createTime != null : !((Object)this$createTime).equals(other$createTime));
    }

    protected boolean canEqual(Object other) {
        return other instanceof SyncLog;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Integer $recordCount = this.getRecordCount();
        result = result * 59 + ($recordCount == null ? 43 : ((Object)$recordCount).hashCode());
        String $syncType = this.getSyncType();
        result = result * 59 + ($syncType == null ? 43 : $syncType.hashCode());
        String $source = this.getSource();
        result = result * 59 + ($source == null ? 43 : $source.hashCode());
        String $target = this.getTarget();
        result = result * 59 + ($target == null ? 43 : $target.hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $action = this.getAction();
        result = result * 59 + ($action == null ? 43 : $action.hashCode());
        String $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : $status.hashCode());
        String $message = this.getMessage();
        result = result * 59 + ($message == null ? 43 : $message.hashCode());
        String $payload = this.getPayload();
        result = result * 59 + ($payload == null ? 43 : $payload.hashCode());
        LocalDateTime $startTime = this.getStartTime();
        result = result * 59 + ($startTime == null ? 43 : ((Object)$startTime).hashCode());
        LocalDateTime $endTime = this.getEndTime();
        result = result * 59 + ($endTime == null ? 43 : ((Object)$endTime).hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        return result;
    }

    public String toString() {
        return "SyncLog(id=" + this.getId() + ", syncType=" + this.getSyncType() + ", source=" + this.getSource() + ", target=" + this.getTarget() + ", vin=" + this.getVin() + ", action=" + this.getAction() + ", recordCount=" + this.getRecordCount() + ", status=" + this.getStatus() + ", message=" + this.getMessage() + ", payload=" + this.getPayload() + ", startTime=" + String.valueOf(this.getStartTime()) + ", endTime=" + String.valueOf(this.getEndTime()) + ", createTime=" + String.valueOf(this.getCreateTime()) + ")";
    }
}

