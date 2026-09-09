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
    /** DBC 文件下发指令 topic：service-dbc 生产，service-access 消费 */
    private String dbcDispatch = "dbc-dispatch";
    /** DBC 文件下发 ACK topic：service-access 生产，service-dbc 消费 */
    private String dbcDispatchAck = "dbc-dispatch-ack";
    /** DBC 下发回调 topic：service-access 生产（MQTT 发送结果），service-dbc 消费更新状态 */
    private String dbcDispatchCallback = "dbc-dispatch-callback";

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

    public String getDbcDispatch() {
        return dbcDispatch;
    }

    public void setDbcDispatch(String dbcDispatch) {
        this.dbcDispatch = dbcDispatch;
    }

    public String getDbcDispatchAck() {
        return dbcDispatchAck;
    }

    public void setDbcDispatchAck(String dbcDispatchAck) {
        this.dbcDispatchAck = dbcDispatchAck;
    }

    public String getDbcDispatchCallback() {
        return dbcDispatchCallback;
    }

    public void setDbcDispatchCallback(String dbcDispatchCallback) {
        this.dbcDispatchCallback = dbcDispatchCallback;
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
        if (this$vos == null ? other$vos != null : !this$vos.equals(other$vos)) return false;
        String this$dd = this.getDbcDispatch();
        String other$dd = other.getDbcDispatch();
        if (this$dd == null ? other$dd != null : !this$dd.equals(other$dd)) return false;
        String this$dda = this.getDbcDispatchAck();
        String other$dda = other.getDbcDispatchAck();
        return !(this$dda == null ? other$dda != null : !this$dda.equals(other$dda));
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
        $s = this.getDbcDispatch();
        result = result * 59 + ($s == null ? 43 : $s.hashCode());
        $s = this.getDbcDispatchAck();
        result = result * 59 + ($s == null ? 43 : $s.hashCode());
        return result;
    }

    public String toString() {
        return "KafkaTopicProperties(vehicleSignals=" + this.getVehicleSignals()
                + ", udsCommands=" + this.getUdsCommands()
                + ", udsResponses=" + this.getUdsResponses()
                + ", vehicleOnlineStatus=" + this.getVehicleOnlineStatus()
                + ", dbcDispatch=" + this.getDbcDispatch()
                + ", dbcDispatchAck=" + this.getDbcDispatchAck() + ")";
    }
}

