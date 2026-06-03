package com.vrd.vehicle.dto;

import lombok.Data;

import java.util.List;

@Data
public class VehicleOnlineTrendVO {

    /** hour / day */
    private String granularity;

    private List<TrendPoint> points;

    @Data
    public static class TrendPoint {
        private String timeLabel;
        private Long onlineCount;
    }
}
