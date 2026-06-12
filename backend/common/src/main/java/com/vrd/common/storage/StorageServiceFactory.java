package com.vrd.common.storage;

import com.vrd.common.storage.config.StorageConfig;
import com.vrd.common.storage.impl.AliyunOssStorageServiceImpl;
import com.vrd.common.storage.impl.HuaweiObsStorageServiceImpl;
import com.vrd.common.storage.impl.LocalStorageServiceImpl;
import com.vrd.common.storage.impl.MinioStorageServiceImpl;
import com.vrd.common.storage.impl.TencentCosStorageServiceImpl;

public class StorageServiceFactory {

    private StorageServiceFactory() {
    }

    public static StorageService createStorageService(StorageConfig config) {
        StorageType type = config.getType() == null ? StorageType.MINIO : config.getType();

        return switch (type) {
            case ALIYUN_OSS -> createAliyunOssService(config.getAliyunOss());
            case TENCENT_COS -> createTencentCosService(config.getTencentCos());
            case HUAWEI_OBS -> createHuaweiObsService(config.getHuaweiObs());
            case LOCAL -> createLocalService(config.getLocal());
            case MINIO -> createMinioService(config.getMinio());
        };
    }

    public static StorageService createLocalService(StorageConfig.Local localConfig) {
        LocalStorageServiceImpl service = new LocalStorageServiceImpl();
        service.setBasePath(localConfig.getBasePath());
        service.setBaseUrl(localConfig.getBaseUrl());
        return service;
    }

    public static StorageService createAliyunOssService(StorageConfig.AliyunOss aliyunConfig) {
        AliyunOssStorageServiceImpl service = new AliyunOssStorageServiceImpl();
        service.setEndpoint(aliyunConfig.getEndpoint());
        service.setAccessKeyId(aliyunConfig.getAccessKeyId());
        service.setAccessKeySecret(aliyunConfig.getAccessKeySecret());
        service.setBucketName(aliyunConfig.getBucketName());
        service.setBaseUrl(aliyunConfig.getBaseUrl());
        return service;
    }

    public static StorageService createTencentCosService(StorageConfig.TencentCos tencentConfig) {
        TencentCosStorageServiceImpl service = new TencentCosStorageServiceImpl();
        service.setRegion(tencentConfig.getRegion());
        service.setSecretId(tencentConfig.getSecretId());
        service.setSecretKey(tencentConfig.getSecretKey());
        service.setBucketName(tencentConfig.getBucketName());
        service.setBaseUrl(tencentConfig.getBaseUrl());
        return service;
    }

    public static StorageService createHuaweiObsService(StorageConfig.HuaweiObs huaweiConfig) {
        HuaweiObsStorageServiceImpl service = new HuaweiObsStorageServiceImpl();
        service.setEndpoint(huaweiConfig.getEndpoint());
        service.setAccessKeyId(huaweiConfig.getAccessKeyId());
        service.setSecretAccessKey(huaweiConfig.getSecretAccessKey());
        service.setBucketName(huaweiConfig.getBucketName());
        service.setBaseUrl(huaweiConfig.getBaseUrl());
        return service;
    }

    public static StorageService createMinioService(StorageConfig.Minio minioConfig) {
        MinioStorageServiceImpl service = new MinioStorageServiceImpl();
        service.setEndpoint(minioConfig.getEndpoint());
        service.setAccessKey(minioConfig.getAccessKey());
        service.setSecretKey(minioConfig.getSecretKey());
        service.setBucketName(minioConfig.getBucketName());
        service.setBaseUrl(minioConfig.getBaseUrl());
        return service;
    }
}
