package com.vrd.bigdata.service.impl;

import com.vrd.bigdata.config.BigDataStorageProperties;
import com.vrd.bigdata.config.StorageType;
import com.vrd.bigdata.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StorageServiceSelector {

    private final StorageService clickHouseStorageService;
    private final StorageService hdfsStorageService;
    private final BigDataStorageProperties storageProperties;

    public StorageServiceSelector(
            ClickHouseStorageServiceImpl clickHouseStorageService,
            HdfsStorageServiceImpl hdfsStorageService,
            BigDataStorageProperties storageProperties) {
        this.clickHouseStorageService = clickHouseStorageService;
        this.hdfsStorageService = hdfsStorageService;
        this.storageProperties = storageProperties;
        log.info("Default storage type configured: {}", storageProperties.getType());
    }

    public StorageService getStorageService() {
        return getStorageService(storageProperties.getType());
    }

    public StorageService getStorageService(StorageType type) {
        return switch (type) {
            case CLICKHOUSE -> {
                log.debug("Using ClickHouse storage");
                yield clickHouseStorageService;
            }
            case HDFS -> {
                log.debug("Using HDFS storage");
                yield hdfsStorageService;
            }
            case HYBRID -> {
                log.debug("Using Hybrid storage (ClickHouse + HDFS)");
                yield clickHouseStorageService;
            }
        };
    }

    public StorageType getCurrentStorageType() {
        return storageProperties.getType();
    }
}
