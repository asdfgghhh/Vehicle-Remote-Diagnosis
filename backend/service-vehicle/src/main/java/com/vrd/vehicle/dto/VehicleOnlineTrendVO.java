/*
 * Decompiled with CFR 0.152.
 */
package com.vrd.vehicle.dto;

import java.util.List;

public class VehicleOnlineTrendVO {
    private String granularity;
    private List<TrendPoint> points;

    public String getGranularity() {
        return this.granularity;
    }

    public List<TrendPoint> getPoints() {
        return this.points;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public void setPoints(List<TrendPoint> points) {
        this.points = points;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VehicleOnlineTrendVO)) {
            return false;
        }
        VehicleOnlineTrendVO other = (VehicleOnlineTrendVO)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$granularity = this.getGranularity();
        String other$granularity = other.getGranularity();
        if (this$granularity == null ? other$granularity != null : !this$granularity.equals(other$granularity)) {
            return false;
        }
        List<TrendPoint> this$points = this.getPoints();
        List<TrendPoint> other$points = other.getPoints();
        return !(this$points == null ? other$points != null : !((Object)this$points).equals(other$points));
    }

    protected boolean canEqual(Object other) {
        return other instanceof VehicleOnlineTrendVO;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $granularity = this.getGranularity();
        result = result * 59 + ($granularity == null ? 43 : $granularity.hashCode());
        List<TrendPoint> $points = this.getPoints();
        result = result * 59 + ($points == null ? 43 : ((Object)$points).hashCode());
        return result;
    }

    public String toString() {
        return "VehicleOnlineTrendVO(granularity=" + this.getGranularity() + ", points=" + String.valueOf(this.getPoints()) + ")";
    }

    public static class TrendPoint {
        private String timeLabel;
        private Long onlineCount;

        public String getTimeLabel() {
            return this.timeLabel;
        }

        public Long getOnlineCount() {
            return this.onlineCount;
        }

        public void setTimeLabel(String timeLabel) {
            this.timeLabel = timeLabel;
        }

        public void setOnlineCount(Long onlineCount) {
            this.onlineCount = onlineCount;
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
            Long this$onlineCount = this.getOnlineCount();
            Long other$onlineCount = other.getOnlineCount();
            if (this$onlineCount == null ? other$onlineCount != null : !((Object)this$onlineCount).equals(other$onlineCount)) {
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
            Long $onlineCount = this.getOnlineCount();
            result = result * 59 + ($onlineCount == null ? 43 : ((Object)$onlineCount).hashCode());
            String $timeLabel = this.getTimeLabel();
            result = result * 59 + ($timeLabel == null ? 43 : $timeLabel.hashCode());
            return result;
        }

        public String toString() {
            return "VehicleOnlineTrendVO.TrendPoint(timeLabel=" + this.getTimeLabel() + ", onlineCount=" + this.getOnlineCount() + ")";
        }
    }
}

