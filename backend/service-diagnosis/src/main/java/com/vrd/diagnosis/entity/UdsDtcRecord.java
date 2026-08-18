package com.vrd.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName(value="uds_dtc_record")
public class UdsDtcRecord {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String traceId;
    private String vin;
    private Long vehicleId;
    private String ecuType;
    private String dtcCode;
    private Integer dtcStatus;
    private String dtcDescription;
    private Integer severity;
    private Integer faultStatus;
    private LocalDateTime detectionTime;
    private LocalDateTime createTime;

    public Long getId() {
        return this.id;
    }

    public String getTraceId() {
        return this.traceId;
    }

    public String getVin() {
        return this.vin;
    }

    public Long getVehicleId() {
        return this.vehicleId;
    }

    public String getEcuType() {
        return this.ecuType;
    }

    public String getDtcCode() {
        return this.dtcCode;
    }

    public Integer getDtcStatus() {
        return this.dtcStatus;
    }

    public String getDtcDescription() {
        return this.dtcDescription;
    }

    public Integer getSeverity() {
        return this.severity;
    }

    public Integer getFaultStatus() {
        return this.faultStatus;
    }

    public LocalDateTime getDetectionTime() {
        return this.detectionTime;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setEcuType(String ecuType) {
        this.ecuType = ecuType;
    }

    public void setDtcCode(String dtcCode) {
        this.dtcCode = dtcCode;
    }

    public void setDtcStatus(Integer dtcStatus) {
        this.dtcStatus = dtcStatus;
    }

    public void setDtcDescription(String dtcDescription) {
        this.dtcDescription = dtcDescription;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public void setFaultStatus(Integer faultStatus) {
        this.faultStatus = faultStatus;
    }

    public void setDetectionTime(LocalDateTime detectionTime) {
        this.detectionTime = detectionTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UdsDtcRecord)) {
            return false;
        }
        UdsDtcRecord other = (UdsDtcRecord)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Long this$vehicleId = this.getVehicleId();
        Long other$vehicleId = other.getVehicleId();
        if (this$vehicleId == null ? other$vehicleId != null : !((Object)this$vehicleId).equals(other$vehicleId)) {
            return false;
        }
        Integer this$dtcStatus = this.getDtcStatus();
        Integer other$dtcStatus = other.getDtcStatus();
        if (this$dtcStatus == null ? other$dtcStatus != null : !((Object)this$dtcStatus).equals(other$dtcStatus)) {
            return false;
        }
        Integer this$severity = this.getSeverity();
        Integer other$severity = other.getSeverity();
        if (this$severity == null ? other$severity != null : !((Object)this$severity).equals(other$severity)) {
            return false;
        }
        Integer this$faultStatus = this.getFaultStatus();
        Integer other$faultStatus = other.getFaultStatus();
        if (this$faultStatus == null ? other$faultStatus != null : !((Object)this$faultStatus).equals(other$faultStatus)) {
            return false;
        }
        String this$traceId = this.getTraceId();
        String other$traceId = other.getTraceId();
        if (this$traceId == null ? other$traceId != null : !this$traceId.equals(other$traceId)) {
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
        String this$dtcCode = this.getDtcCode();
        String other$dtcCode = other.getDtcCode();
        if (this$dtcCode == null ? other$dtcCode != null : !this$dtcCode.equals(other$dtcCode)) {
            return false;
        }
        String this$dtcDescription = this.getDtcDescription();
        String other$dtcDescription = other.getDtcDescription();
        if (this$dtcDescription == null ? other$dtcDescription != null : !this$dtcDescription.equals(other$dtcDescription)) {
            return false;
        }
        LocalDateTime this$detectionTime = this.getDetectionTime();
        LocalDateTime other$detectionTime = other.getDetectionTime();
        if (this$detectionTime == null ? other$detectionTime != null : !((Object)this$detectionTime).equals(other$detectionTime)) {
            return false;
        }
        LocalDateTime this$createTime = this.getCreateTime();
        LocalDateTime other$createTime = other.getCreateTime();
        return !(this$createTime == null ? other$createTime != null : !((Object)this$createTime).equals(other$createTime));
    }

    protected boolean canEqual(Object other) {
        return other instanceof UdsDtcRecord;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $vehicleId = this.getVehicleId();
        result = result * 59 + ($vehicleId == null ? 43 : ((Object)$vehicleId).hashCode());
        Integer $dtcStatus = this.getDtcStatus();
        result = result * 59 + ($dtcStatus == null ? 43 : ((Object)$dtcStatus).hashCode());
        Integer $severity = this.getSeverity();
        result = result * 59 + ($severity == null ? 43 : ((Object)$severity).hashCode());
        Integer $faultStatus = this.getFaultStatus();
        result = result * 59 + ($faultStatus == null ? 43 : ((Object)$faultStatus).hashCode());
        String $traceId = this.getTraceId();
        result = result * 59 + ($traceId == null ? 43 : $traceId.hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $ecuType = this.getEcuType();
        result = result * 59 + ($ecuType == null ? 43 : $ecuType.hashCode());
        String $dtcCode = this.getDtcCode();
        result = result * 59 + ($dtcCode == null ? 43 : $dtcCode.hashCode());
        String $dtcDescription = this.getDtcDescription();
        result = result * 59 + ($dtcDescription == null ? 43 : $dtcDescription.hashCode());
        LocalDateTime $detectionTime = this.getDetectionTime();
        result = result * 59 + ($detectionTime == null ? 43 : ((Object)$detectionTime).hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        return result;
    }

    public String toString() {
        return "UdsDtcRecord(id=" + this.getId() + ", traceId=" + this.getTraceId() + ", vin=" + this.getVin() + ", vehicleId=" + this.getVehicleId() + ", ecuType=" + this.getEcuType() + ", dtcCode=" + this.getDtcCode() + ", dtcStatus=" + this.getDtcStatus() + ", dtcDescription=" + this.getDtcDescription() + ", severity=" + this.getSeverity() + ", faultStatus=" + this.getFaultStatus() + ", detectionTime=" + String.valueOf(this.getDetectionTime()) + ", createTime=" + String.valueOf(this.getCreateTime()) + ")";
    }
}

