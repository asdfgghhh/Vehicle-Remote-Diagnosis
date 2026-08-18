/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.kafka.annotation.KafkaListener
 *  org.springframework.stereotype.Component
 */
package com.vrd.vehicle.kafka;

import com.vrd.vehicle.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VehicleKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(VehicleKafkaConsumer.class);
    @Autowired
    private VehicleService vehicleService;

    @KafkaListener(topics={"${vrd.vehicle.kafka.consumer-topic:vehicle-data}"}, groupId="${vrd.vehicle.kafka.consumer-group-id:vehicle-processor}")
    public void onVehicleMessage(String message) {
        log.debug("Received vehicle kafka message");
        this.vehicleService.processKafkaMessage(message);
    }
}

