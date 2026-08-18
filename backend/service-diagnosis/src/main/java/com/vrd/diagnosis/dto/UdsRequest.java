package com.vrd.diagnosis.dto;

public class UdsRequest {
    private String vin;
    private Long vehicleId;
    private String ecuType;
    private Integer serviceId;
    private Integer subFunction;
    private Integer dataIdentifier;
    private String requestData;
    private Integer sessionType;
    private Integer securityLevel;
    private String securityKey;
    private Integer resetType;
    private Integer dtcStatusMask;
    private Integer routineId;
    private Long memoryAddress;
    private Integer memorySize;
    private Long timeoutMs;
    private String operator;

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

    public Integer getDataIdentifier() {
        return this.dataIdentifier;
    }

    public String getRequestData() {
        return this.requestData;
    }

    public Integer getSessionType() {
        return this.sessionType;
    }

    public Integer getSecurityLevel() {
        return this.securityLevel;
    }

    public String getSecurityKey() {
        return this.securityKey;
    }

    public Integer getResetType() {
        return this.resetType;
    }

    public Integer getDtcStatusMask() {
        return this.dtcStatusMask;
    }

    public Integer getRoutineId() {
        return this.routineId;
    }

    public Long getMemoryAddress() {
        return this.memoryAddress;
    }

    public Integer getMemorySize() {
        return this.memorySize;
    }

    public Long getTimeoutMs() {
        return this.timeoutMs;
    }

