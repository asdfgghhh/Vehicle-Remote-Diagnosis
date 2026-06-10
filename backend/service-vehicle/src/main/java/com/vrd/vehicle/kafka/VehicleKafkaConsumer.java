package com.vrd.vehicle.kafka;

import com.vrd.vehicle.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VehicleKafkaConsumer {

    @Autowired
    private VehicleService vehicleService;

    @KafkaListener(
            topics = "${vrd.vehicle.kafka.consumer-topic:vehicle-data}",
            groupId = "${vrd.vehicle.kafka.consumer-group-id:vehicle-processor}"
    )
    public void onVehicleMessage(String message) {
        log.debug("Received vehicle kafka message");
        vehicleService.processKafkaMessage(message);
    }
}
