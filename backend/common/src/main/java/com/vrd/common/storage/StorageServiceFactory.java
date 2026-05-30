package com.vrd.common.storage;

import com.vrd.common.storage.config.StorageConfig;
import com.vrd.common.storage.impl.AliyunOssStorageServiceImpl;
import com.vrd.common.storage.impl.HuaweiObsStorageServiceImpl;
import com.vrd.common.storage.impl.LocalStorageServiceImpl;
import com.vrd.common.storage.impl.MinioStorageServiceImpl;

public class StorageServiceFactory {

    private StorageServiceFactory() {
    }

    public static StorageService createStorageService(StorageConfig config) {
        StorageType type = config.getType();
        
        switch (type) {
            case ALIYUN_OSS:
                return createAliyunOssService(config);
            case HUAWEI_OBS:
                return createHuaweiObsService(config);
            case MINIO:
                return createMinioService(config);
            case LOCAL:
            default:
                return createLocalService(config);
        }
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

    private static LocalStorageServiceImpl createLocalService(StorageConfig config) {
        return (LocalStorageServiceImpl) createLocalService(config.getLocal());
    }

    private static AliyunOssStorageServiceImpl createAliyunOssService(StorageConfig config) {
        return (AliyunOssStorageServiceImpl) createAliyunOssService(config.getAliyunOss());
    }

    private static HuaweiObsStorageServiceImpl createHuaweiObsService(StorageConfig config) {
        return (HuaweiObsStorageServiceImpl) createHuaweiObsService(config.getHuaweiObs());
    }

    private static MinioStorageServiceImpl createMinioService(StorageConfig config) {
        return (MinioStorageServiceImpl) createMinioService(config.getMinio());
    }
}