    public String getOperator() {
        return this.operator;
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

    public void setDataIdentifier(Integer dataIdentifier) {
        this.dataIdentifier = dataIdentifier;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public void setSessionType(Integer sessionType) {
        this.sessionType = sessionType;
    }

    public void setSecurityLevel(Integer securityLevel) {
        this.securityLevel = securityLevel;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    public void setResetType(Integer resetType) {
        this.resetType = resetType;
    }

    public void setDtcStatusMask(Integer dtcStatusMask) {
        this.dtcStatusMask = dtcStatusMask;
    }

    public void setRoutineId(Integer routineId) {
        this.routineId = routineId;
    }

    public void setMemoryAddress(Long memoryAddress) {
        this.memoryAddress = memoryAddress;
    }

    public void setMemorySize(Integer memorySize) {
        this.memorySize = memorySize;
    }

    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UdsRequest)) {
            return false;
        }
        UdsRequest other = (UdsRequest)o;
        if (!other.canEqual(this)) {
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
        Integer this$dataIdentifier = this.getDataIdentifier();
        Integer other$dataIdentifier = other.getDataIdentifier();
        if (this$dataIdentifier == null ? other$dataIdentifier != null : !((Object)this$dataIdentifier).equals(other$dataIdentifier)) {
            return false;
        }
        Integer this$sessionType = this.getSessionType();
        Integer other$sessionType = other.getSessionType();
        if (this$sessionType == null ? other$sessionType != null : !((Object)this$sessionType).equals(other$sessionType)) {
            return false;
        }
        Integer this$securityLevel = this.getSecurityLevel();
        Integer other$securityLevel = other.getSecurityLevel();
        if (this$securityLevel == null ? other$securityLevel != null : !((Object)this$securityLevel).equals(other$securityLevel)) {
            return false;
        }
        Integer this$resetType = this.getResetType();
        Integer other$resetType = other.getResetType();
        if (this$resetType == null ? other$resetType != null : !((Object)this$resetType).equals(other$resetType)) {
            return false;
        }
        Integer this$dtcStatusMask = this.getDtcStatusMask();
        Integer other$dtcStatusMask = other.getDtcStatusMask();
        if (this$dtcStatusMask == null ? other$dtcStatusMask != null : !((Object)this$dtcStatusMask).equals(other$dtcStatusMask)) {
            return false;
        }
        Integer this$routineId = this.getRoutineId();
        Integer other$routineId = other.getRoutineId();
        if (this$routineId == null ? other$routineId != null : !((Object)this$routineId).equals(other$routineId)) {
            return false;
        }
        Long this$memoryAddress = this.getMemoryAddress();
        Long other$memoryAddress = other.getMemoryAddress();
        if (this$memoryAddress == null ? other$memoryAddress != null : !((Object)this$memoryAddress).equals(other$memoryAddress)) {
            return false;
        }
        Integer this$memorySize = this.getMemorySize();
        Integer other$memorySize = other.getMemorySize();
        if (this$memorySize == null ? other$memorySize != null : !((Object)this$memorySize).equals(other$memorySize)) {
            return false;
        }
        Long this$timeoutMs = this.getTimeoutMs();
        Long other$timeoutMs = other.getTimeoutMs();
        if (this$timeoutMs == null ? other$timeoutMs != null : !((Object)this$timeoutMs).equals(other$timeoutMs)) {
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
        String this$securityKey = this.getSecurityKey();
        String other$securityKey = other.getSecurityKey();
        if (this$securityKey == null ? other$securityKey != null : !this$securityKey.equals(other$securityKey)) {
            return false;
        }
        String this$operator = this.getOperator();
        String other$operator = other.getOperator();
        return !(this$operator == null ? other$operator != null : !this$operator.equals(other$operator));
    }

    protected boolean canEqual(Object other) {
        return other instanceof UdsRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $vehicleId = this.getVehicleId();
        result = result * 59 + ($vehicleId == null ? 43 : ((Object)$vehicleId).hashCode());
        Integer $serviceId = this.getServiceId();
        result = result * 59 + ($serviceId == null ? 43 : ((Object)$serviceId).hashCode());
        Integer $subFunction = this.getSubFunction();
        result = result * 59 + ($subFunction == null ? 43 : ((Object)$subFunction).hashCode());
        Integer $dataIdentifier = this.getDataIdentifier();
        result = result * 59 + ($dataIdentifier == null ? 43 : ((Object)$dataIdentifier).hashCode());
        Integer $sessionType = this.getSessionType();
        result = result * 59 + ($sessionType == null ? 43 : ((Object)$sessionType).hashCode());
        Integer $securityLevel = this.getSecurityLevel();
        result = result * 59 + ($securityLevel == null ? 43 : ((Object)$securityLevel).hashCode());
        Integer $resetType = this.getResetType();
        result = result * 59 + ($resetType == null ? 43 : ((Object)$resetType).hashCode());
        Integer $dtcStatusMask = this.getDtcStatusMask();
        result = result * 59 + ($dtcStatusMask == null ? 43 : ((Object)$dtcStatusMask).hashCode());
        Integer $routineId = this.getRoutineId();
        result = result * 59 + ($routineId == null ? 43 : ((Object)$routineId).hashCode());
        Long $memoryAddress = this.getMemoryAddress();
        result = result * 59 + ($memoryAddress == null ? 43 : ((Object)$memoryAddress).hashCode());
        Integer $memorySize = this.getMemorySize();
        result = result * 59 + ($memorySize == null ? 43 : ((Object)$memorySize).hashCode());
        Long $timeoutMs = this.getTimeoutMs();
        result = result * 59 + ($timeoutMs == null ? 43 : ((Object)$timeoutMs).hashCode());
        String $vin = this.getVin();
        result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
        String $ecuType = this.getEcuType();
        result = result * 59 + ($ecuType == null ? 43 : $ecuType.hashCode());
        String $requestData = this.getRequestData();
        result = result * 59 + ($requestData == null ? 43 : $requestData.hashCode());
        String $securityKey = this.getSecurityKey();
        result = result * 59 + ($securityKey == null ? 43 : $securityKey.hashCode());
        String $operator = this.getOperator();
        result = result * 59 + ($operator == null ? 43 : $operator.hashCode());
        return result;
    }

    public String toString() {
        return "UdsRequest(vin=" + this.getVin() + ", vehicleId=" + this.getVehicleId() + ", ecuType=" + this.getEcuType() + ", serviceId=" + this.getServiceId() + ", subFunction=" + this.getSubFunction() + ", dataIdentifier=" + this.getDataIdentifier() + ", requestData=" + this.getRequestData() + ", sessionType=" + this.getSessionType() + ", securityLevel=" + this.getSecurityLevel() + ", securityKey=" + this.getSecurityKey() + ", resetType=" + this.getResetType() + ", dtcStatusMask=" + this.getDtcStatusMask() + ", routineId=" + this.getRoutineId() + ", memoryAddress=" + this.getMemoryAddress() + ", memorySize=" + this.getMemorySize() + ", timeoutMs=" + this.getTimeoutMs() + ", operator=" + this.getOperator() + ")";
    }
}

