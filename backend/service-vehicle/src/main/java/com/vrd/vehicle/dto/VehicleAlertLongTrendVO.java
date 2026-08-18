/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.vehicle.dto;

import java.util.List;

public class VehicleAlertLongTrendVO {
    private String granularity;
    private String metric;
    private List<TrendPoint> points;

    public String getGranularity() {
        return this.granularity;
    }

    public String getMetric() {
        return this.metric;
    }

    public List<TrendPoint> getPoints() {
        return this.points;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public void setPoints(List<TrendPoint> points) {
        this.points = points;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VehicleAlertLongTrendVO)) {
            return false;
        }
        VehicleAlertLongTrendVO other = (VehicleAlertLongTrendVO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$granularity = this.getGranularity();
        String other$granularity = other.getGranularity();
        if (this$granularity == null ? other$granularity != null : !this$granularity.equals(other$granularity)) {
            return false;
        }
        String this$metric = this.getMetric();
        String other$metric = other.getMetric();
        if (this$metric == null ? other$metric != null : !this$metric.equals(other$metric)) {
            return false;
        }
        List<TrendPoint> this$points = this.getPoints();
        List<TrendPoint> other$points = other.getPoints();
        return !(this$points == null ? other$points != null : !((Object)this$points).equals(other$points));
    }

    protected boolean canEqual(Object other) {
        return other instanceof VehicleAlertLongTrendVO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $granularity = this.getGranularity();
        result = result * 59 + ($granularity == null ? 43 : $granularity.hashCode());
        String $metric = this.getMetric();
        result = result * 59 + ($metric == null ? 43 : $metric.hashCode());
        List<TrendPoint> $points = this.getPoints();
        result = result * 59 + ($points == null ? 43 : ((Object)$points).hashCode());
        return result;
    }

    public String toString() {
        return "VehicleAlertLongTrendVO(granularity=" + this.getGranularity() + ", metric=" + this.getMetric() + ", points=" + String.valueOf(this.getPoints()) + ")";
    }

    public static class TrendPoint {
        private String timeLabel;
        private Double value;
        private Long faultCount;
        private Long faultVehicleCount;

        public String getTimeLabel() {
            return this.timeLabel;
        }

        public Double getValue() {
            return this.value;
        }

        public Long getFaultCount() {
            return this.faultCount;
        }

        public Long getFaultVehicleCount() {
            return this.faultVehicleCount;
        }

        public void setTimeLabel(String timeLabel) {
            this.timeLabel = timeLabel;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public void setFaultCount(Long faultCount) {
            this.faultCount = faultCount;
        }

        public void setFaultVehicleCount(Long faultVehicleCount) {
            this.faultVehicleCount = faultVehicleCount;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof TrendPoint)) {
                return false;
            }
            TrendPoint other = (TrendPoint)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Double this$value = this.getValue();
            Double other$value = other.getValue();
            if (this$value == null ? other$value != null : !((Object)this$value).equals(other$value)) {
                return false;
            }
            Long this$faultCount = this.getFaultCount();
            Long other$faultCount = other.getFaultCount();
            if (this$faultCount == null ? other$faultCount != null : !((Object)this$faultCount).equals(other$faultCount)) {
                return false;
            }
            Long this$faultVehicleCount = this.getFaultVehicleCount();
            Long other$faultVehicleCount = other.getFaultVehicleCount();
            if (this$faultVehicleCount == null ? other$faultVehicleCount != null : !((Object)this$faultVehicleCount).equals(other$faultVehicleCount)) {
                return false;
            }
            String this$timeLabel = this.getTimeLabel();
            String other$timeLabel = other.getTimeLabel();
            return !(this$timeLabel == null ? other$timeLabel != null : !this$timeLabel.equals(other$timeLabel));
        }

        protected boolean canEqual(Object other) {
            return other instanceof TrendPoint;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Double $value = this.getValue();
            result = result * 59 + ($value == null ? 43 : ((Object)$value).hashCode());
            Long $faultCount = this.getFaultCount();
            result = result * 59 + ($faultCount == null ? 43 : ((Object)$faultCount).hashCode());
            Long $faultVehicleCount = this.getFaultVehicleCount();
            result = result * 59 + ($faultVehicleCount == null ? 43 : ((Object)$faultVehicleCount).hashCode());
            String $timeLabel = this.getTimeLabel();
            result = result * 59 + ($timeLabel == null ? 43 : $timeLabel.hashCode());
            return result;
        }

        public String toString() {
            return "VehicleAlertLongTrendVO.TrendPoint(timeLabel=" + this.getTimeLabel() + ", value=" + this.getValue() + ", faultCount=" + this.getFaultCount() + ", faultVehicleCount=" + this.getFaultVehicleCount() + ")";
        }
    }
}

