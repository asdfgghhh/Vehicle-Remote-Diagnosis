package com.vrd.access;

import com.vrd.common.storage.config.StorageAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableKafka
@IntegrationComponentScan
@Import(StorageAutoConfiguration.class)
public class ServiceAccessApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAccessApplication.class, args);
    }
}
