/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.mybatis.spring.annotation.MapperScan
 *  org.springframework.boot.SpringApplication
 *  org.springframework.boot.autoconfigure.SpringBootApplication
 *  org.springframework.cloud.client.discovery.EnableDiscoveryClient
 *  org.springframework.kafka.annotation.EnableKafka
 *  org.springframework.scheduling.annotation.EnableAsync
 */
package com.vrd.vehicle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableAsync
@MapperScan(value={"com.vrd.vehicle.mapper", "com.vrd.vehicle.rule.mapper"})
public class ServiceVehicleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceVehicleApplication.class, (String[])args);
    }
}

