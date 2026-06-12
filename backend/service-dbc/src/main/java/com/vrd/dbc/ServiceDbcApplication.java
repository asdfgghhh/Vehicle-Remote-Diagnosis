package com.vrd.dbc;

import com.vrd.common.storage.config.StorageAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.vrd.dbc.mapper")
@Import(StorageAutoConfiguration.class)
public class ServiceDbcApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceDbcApplication.class, args);
    }
}
