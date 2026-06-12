package com.vrd.common.storage.config;

import com.vrd.common.storage.StorageService;
import com.vrd.common.storage.StorageType;
import com.vrd.common.storage.impl.AliyunOssStorageServiceImpl;
import com.vrd.common.storage.impl.HuaweiObsStorageServiceImpl;
import com.vrd.common.storage.impl.LocalStorageServiceImpl;
import com.vrd.common.storage.impl.MinioStorageServiceImpl;
import com.vrd.common.storage.impl.TencentCosStorageServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfiguration
@EnableConfigurationProperties(StorageConfig.class)
public class StorageAutoConfiguration {

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    public StorageService storageService(StorageConfig config) {
        StorageType type = config.getType() == null ? StorageType.MINIO : config.getType();

        return switch (type) {
            case ALIYUN_OSS -> createAliyunOssService(config);
            case TENCENT_COS -> createTencentCosService(config);
            case HUAWEI_OBS -> createHuaweiObsService(config);
            case LOCAL -> createLocalService(config);
            case MINIO -> createMinioService(config);
        };
    }

    private LocalStorageServiceImpl createLocalService(StorageConfig config) {
        LocalStorageServiceImpl service = new LocalStorageServiceImpl();
        service.setBasePath(config.getLocal().getBasePath());
        service.setBaseUrl(config.getLocal().getBaseUrl());
        return service;
    }

    private AliyunOssStorageServiceImpl createAliyunOssService(StorageConfig config) {
        AliyunOssStorageServiceImpl service = new AliyunOssStorageServiceImpl();
        service.setEndpoint(config.getAliyunOss().getEndpoint());
        service.setAccessKeyId(config.getAliyunOss().getAccessKeyId());
        service.setAccessKeySecret(config.getAliyunOss().getAccessKeySecret());
        service.setBucketName(config.getAliyunOss().getBucketName());
        service.setBaseUrl(config.getAliyunOss().getBaseUrl());
        return service;
    }

    private TencentCosStorageServiceImpl createTencentCosService(StorageConfig config) {
        TencentCosStorageServiceImpl service = new TencentCosStorageServiceImpl();
        service.setRegion(config.getTencentCos().getRegion());
        service.setSecretId(config.getTencentCos().getSecretId());
        service.setSecretKey(config.getTencentCos().getSecretKey());
        service.setBucketName(config.getTencentCos().getBucketName());
        service.setBaseUrl(config.getTencentCos().getBaseUrl());
        return service;
    }

    private HuaweiObsStorageServiceImpl createHuaweiObsService(StorageConfig config) {
        HuaweiObsStorageServiceImpl service = new HuaweiObsStorageServiceImpl();
        service.setEndpoint(config.getHuaweiObs().getEndpoint());
        service.setAccessKeyId(config.getHuaweiObs().getAccessKeyId());
        service.setSecretAccessKey(config.getHuaweiObs().getSecretAccessKey());
        service.setBucketName(config.getHuaweiObs().getBucketName());
        service.setBaseUrl(config.getHuaweiObs().getBaseUrl());
        return service;
    }

    private MinioStorageServiceImpl createMinioService(StorageConfig config) {
        MinioStorageServiceImpl service = new MinioStorageServiceImpl();
        service.setEndpoint(config.getMinio().getEndpoint());
        service.setAccessKey(config.getMinio().getAccessKey());
        service.setSecretKey(config.getMinio().getSecretKey());
        service.setBucketName(config.getMinio().getBucketName());
        service.setBaseUrl(config.getMinio().getBaseUrl());
        return service;
    }
}
