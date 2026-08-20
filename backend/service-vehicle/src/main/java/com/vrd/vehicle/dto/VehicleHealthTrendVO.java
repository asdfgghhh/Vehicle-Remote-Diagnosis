package com.vrd.vehicle.dto;

import java.util.List;

public class VehicleHealthTrendVO {
    private String vin;
    private String granularity;
    private List<TrendPoint> points;

    public String getVin() { return this.vin; }
    public String getGranularity() { return this.granularity; }
    public List<TrendPoint> getPoints() { return this.points; }

    public void setVin(String vin) { this.vin = vin; }
    public void setGranularity(String granularity) { this.granularity = granularity; }
    public void setPoints(List<TrendPoint> points) { this.points = points; }

    public static class TrendPoint {
        private String timeLabel;
        private Integer healthScore;
        private String status;

        public String getTimeLabel() { return this.timeLabel; }
        public Integer getHealthScore() { return this.healthScore; }
        public String getStatus() { return this.status; }

        public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }
        public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
        public void setStatus(String status) { this.status = status; }
    }
}
