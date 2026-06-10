package com.vrd.access.controller;

import com.vrd.access.kafka.KafkaMessageProducer;
import com.vrd.common.result.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/signal/vehicle")
public class VehicleSignalController {

    private final KafkaMessageProducer kafkaMessageProducer;

    public VehicleSignalController(KafkaMessageProducer kafkaMessageProducer) {
        this.kafkaMessageProducer = kafkaMessageProducer;
    }

    @PostMapping("/receive")
    public Result<Void> receiveSignal(@RequestParam String vin, @RequestBody String payload) {
        kafkaMessageProducer.publishVehicleSignal(vin, "http", payload);
        return Result.success();
    }
}
