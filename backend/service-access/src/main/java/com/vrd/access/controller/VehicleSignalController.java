/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.result.Result
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.vrd.access.controller;

import com.vrd.access.kafka.KafkaMessageProducer;
import com.vrd.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/signal/vehicle"})
public class VehicleSignalController {
    private final KafkaMessageProducer kafkaMessageProducer;

    public VehicleSignalController(KafkaMessageProducer kafkaMessageProducer) {
        this.kafkaMessageProducer = kafkaMessageProducer;
    }

    @PostMapping(value={"/receive"})
    public Result<Void> receiveSignal(@RequestParam String vin, @RequestBody String payload) {
        this.kafkaMessageProducer.publishVehicleSignal(vin, "http", payload);
        return Result.success();
    }
}

