/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.storage.config.StorageAutoConfiguration
 *  org.springframework.boot.SpringApplication
 *  org.springframework.boot.autoconfigure.SpringBootApplication
 *  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
 *  org.springframework.cloud.client.discovery.EnableDiscoveryClient
 */
package com.vrd.gateway;

import com.vrd.common.storage.config.StorageAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, StorageAutoConfiguration.class})
@EnableDiscoveryClient
public class ServiceGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceGatewayApplication.class, (String[])args);
    }
}

