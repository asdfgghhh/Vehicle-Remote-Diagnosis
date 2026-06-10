package com.vrd.ecu;

import com.vrd.common.storage.config.StorageAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableAsync
@Import(StorageAutoConfiguration.class)
public class ServiceEcuLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceEcuLogApplication.class, args);
    }
}
