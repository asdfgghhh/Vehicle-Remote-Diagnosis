/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.SpringApplication
 *  org.springframework.boot.autoconfigure.SpringBootApplication
 *  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
 *  org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration
 *  org.springframework.cloud.client.discovery.EnableDiscoveryClient
 */
package com.vrd.signal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, SqlInitializationAutoConfiguration.class})
@EnableDiscoveryClient
public class ServiceSignalApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceSignalApplication.class, (String[])args);
    }
}

