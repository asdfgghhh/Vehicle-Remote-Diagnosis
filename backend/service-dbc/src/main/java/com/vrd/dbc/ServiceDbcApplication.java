/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.storage.config.StorageAutoConfiguration
 *  org.mybatis.spring.annotation.MapperScan
 *  org.springframework.boot.SpringApplication
 *  org.springframework.boot.autoconfigure.SpringBootApplication
 *  org.springframework.cloud.client.discovery.EnableDiscoveryClient
 *  org.springframework.context.annotation.Import
 */
package com.vrd.dbc;

import com.vrd.common.storage.config.StorageAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan(value={"com.vrd.dbc.mapper"})
@Import(value={StorageAutoConfiguration.class})
public class ServiceDbcApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceDbcApplication.class, (String[])args);
    }
}

