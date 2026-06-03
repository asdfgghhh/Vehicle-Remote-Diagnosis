package com.vrd.vehicle.dto;

import lombok.Data;

import java.util.List;

@Data
public class VehicleAlertLongTrendVO {

    /** hour / day / week / month */
    private String granularity;

    /** faultCount / faultVehicleCount / avgFaultPerVehicle */
    private String metric;

    private List<TrendPoint> points;

    @Data
    public static class TrendPoint {
        private String timeLabel;
        private Double value;
        private Long faultCount;
        private Long faultVehicleCount;
    }
}
