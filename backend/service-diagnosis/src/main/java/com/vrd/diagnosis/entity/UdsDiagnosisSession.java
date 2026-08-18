package com.vrd.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName(value="uds_diagnosis_session")
public class UdsDiagnosisSession {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String traceId;
    private String vin;
    private Long vehicleId;
    private String ecuType;
    private Integer serviceId;
    private Integer subFunction;
    private String requestData;
    private String responseData;
    private Integer success;
    private Integer negativeResponseCode;
    private String sessionStatus;
    private Long responseTimeMs;
    private String operator;
    private String remark;
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;
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

    public Integer getServiceId() {
        return this.serviceId;
    }

    public Integer getSubFunction() {
        return this.subFunction;
    }

    public String getRequestData() {
        return this.requestData;
    }

    public String getResponseData() {
        return this.responseData;
    }

    public Integer getSuccess() {
        return this.success;
    }

    public Integer getNegativeResponseCode() {
        return this.negativeResponseCode;
    }

    public String getSessionStatus() {
        return this.sessionStatus;
    }

    public Long getResponseTimeMs() {
        return this.responseTimeMs;
    }

    public String getOperator() {
        return this.operator;
    }

    public String getRemark() {
        return this.remark;
    }

    public LocalDateTime getRequestTime() {
        return this.requestTime;
    }

