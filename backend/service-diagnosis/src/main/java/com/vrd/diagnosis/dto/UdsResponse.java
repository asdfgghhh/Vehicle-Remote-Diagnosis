package com.vrd.diagnosis.dto;

import java.time.LocalDateTime;

public class UdsResponse {
    private String traceId;
    private String vin;
    private Integer serviceId;
    private Boolean success;
    private Boolean negativeResponse;
    private Integer negativeResponseCode;
    private String negativeResponseDesc;
    private String responseData;
    private Object parsedData;
    private String sessionStatus;
    private String securityStatus;
    private Long responseTimeMs;
    private LocalDateTime timestamp;

    public static UdsResponse success(String traceId, String vin, int serviceId, String responseData, long responseTimeMs) {
        UdsResponse resp = new UdsResponse();
        resp.setTraceId(traceId);
        resp.setVin(vin);
        resp.setServiceId(serviceId);
        resp.setSuccess(true);
        resp.setNegativeResponse(false);
        resp.setResponseData(responseData);
        resp.setResponseTimeMs(responseTimeMs);
        resp.setTimestamp(LocalDateTime.now());
        return resp;
    }

    public static UdsResponse negativeResponse(String traceId, String vin, int serviceId, int nrc, String desc, long responseTimeMs) {
        UdsResponse resp = new UdsResponse();
        resp.setTraceId(traceId);
        resp.setVin(vin);
        resp.setServiceId(serviceId);
        resp.setSuccess(false);
        resp.setNegativeResponse(true);
        resp.setNegativeResponseCode(nrc);
        resp.setNegativeResponseDesc(desc);
        resp.setResponseTimeMs(responseTimeMs);
        resp.setTimestamp(LocalDateTime.now());
        return resp;
    }

    public static UdsResponse error(String traceId, String vin, int serviceId, String message) {
        UdsResponse resp = new UdsResponse();
        resp.setTraceId(traceId);
        resp.setVin(vin);
        resp.setServiceId(serviceId);
        resp.setSuccess(false);
        resp.setNegativeResponse(false);
        resp.setNegativeResponseDesc(message);
        resp.setTimestamp(LocalDateTime.now());
        return resp;
    }

    public String getTraceId() {
        return this.traceId;
    }

    public String getVin() {
        return this.vin;
    }

