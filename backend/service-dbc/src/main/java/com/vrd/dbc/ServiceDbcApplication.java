package com.vrd.dbc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.vrd.dbc.mapper")
public class ServiceDbcApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceDbcApplication.class, args);
    }
}
