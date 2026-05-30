package com.vrd.common.storage.impl;

import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageService;
import com.vrd.common.storage.StorageType;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MinioStorageServiceImpl implements StorageService {

    @Value("${storage.minio.endpoint}")
    private String endpoint;

    @Value("${storage.minio.access-key}")
    private String accessKey;

    @Value("${storage.minio.secret-key}")
    private String secretKey;

    @Value("${storage.minio.bucket-name}")
    private String bucketName;

    @Value("${storage.minio.base-url}")
    private String baseUrl;

    private MinioClient minioClient;

    private MinioClient getMinioClient() {
        if (minioClient == null) {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        }
        return minioClient;
    }

    @Override
    public String upload(String key, InputStream inputStream, long size, String contentType) {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .stream(inputStream, size, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build();
            getMinioClient().putObject(args);
            return baseUrl + "/" + key;
        } catch (Exception e) {
            throw new BusinessException("MinIO上传失败: " + e.getMessage());
        }
    }

    @Override
    public String upload(String key, InputStream inputStream, long size) {
        return upload(key, inputStream, size, null);
    }

    @Override
    public void download(String key, OutputStream outputStream) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .build();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            getMinioClient().getObject(args).transferTo(baos);
            outputStream.write(baos.toByteArray());
        } catch (Exception e) {
            throw new BusinessException("MinIO下载失败: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .build();
            getMinioClient().removeObject(args);
            return true;
        } catch (Exception e) {
            throw new BusinessException("MinIO删除失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .build();
            getMinioClient().statObject(args);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUrl(String key) {
        return baseUrl + "/" + key;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.MINIO;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}