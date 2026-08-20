/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.vehicle.dto;

import java.util.List;

public class VehicleDashboardStatsVO {
    private Long connectedModelCount;
    private Long totalVehicles;
    private Long onlineVehicles;
    private Long totalAlertCount;
    private List<ModelVehicleStat> modelStats;
    private List<AlertByComponent> alertByComponent;
    private List<RecentAlert> recentAlerts;
    private Long totalFaultCount;
    private List<FaultByCode> faultByCode;
    private Integer fleetHealthScore;
    private List<DomainHealthStat> domainHealth;

    public Integer getFleetHealthScore() {
        return this.fleetHealthScore;
    }

    public List<DomainHealthStat> getDomainHealth() {
        return this.domainHealth;
    }

    public Long getConnectedModelCount() {
        return this.connectedModelCount;
    }

    public Long getTotalVehicles() {
        return this.totalVehicles;
    }

    public Long getOnlineVehicles() {
        return this.onlineVehicles;
    }

    public Long getTotalAlertCount() {
        return this.totalAlertCount;
    }

    public List<ModelVehicleStat> getModelStats() {
        return this.modelStats;
    }

    public List<AlertByComponent> getAlertByComponent() {
        return this.alertByComponent;
    }

    public List<RecentAlert> getRecentAlerts() {
        return this.recentAlerts;
    }

    public Long getTotalFaultCount() {
        return this.totalFaultCount;
    }

    public List<FaultByCode> getFaultByCode() {
        return this.faultByCode;
    }

    public void setConnectedModelCount(Long connectedModelCount) {
        this.connectedModelCount = connectedModelCount;
    }

    public void setTotalVehicles(Long totalVehicles) {
        this.totalVehicles = totalVehicles;
    }

    public void setOnlineVehicles(Long onlineVehicles) {
        this.onlineVehicles = onlineVehicles;
    }

    public void setTotalAlertCount(Long totalAlertCount) {
        this.totalAlertCount = totalAlertCount;
    }

    public void setModelStats(List<ModelVehicleStat> modelStats) {
        this.modelStats = modelStats;
    }

    public void setAlertByComponent(List<AlertByComponent> alertByComponent) {
        this.alertByComponent = alertByComponent;
    }

    public void setRecentAlerts(List<RecentAlert> recentAlerts) {
        this.recentAlerts = recentAlerts;
    }

    public void setTotalFaultCount(Long totalFaultCount) {
        this.totalFaultCount = totalFaultCount;
    }

    public void setFaultByCode(List<FaultByCode> faultByCode) {
        this.faultByCode = faultByCode;
    }

    public void setFleetHealthScore(Integer fleetHealthScore) {
        this.fleetHealthScore = fleetHealthScore;
    }