    public LocalDateTime getResponseTime() {
        return this.responseTime;
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

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public void setSubFunction(Integer subFunction) {
        this.subFunction = subFunction;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public void setNegativeResponseCode(Integer negativeResponseCode) {
        this.negativeResponseCode = negativeResponseCode;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public void setResponseTime(LocalDateTime responseTime) {
        this.responseTime = responseTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UdsDiagnosisSession)) {
            return false;
        }
        UdsDiagnosisSession other = (UdsDiagnosisSession)o;
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
        Integer this$serviceId = this.getServiceId();
        Integer other$serviceId = other.getServiceId();
        if (this$serviceId == null ? other$serviceId != null : !((Object)this$serviceId).equals(other$serviceId)) {
            return false;
        }
        Integer this$subFunction = this.getSubFunction();
        Integer other$subFunction = other.getSubFunction();
        if (this$subFunction == null ? other$subFunction != null : !((Object)this$subFunction).equals(other$subFunction)) {
            return false;
        }
        Integer this$success = this.getSuccess();
        Integer other$success = other.getSuccess();
        if (this$success == null ? other$success != null : !((Object)this$success).equals(other$success)) {
            return false;
        }
        Integer this$negativeResponseCode = this.getNegativeResponseCode();
        Integer other$negativeResponseCode = other.getNegativeResponseCode();
        if (this$negativeResponseCode == null ? other$negativeResponseCode != null : !((Object)this$negativeResponseCode).equals(other$negativeResponseCode)) {
            return false;
        }
        Long this$responseTimeMs = this.getResponseTimeMs();
        Long other$responseTimeMs = other.getResponseTimeMs();
        if (this$responseTimeMs == null ? other$responseTimeMs != null : !((Object)this$responseTimeMs).equals(other$responseTimeMs)) {
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
        String this$requestData = this.getRequestData();
        String other$requestData = other.getRequestData();
        if (this$requestData == null ? other$requestData != null : !this$requestData.equals(other$requestData)) {
            return false;
        }
        String this$responseData = this.getResponseData();
        String other$responseData = other.getResponseData();
        if (this$responseData == null ? other$responseData != null : !this$responseData.equals(other$responseData)) {
            return false;
        }
        String this$sessionStatus = this.getSessionStatus();
        String other$sessionStatus = other.getSessionStatus();
        if (this$sessionStatus == null ? other$sessionStatus != null : !this$sessionStatus.equals(other$sessionStatus)) {
            return false;
        }
        String this$operator = this.getOperator();
        String other$operator = other.getOperator();
        if (this$operator == null ? other$operator != null : !this$operator.equals(other$operator)) {
            return false;
        }
        String this$remark = this.getRemark();
        String other$remark = other.getRemark();
        if (this$remark == null ? other$remark != null : !this$remark.equals(other$remark)) {
            return false;
        }
        LocalDateTime this$requestTime = this.getRequestTime();
        LocalDateTime other$requestTime = other.getRequestTime();
        if (this$requestTime == null ? other$requestTime != null : !((Object)this$requestTime).equals(other$requestTime)) {
            return false;
        }
        LocalDateTime this$responseTime = this.getResponseTime();
        LocalDateTime other$responseTime = other.getResponseTime();
        if (this$responseTime == null ? other$responseTime != null : !((Object)this$responseTime).equals(other$responseTime)) {
            return false;
        }
        LocalDateTime this$createTime = this.getCreateTime();
        LocalDateTime other$createTime = other.getCreateTime();
        return !(this$createTime == null ? other$createTime != null : !((Object)this$createTime).equals(other$createTime));
    }

    protected boolean canEqual(Object other) {
        return other instanceof UdsDiagnosisSession;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $vehicleId = this.getVehicleId();
        result = result * 59 + ($vehicleId == null ? 43 : ((Object)$vehicleId).hashCode());
        Integer $serviceId = this.getServiceId();
        result = result * 59 + ($serviceId == null ? 43 : ((Object)$serviceId).hashCode());
        Integer $subFunction = this.getSubFunction();
        result = result * 59 + ($subFunction == null ? 43 : ((Object)$subFunction).hashCode());
        Integer $success = this.getSuccess();
        result = result * 59 + ($success == null ? 43 : ((Object)$success).hashCode());
        Integer $negativeResponseCode = this.getNegativeResponseCode();
        result = result * 59 + ($negativeResponseCode == null ? 43 : ((Object)$negativeResponseCode).hashCode());
        Long $responseTimeMs = this.getResponseTimeMs();
        result = result * 59 + ($responseTimeMs == null ? 43 : ((Object)$responseTimeMs).hashCode());
        String $traceId = this.getTraceId();
        result = result * 59 + ($traceId == null ? 43 : $traceId.hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $ecuType = this.getEcuType();
        result = result * 59 + ($ecuType == null ? 43 : $ecuType.hashCode());
        String $requestData = this.getRequestData();
        result = result * 59 + ($requestData == null ? 43 : $requestData.hashCode());
        String $responseData = this.getResponseData();
        result = result * 59 + ($responseData == null ? 43 : $responseData.hashCode());
        String $sessionStatus = this.getSessionStatus();
        result = result * 59 + ($sessionStatus == null ? 43 : $sessionStatus.hashCode());
        String $operator = this.getOperator();
        result = result * 59 + ($operator == null ? 43 : $operator.hashCode());
        String $remark = this.getRemark();
        result = result * 59 + ($remark == null ? 43 : $remark.hashCode());
        LocalDateTime $requestTime = this.getRequestTime();
        result = result * 59 + ($requestTime == null ? 43 : ((Object)$requestTime).hashCode());
        LocalDateTime $responseTime = this.getResponseTime();
        result = result * 59 + ($responseTime == null ? 43 : ((Object)$responseTime).hashCode());
        LocalDateTime $createTime = this.getCreateTime();
        result = result * 59 + ($createTime == null ? 43 : ((Object)$createTime).hashCode());
        return result;
    }

    public String toString() {
        return "UdsDiagnosisSession(id=" + this.getId() + ", traceId=" + this.getTraceId() + ", vin=" + this.getVin() + ", vehicleId=" + this.getVehicleId() + ", ecuType=" + this.getEcuType() + ", serviceId=" + this.getServiceId() + ", subFunction=" + this.getSubFunction() + ", requestData=" + this.getRequestData() + ", responseData=" + this.getResponseData() + ", success=" + this.getSuccess() + ", negativeResponseCode=" + this.getNegativeResponseCode() + ", sessionStatus=" + this.getSessionStatus() + ", responseTimeMs=" + this.getResponseTimeMs() + ", operator=" + this.getOperator() + ", remark=" + this.getRemark() + ", requestTime=" + String.valueOf(this.getRequestTime()) + ", responseTime=" + String.valueOf(this.getResponseTime()) + ", createTime=" + String.valueOf(this.getCreateTime()) + ")";
    }
}

