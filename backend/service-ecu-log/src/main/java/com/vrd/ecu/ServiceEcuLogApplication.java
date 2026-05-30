package com.vrd.ecu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.vrd.ecu.mapper")
public class ServiceEcuLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceEcuLogApplication.class, args);
    }
}