    public void setDomainHealth(List<DomainHealthStat> domainHealth) {
        this.domainHealth = domainHealth;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VehicleDashboardStatsVO)) {
            return false;
        }
        VehicleDashboardStatsVO other = (VehicleDashboardStatsVO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$connectedModelCount = this.getConnectedModelCount();
        Long other$connectedModelCount = other.getConnectedModelCount();
        if (this$connectedModelCount == null ? other$connectedModelCount != null : !((Object)this$connectedModelCount).equals(other$connectedModelCount)) {
            return false;
        }
        Long this$totalVehicles = this.getTotalVehicles();
        Long other$totalVehicles = other.getTotalVehicles();
        if (this$totalVehicles == null ? other$totalVehicles != null : !((Object)this$totalVehicles).equals(other$totalVehicles)) {
            return false;
        }
        Long this$onlineVehicles = this.getOnlineVehicles();
        Long other$onlineVehicles = other.getOnlineVehicles();
        if (this$onlineVehicles == null ? other$onlineVehicles != null : !((Object)this$onlineVehicles).equals(other$onlineVehicles)) {
            return false;
        }
        Long this$totalAlertCount = this.getTotalAlertCount();
        Long other$totalAlertCount = other.getTotalAlertCount();
        if (this$totalAlertCount == null ? other$totalAlertCount != null : !((Object)this$totalAlertCount).equals(other$totalAlertCount)) {
            return false;
        }
        Long this$totalFaultCount = this.getTotalFaultCount();
        Long other$totalFaultCount = other.getTotalFaultCount();
        if (this$totalFaultCount == null ? other$totalFaultCount != null : !((Object)this$totalFaultCount).equals(other$totalFaultCount)) {
            return false;
        }
        List<ModelVehicleStat> this$modelStats = this.getModelStats();
        List<ModelVehicleStat> other$modelStats = other.getModelStats();
        if (this$modelStats == null ? other$modelStats != null : !((Object)this$modelStats).equals(other$modelStats)) {
            return false;
        }
        List<AlertByComponent> this$alertByComponent = this.getAlertByComponent();
        List<AlertByComponent> other$alertByComponent = other.getAlertByComponent();
        if (this$alertByComponent == null ? other$alertByComponent != null : !((Object)this$alertByComponent).equals(other$alertByComponent)) {
            return false;
        }
        List<RecentAlert> this$recentAlerts = this.getRecentAlerts();
        List<RecentAlert> other$recentAlerts = other.getRecentAlerts();
        if (this$recentAlerts == null ? other$recentAlerts != null : !((Object)this$recentAlerts).equals(other$recentAlerts)) {
            return false;
        }
        List<FaultByCode> this$faultByCode = this.getFaultByCode();
        List<FaultByCode> other$faultByCode = other.getFaultByCode();
        if (this$faultByCode == null ? other$faultByCode != null : !((Object)this$faultByCode).equals(other$faultByCode)) {
            return false;
        }
        Integer this$fleetHealthScore = this.getFleetHealthScore();
        Integer other$fleetHealthScore = other.getFleetHealthScore();
        if (this$fleetHealthScore == null ? other$fleetHealthScore != null : !((Object)this$fleetHealthScore).equals(other$fleetHealthScore)) {
            return false;
        }
        List<DomainHealthStat> this$domainHealth = this.getDomainHealth();
        List<DomainHealthStat> other$domainHealth = other.getDomainHealth();
        return !(this$domainHealth == null ? other$domainHealth != null : !((Object)this$domainHealth).equals(other$domainHealth));
    }

    protected boolean canEqual(Object other) {
        return other instanceof VehicleDashboardStatsVO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $connectedModelCount = this.getConnectedModelCount();
        result = result * 59 + ($connectedModelCount == null ? 43 : ((Object)$connectedModelCount).hashCode());
        Long $totalVehicles = this.getTotalVehicles();
        result = result * 59 + ($totalVehicles == null ? 43 : ((Object)$totalVehicles).hashCode());
        Long $onlineVehicles = this.getOnlineVehicles();
        result = result * 59 + ($onlineVehicles == null ? 43 : ((Object)$onlineVehicles).hashCode());
        Long $totalAlertCount = this.getTotalAlertCount();
        result = result * 59 + ($totalAlertCount == null ? 43 : ((Object)$totalAlertCount).hashCode());
        Long $totalFaultCount = this.getTotalFaultCount();
        result = result * 59 + ($totalFaultCount == null ? 43 : ((Object)$totalFaultCount).hashCode());
        List<ModelVehicleStat> $modelStats = this.getModelStats();
        result = result * 59 + ($modelStats == null ? 43 : ((Object)$modelStats).hashCode());
        List<AlertByComponent> $alertByComponent = this.getAlertByComponent();
        result = result * 59 + ($alertByComponent == null ? 43 : ((Object)$alertByComponent).hashCode());
        List<RecentAlert> $recentAlerts = this.getRecentAlerts();
        result = result * 59 + ($recentAlerts == null ? 43 : ((Object)$recentAlerts).hashCode());
        List<FaultByCode> $faultByCode = this.getFaultByCode();
        result = result * 59 + ($faultByCode == null ? 43 : ((Object)$faultByCode).hashCode());
        Integer $fleetHealthScore = this.getFleetHealthScore();
        result = result * 59 + ($fleetHealthScore == null ? 43 : ((Object)$fleetHealthScore).hashCode());
        List<DomainHealthStat> $domainHealth = this.getDomainHealth();
        result = result * 59 + ($domainHealth == null ? 43 : ((Object)$domainHealth).hashCode());
        return result;
    }

    public String toString() {
        return "VehicleDashboardStatsVO(connectedModelCount=" + this.getConnectedModelCount() + ", totalVehicles=" + this.getTotalVehicles() + ", onlineVehicles=" + this.getOnlineVehicles() + ", totalAlertCount=" + this.getTotalAlertCount() + ", modelStats=" + String.valueOf(this.getModelStats()) + ", alertByComponent=" + String.valueOf(this.getAlertByComponent()) + ", recentAlerts=" + String.valueOf(this.getRecentAlerts()) + ", totalFaultCount=" + this.getTotalFaultCount() + ", faultByCode=" + String.valueOf(this.getFaultByCode()) + ", fleetHealthScore=" + this.getFleetHealthScore() + ", domainHealth=" + String.valueOf(this.getDomainHealth()) + ")";
    }

    public static class ModelVehicleStat {
        private Long modelId;
        private String modelName;
        private String modelCode;
        private Long vehicleCount;

        public Long getModelId() {
            return this.modelId;
        }

        public String getModelName() {
            return this.modelName;
        }

        public String getModelCode() {
            return this.modelCode;
        }

        public Long getVehicleCount() {
            return this.vehicleCount;
        }

        public void setModelId(Long modelId) {
            this.modelId = modelId;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public void setModelCode(String modelCode) {
            this.modelCode = modelCode;
        }

        public void setVehicleCount(Long vehicleCount) {
            this.vehicleCount = vehicleCount;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ModelVehicleStat)) {
                return false;
            }
            ModelVehicleStat other = (ModelVehicleStat)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Long this$modelId = this.getModelId();
            Long other$modelId = other.getModelId();
            if (this$modelId == null ? other$modelId != null : !((Object)this$modelId).equals(other$modelId)) {
                return false;
            }
            Long this$vehicleCount = this.getVehicleCount();
            Long other$vehicleCount = other.getVehicleCount();
            if (this$vehicleCount == null ? other$vehicleCount != null : !((Object)this$vehicleCount).equals(other$vehicleCount)) {
                return false;
            }
            String this$modelName = this.getModelName();
            String other$modelName = other.getModelName();
            if (this$modelName == null ? other$modelName != null : !this$modelName.equals(other$modelName)) {
                return false;
            }
            String this$modelCode = this.getModelCode();
            String other$modelCode = other.getModelCode();
            return !(this$modelCode == null ? other$modelCode != null : !this$modelCode.equals(other$modelCode));
        }

        protected boolean canEqual(Object other) {
            return other instanceof ModelVehicleStat;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Long $modelId = this.getModelId();
            result = result * 59 + ($modelId == null ? 43 : ((Object)$modelId).hashCode());
            Long $vehicleCount = this.getVehicleCount();
            result = result * 59 + ($vehicleCount == null ? 43 : ((Object)$vehicleCount).hashCode());
            String $modelName = this.getModelName();
            result = result * 59 + ($modelName == null ? 43 : $modelName.hashCode());
            String $modelCode = this.getModelCode();
            result = result * 59 + ($modelCode == null ? 43 : $modelCode.hashCode());
            return result;
        }

        public String toString() {
            return "VehicleDashboardStatsVO.ModelVehicleStat(modelId=" + this.getModelId() + ", modelName=" + this.getModelName() + ", modelCode=" + this.getModelCode() + ", vehicleCount=" + this.getVehicleCount() + ")";
        }
    }

    public static class RecentAlert {
        private String time;
        private String vin;
        private String componentCode;
        private String type;
        private String message;
        private String status;

        public String getTime() {
            return this.time;
        }

        public String getVin() {
            return this.vin;
        }

        public String getComponentCode() {
            return this.componentCode;
        }

        public String getType() {
            return this.type;
        }

        public String getMessage() {
            return this.message;
        }

        public String getStatus() {
            return this.status;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public void setVin(String vin) {
            this.vin = vin;
        }

        public void setComponentCode(String componentCode) {
            this.componentCode = componentCode;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof RecentAlert)) {
                return false;
            }
            RecentAlert other = (RecentAlert)o;
            if (!other.canEqual(this)) {
                return false;
            }
            String this$time = this.getTime();
            String other$time = other.getTime();
            if (this$time == null ? other$time != null : !this$time.equals(other$time)) {
                return false;
            }
            String this$vin = this.getVin();
            String other$vin = other.getVin();
            if (this$vin == null ? other$vin != null : !this$vin.equals(other$vin)) {
                return false;
            }
            String this$componentCode = this.getComponentCode();
            String other$componentCode = other.getComponentCode();
            if (this$componentCode == null ? other$componentCode != null : !this$componentCode.equals(other$componentCode)) {
                return false;
            }
            String this$type = this.getType();
            String other$type = other.getType();
            if (this$type == null ? other$type != null : !this$type.equals(other$type)) {
                return false;
            }
            String this$message = this.getMessage();
            String other$message = other.getMessage();
            if (this$message == null ? other$message != null : !this$message.equals(other$message)) {
                return false;
            }
            String this$status = this.getStatus();
            String other$status = other.getStatus();
            return !(this$status == null ? other$status != null : !this$status.equals(other$status));
        }

        protected boolean canEqual(Object other) {
            return other instanceof RecentAlert;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            String $time = this.getTime();
            result = result * 59 + ($time == null ? 43 : $time.hashCode());
            String $vin = this.getVin();
            result = result * 59 + ($vin == null ? 43 : $vin.hashCode());
            String $componentCode = this.getComponentCode();
            result = result * 59 + ($componentCode == null ? 43 : $componentCode.hashCode());
            String $type = this.getType();
            result = result * 59 + ($type == null ? 43 : $type.hashCode());
            String $message = this.getMessage();
            result = result * 59 + ($message == null ? 43 : $message.hashCode());
            String $status = this.getStatus();
            result = result * 59 + ($status == null ? 43 : $status.hashCode());
            return result;
        }

        public String toString() {
            return "VehicleDashboardStatsVO.RecentAlert(time=" + this.getTime() + ", vin=" + this.getVin() + ", componentCode=" + this.getComponentCode() + ", type=" + this.getType() + ", message=" + this.getMessage() + ", status=" + this.getStatus() + ")";
        }
    }

    public static class AlertByComponent {
        private String componentCode;
        private Long alertCount;

        public String getComponentCode() {
            return this.componentCode;
        }

        public Long getAlertCount() {
            return this.alertCount;
        }

        public void setComponentCode(String componentCode) {
            this.componentCode = componentCode;
        }

        public void setAlertCount(Long alertCount) {
            this.alertCount = alertCount;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof AlertByComponent)) {
                return false;
            }
            AlertByComponent other = (AlertByComponent)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Long this$alertCount = this.getAlertCount();
            Long other$alertCount = other.getAlertCount();
            if (this$alertCount == null ? other$alertCount != null : !((Object)this$alertCount).equals(other$alertCount)) {
                return false;
            }
            String this$componentCode = this.getComponentCode();
            String other$componentCode = other.getComponentCode();
            return !(this$componentCode == null ? other$componentCode != null : !this$componentCode.equals(other$componentCode));
        }

        protected boolean canEqual(Object other) {
            return other instanceof AlertByComponent;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Long $alertCount = this.getAlertCount();
            result = result * 59 + ($alertCount == null ? 43 : ((Object)$alertCount).hashCode());
            String $componentCode = this.getComponentCode();
            result = result * 59 + ($componentCode == null ? 43 : $componentCode.hashCode());
            return result;
        }

        public String toString() {
            return "VehicleDashboardStatsVO.AlertByComponent(componentCode=" + this.getComponentCode() + ", alertCount=" + this.getAlertCount() + ")";
        }
    }

    public static class FaultByCode {
        private String faultCode;
        private String componentCode;
        private String faultName;
        private Long faultCount;

        public String getFaultCode() {
            return this.faultCode;
        }

        public String getComponentCode() {
            return this.componentCode;
        }

        public String getFaultName() {
            return this.faultName;
        }

        public Long getFaultCount() {
            return this.faultCount;
        }

        public void setFaultCode(String faultCode) {
            this.faultCode = faultCode;
        }

        public void setComponentCode(String componentCode) {
            this.componentCode = componentCode;
        }

        public void setFaultName(String faultName) {
            this.faultName = faultName;
        }

        public void setFaultCount(Long faultCount) {
            this.faultCount = faultCount;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof FaultByCode)) {
                return false;
            }
            FaultByCode other = (FaultByCode)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Long this$faultCount = this.getFaultCount();
            Long other$faultCount = other.getFaultCount();
            if (this$faultCount == null ? other$faultCount != null : !((Object)this$faultCount).equals(other$faultCount)) {
                return false;
            }
            String this$faultCode = this.getFaultCode();
            String other$faultCode = other.getFaultCode();
            if (this$faultCode == null ? other$faultCode != null : !this$faultCode.equals(other$faultCode)) {
                return false;
            }
            String this$componentCode = this.getComponentCode();
            String other$componentCode = other.getComponentCode();
            if (this$componentCode == null ? other$componentCode != null : !this$componentCode.equals(other$componentCode)) {
                return false;
            }
            String this$faultName = this.getFaultName();
            String other$faultName = other.getFaultName();
            return !(this$faultName == null ? other$faultName != null : !this$faultName.equals(other$faultName));
        }

        protected boolean canEqual(Object other) {
            return other instanceof FaultByCode;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Long $faultCount = this.getFaultCount();
            result = result * 59 + ($faultCount == null ? 43 : ((Object)$faultCount).hashCode());
            String $faultCode = this.getFaultCode();
            result = result * 59 + ($faultCode == null ? 43 : $faultCode.hashCode());
            String $componentCode = this.getComponentCode();
            result = result * 59 + ($componentCode == null ? 43 : $componentCode.hashCode());
            String $faultName = this.getFaultName();
            result = result * 59 + ($faultName == null ? 43 : $faultName.hashCode());
            return result;
        }

        public String toString() {
            return "VehicleDashboardStatsVO.FaultByCode(faultCode=" + this.getFaultCode() + ", componentCode=" + this.getComponentCode() + ", faultName=" + this.getFaultName() + ", faultCount=" + this.getFaultCount() + ")";
        }
    }

    public static class DomainHealthStat {
        private String domainCode;
        private String domainName;
        private Integer healthScore;
        private String status;

        public String getDomainCode() {
            return this.domainCode;
        }

        public String getDomainName() {
            return this.domainName;
        }

        public Integer getHealthScore() {
            return this.healthScore;
        }

        public String getStatus() {
            return this.status;
        }

        public void setDomainCode(String domainCode) {
            this.domainCode = domainCode;
        }

        public void setDomainName(String domainName) {
            this.domainName = domainName;
        }

        public void setHealthScore(Integer healthScore) {
            this.healthScore = healthScore;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof DomainHealthStat)) {
                return false;
            }
            DomainHealthStat other = (DomainHealthStat)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Integer this$healthScore = this.getHealthScore();
            Integer other$healthScore = other.getHealthScore();
            if (this$healthScore == null ? other$healthScore != null : !((Object)this$healthScore).equals(other$healthScore)) {
                return false;
            }
            String this$domainCode = this.getDomainCode();
            String other$domainCode = other.getDomainCode();
            if (this$domainCode == null ? other$domainCode != null : !this$domainCode.equals(other$domainCode)) {
                return false;
            }
            String this$domainName = this.getDomainName();
            String other$domainName = other.getDomainName();
            if (this$domainName == null ? other$domainName != null : !this$domainName.equals(other$domainName)) {
                return false;
            }
            String this$status = this.getStatus();
            String other$status = other.getStatus();
            return !(this$status == null ? other$status != null : !this$status.equals(other$status));
        }

        protected boolean canEqual(Object other) {
            return other instanceof DomainHealthStat;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Integer $healthScore = this.getHealthScore();
            result = result * 59 + ($healthScore == null ? 43 : ((Object)$healthScore).hashCode());
            String $domainCode = this.getDomainCode();
            result = result * 59 + ($domainCode == null ? 43 : $domainCode.hashCode());
            String $domainName = this.getDomainName();
            result = result * 59 + ($domainName == null ? 43 : $domainName.hashCode());
            String $status = this.getStatus();
            result = result * 59 + ($status == null ? 43 : $status.hashCode());
            return result;
        }

        public String toString() {
            return "VehicleDashboardStatsVO.DomainHealthStat(domainCode=" + this.getDomainCode() + ", domainName=" + this.getDomainName() + ", healthScore=" + this.getHealthScore() + ", status=" + this.getStatus() + ")";
        }
    }
}

