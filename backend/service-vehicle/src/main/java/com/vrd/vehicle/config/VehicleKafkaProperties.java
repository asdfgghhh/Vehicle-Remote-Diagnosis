/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 *  org.springframework.cloud.context.config.annotation.RefreshScope
 *  org.springframework.stereotype.Component
 */
package com.vrd.vehicle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
@ConfigurationProperties(prefix="vrd.vehicle.kafka")
public class VehicleKafkaProperties {
    private String consumerTopic = "vehicle-data";
    private String producerTopic = "vehicle-data";
    private String consumerGroupId = "vehicle-processor";

    public String getConsumerTopic() {
        return this.consumerTopic;
    }

    public String getProducerTopic() {
        return this.producerTopic;
    }

    public String getConsumerGroupId() {
        return this.consumerGroupId;
    }

    public void setConsumerTopic(String consumerTopic) {
        this.consumerTopic = consumerTopic;
    }

    public void setProducerTopic(String producerTopic) {
        this.producerTopic = producerTopic;
    }

    public void setConsumerGroupId(String consumerGroupId) {
        this.consumerGroupId = consumerGroupId;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VehicleKafkaProperties)) {
            return false;
        }
        VehicleKafkaProperties other = (VehicleKafkaProperties)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$consumerTopic = this.getConsumerTopic();
        String other$consumerTopic = other.getConsumerTopic();
        if (this$consumerTopic == null ? other$consumerTopic != null : !this$consumerTopic.equals(other$consumerTopic)) {
            return false;
        }
        String this$producerTopic = this.getProducerTopic();
        String other$producerTopic = other.getProducerTopic();
        if (this$producerTopic == null ? other$producerTopic != null : !this$producerTopic.equals(other$producerTopic)) {
            return false;
        }
        String this$consumerGroupId = this.getConsumerGroupId();
        String other$consumerGroupId = other.getConsumerGroupId();
        return !(this$consumerGroupId == null ? other$consumerGroupId != null : !this$consumerGroupId.equals(other$consumerGroupId));
    }

    protected boolean canEqual(Object other) {
        return other instanceof VehicleKafkaProperties;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $consumerTopic = this.getConsumerTopic();
        result = result * 59 + ($consumerTopic == null ? 43 : $consumerTopic.hashCode());
        String $producerTopic = this.getProducerTopic();
        result = result * 59 + ($producerTopic == null ? 43 : $producerTopic.hashCode());
        String $consumerGroupId = this.getConsumerGroupId();
        result = result * 59 + ($consumerGroupId == null ? 43 : $consumerGroupId.hashCode());
        return result;
    }

    public String toString() {
        return "VehicleKafkaProperties(consumerTopic=" + this.getConsumerTopic() + ", producerTopic=" + this.getProducerTopic() + ", consumerGroupId=" + this.getConsumerGroupId() + ")";
    }
}

