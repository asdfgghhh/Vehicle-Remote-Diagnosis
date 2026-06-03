package com.vrd.vehicle.dto;

import lombok.Data;

import java.util.List;

@Data
public class VehicleDashboardStatsVO {

    /** 已接入车型数量 */
    private Long connectedModelCount;

    /** 车辆总数 */
    private Long totalVehicles;

    /** 在线车辆数 */
    private Long onlineVehicles;

    /** 告警总数 */
    private Long totalAlertCount;

    /** 各车型接入车辆数 */
    private List<ModelVehicleStat> modelStats;

    /** 按部件英文简称统计的告警数 */
    private List<AlertByComponent> alertByComponent;

    /** 最近告警 */
    private List<RecentAlert> recentAlerts;

    /** 故障总数 */
    private Long totalFaultCount;

    /** 按故障编码统计 */
    private List<FaultByCode> faultByCode;

    @Data
    public static class FaultByCode {
        private String faultCode;
        /** 部件英文简称 */
        private String componentCode;
        private String faultName;
        private Long faultCount;
    }

    @Data
    public static class AlertByComponent {
        /** 部件英文简称 */
        private String componentCode;
        private Long alertCount;
    }

    @Data
    public static class RecentAlert {
        private String time;
        private String vin;
        /** 部件英文简称 */
        private String componentCode;
        private String type;
        private String message;
        private String status;
    }

    @Data
    public static class ModelVehicleStat {
        private Long modelId;
        private String modelName;
        private String modelCode;
        private Long vehicleCount;
    }
}
