/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 *  org.springframework.stereotype.Component
 */
package com.vrd.access.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="kafka.topics")
public class KafkaTopicProperties {
    private String vehicleSignals = "vehicle-signals";
    private String udsCommands = "uds-commands";
    private String udsResponses = "uds-responses";
    /** 车辆在线状态事件 topic：service-access 生产，service-vehicle 消费 */
    private String vehicleOnlineStatus = "vehicle-online-status";

    public String getVehicleSignals() {
        return this.vehicleSignals;
    }

    public void setVehicleSignals(String vehicleSignals) {
        this.vehicleSignals = vehicleSignals;
    }

    public String getUdsCommands() {
        return this.udsCommands;
    }

    public void setUdsCommands(String udsCommands) {
        this.udsCommands = udsCommands;
    }

    public String getUdsResponses() {
        return this.udsResponses;
    }

    public void setUdsResponses(String udsResponses) {
        this.udsResponses = udsResponses;
    }

    public String getVehicleOnlineStatus() {
        return vehicleOnlineStatus;
    }

    public void setVehicleOnlineStatus(String vehicleOnlineStatus) {
        this.vehicleOnlineStatus = vehicleOnlineStatus;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof KafkaTopicProperties)) {
            return false;
        }
        KafkaTopicProperties other = (KafkaTopicProperties)o;
        String this$vehicleSignals = this.getVehicleSignals();
        String other$vehicleSignals = other.getVehicleSignals();
        if (this$vehicleSignals == null ? other$vehicleSignals != null : !this$vehicleSignals.equals(other$vehicleSignals)) {
            return false;
        }
        String this$uds = this.getUdsCommands();
        String other$uds = other.getUdsCommands();
        if (this$uds == null ? other$uds != null : !this$uds.equals(other$uds)) return false;
        String this$udr = this.getUdsResponses();
        String other$udr = other.getUdsResponses();
        if (this$udr == null ? other$udr != null : !this$udr.equals(other$udr)) return false;
        String this$vos = this.getVehicleOnlineStatus();
        String other$vos = other.getVehicleOnlineStatus();
        return !(this$vos == null ? other$vos != null : !this$vos.equals(other$vos));
    }

    protected boolean canEqual(Object other) {
        return other instanceof KafkaTopicProperties;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $s = this.getVehicleSignals();
        result = result * 59 + ($s == null ? 43 : $s.hashCode());
        $s = this.getUdsCommands();
        result = result * 59 + ($s == null ? 43 : $s.hashCode());
        $s = this.getUdsResponses();
        result = result * 59 + ($s == null ? 43 : $s.hashCode());
        $s = this.getVehicleOnlineStatus();
        result = result * 59 + ($s == null ? 43 : $s.hashCode());
        return result;
    }

    public String toString() {
        return "KafkaTopicProperties(vehicleSignals=" + this.getVehicleSignals()
                + ", udsCommands=" + this.getUdsCommands()
                + ", udsResponses=" + this.getUdsResponses()
                + ", vehicleOnlineStatus=" + this.getVehicleOnlineStatus() + ")";
    }
}

