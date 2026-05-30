package com.vrd.signal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.integration.annotation.IntegrationComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.vrd.signal.mapper")
@IntegrationComponentScan
public class ServiceSignalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceSignalApplication.class, args);
    }
}