    public Integer getServiceId() {
        return this.serviceId;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public Boolean getNegativeResponse() {
        return this.negativeResponse;
    }

    public Integer getNegativeResponseCode() {
        return this.negativeResponseCode;
    }

    public String getNegativeResponseDesc() {
        return this.negativeResponseDesc;
    }

    public String getResponseData() {
        return this.responseData;
    }

    public Object getParsedData() {
        return this.parsedData;
    }

    public String getSessionStatus() {
        return this.sessionStatus;
    }

    public String getSecurityStatus() {
        return this.securityStatus;
    }

    public Long getResponseTimeMs() {
        return this.responseTimeMs;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setNegativeResponse(Boolean negativeResponse) {
        this.negativeResponse = negativeResponse;
    }

    public void setNegativeResponseCode(Integer negativeResponseCode) {
        this.negativeResponseCode = negativeResponseCode;
    }

    public void setNegativeResponseDesc(String negativeResponseDesc) {
        this.negativeResponseDesc = negativeResponseDesc;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public void setParsedData(Object parsedData) {
        this.parsedData = parsedData;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public void setSecurityStatus(String securityStatus) {
        this.securityStatus = securityStatus;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UdsResponse)) {
            return false;
        }
        UdsResponse other = (UdsResponse)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Integer this$serviceId = this.getServiceId();
        Integer other$serviceId = other.getServiceId();
        if (this$serviceId == null ? other$serviceId != null : !((Object)this$serviceId).equals(other$serviceId)) {
            return false;
        }
        Boolean this$success = this.getSuccess();
        Boolean other$success = other.getSuccess();
        if (this$success == null ? other$success != null : !((Object)this$success).equals(other$success)) {
            return false;
        }
        Boolean this$negativeResponse = this.getNegativeResponse();
        Boolean other$negativeResponse = other.getNegativeResponse();
        if (this$negativeResponse == null ? other$negativeResponse != null : !((Object)this$negativeResponse).equals(other$negativeResponse)) {
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
        String this$negativeResponseDesc = this.getNegativeResponseDesc();
        String other$negativeResponseDesc = other.getNegativeResponseDesc();
        if (this$negativeResponseDesc == null ? other$negativeResponseDesc != null : !this$negativeResponseDesc.equals(other$negativeResponseDesc)) {
            return false;
        }
        String this$responseData = this.getResponseData();
        String other$responseData = other.getResponseData();
        if (this$responseData == null ? other$responseData != null : !this$responseData.equals(other$responseData)) {
            return false;
        }
        Object this$parsedData = this.getParsedData();
        Object other$parsedData = other.getParsedData();
        if (this$parsedData == null ? other$parsedData != null : !this$parsedData.equals(other$parsedData)) {
            return false;
        }
        String this$sessionStatus = this.getSessionStatus();
        String other$sessionStatus = other.getSessionStatus();
        if (this$sessionStatus == null ? other$sessionStatus != null : !this$sessionStatus.equals(other$sessionStatus)) {
            return false;
        }
        String this$securityStatus = this.getSecurityStatus();
        String other$securityStatus = other.getSecurityStatus();
        if (this$securityStatus == null ? other$securityStatus != null : !this$securityStatus.equals(other$securityStatus)) {
            return false;
        }
        LocalDateTime this$timestamp = this.getTimestamp();
        LocalDateTime other$timestamp = other.getTimestamp();
        return !(this$timestamp == null ? other$timestamp != null : !((Object)this$timestamp).equals(other$timestamp));
    }

    protected boolean canEqual(Object other) {
        return other instanceof UdsResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Integer $serviceId = this.getServiceId();
        result = result * 59 + ($serviceId == null ? 43 : ((Object)$serviceId).hashCode());
        Boolean $success = this.getSuccess();
        result = result * 59 + ($success == null ? 43 : ((Object)$success).hashCode());
        Boolean $negativeResponse = this.getNegativeResponse();
        result = result * 59 + ($negativeResponse == null ? 43 : ((Object)$negativeResponse).hashCode());
        Integer $negativeResponseCode = this.getNegativeResponseCode();
        result = result * 59 + ($negativeResponseCode == null ? 43 : ((Object)$negativeResponseCode).hashCode());
        Long $responseTimeMs = this.getResponseTimeMs();
        result = result * 59 + ($responseTimeMs == null ? 43 : ((Object)$responseTimeMs).hashCode());
        String $traceId = this.getTraceId();
        result = result * 59 + ($traceId == null ? 43 : $traceId.hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $negativeResponseDesc = this.getNegativeResponseDesc();
        result = result * 59 + ($negativeResponseDesc == null ? 43 : $negativeResponseDesc.hashCode());
        String $responseData = this.getResponseData();
        result = result * 59 + ($responseData == null ? 43 : $responseData.hashCode());
        Object $parsedData = this.getParsedData();
        result = result * 59 + ($parsedData == null ? 43 : $parsedData.hashCode());
        String $sessionStatus = this.getSessionStatus();
        result = result * 59 + ($sessionStatus == null ? 43 : $sessionStatus.hashCode());
        String $securityStatus = this.getSecurityStatus();
        result = result * 59 + ($securityStatus == null ? 43 : $securityStatus.hashCode());
        LocalDateTime $timestamp = this.getTimestamp();
        result = result * 59 + ($timestamp == null ? 43 : ((Object)$timestamp).hashCode());
        return result;
    }

    public String toString() {
        return "UdsResponse(traceId=" + this.getTraceId() + ", vin=" + this.getVin() + ", serviceId=" + this.getServiceId() + ", success=" + this.getSuccess() + ", negativeResponse=" + this.getNegativeResponse() + ", negativeResponseCode=" + this.getNegativeResponseCode() + ", negativeResponseDesc=" + this.getNegativeResponseDesc() + ", responseData=" + this.getResponseData() + ", parsedData=" + String.valueOf(this.getParsedData()) + ", sessionStatus=" + this.getSessionStatus() + ", securityStatus=" + this.getSecurityStatus() + ", responseTimeMs=" + this.getResponseTimeMs() + ", timestamp=" + String.valueOf(this.getTimestamp()) + ")";
    }
}

