package com.vrd.vehicle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "vrd.vehicle.kafka")
public class VehicleKafkaProperties {

    /**
     * 消费其他服务推送的车辆数据主题
     */
    private String consumerTopic = "vehicle-data";

    /**
     * 本服务对外发布车辆变更的主题
     */
    private String producerTopic = "vehicle-data";

    /**
     * Kafka 消费者组
     */
    private String consumerGroupId = "vehicle-processor";
}
