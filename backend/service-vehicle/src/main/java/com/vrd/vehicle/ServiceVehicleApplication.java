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
@MapperScan("com.vrd.vehicle.mapper")
public class ServiceVehicleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceVehicleApplication.class, args);
    }
}
