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

    public String getVehicleSignals() {
        return this.vehicleSignals;
    }

    public void setVehicleSignals(String vehicleSignals) {
        this.vehicleSignals = vehicleSignals;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof KafkaTopicProperties)) {
            return false;
        }
        KafkaTopicProperties other = (KafkaTopicProperties)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$vehicleSignals = this.getVehicleSignals();
        String other$vehicleSignals = other.getVehicleSignals();
        return !(this$vehicleSignals == null ? other$vehicleSignals != null : !this$vehicleSignals.equals(other$vehicleSignals));
    }

    protected boolean canEqual(Object other) {
        return other instanceof KafkaTopicProperties;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $vehicleSignals = this.getVehicleSignals();
        result = result * 59 + ($vehicleSignals == null ? 43 : $vehicleSignals.hashCode());
        return result;
    }

    public String toString() {
        return "KafkaTopicProperties(vehicleSignals=" + this.getVehicleSignals() + ")";
    }
}

