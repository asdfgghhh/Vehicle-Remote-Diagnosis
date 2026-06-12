package com.vrd.common.storage.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageService;
import com.vrd.common.storage.StorageType;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AliyunOssStorageServiceImpl implements StorageService {

    @Value("${storage.aliyun-oss.endpoint}")
    private String endpoint;

    @Value("${storage.aliyun-oss.access-key-id}")
    private String accessKeyId;

    @Value("${storage.aliyun-oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${storage.aliyun-oss.bucket-name}")
    private String bucketName;

    @Value("${storage.aliyun-oss.base-url}")
    private String baseUrl;

    private OSS ossClient;

    private OSS getOssClient() {
        if (ossClient == null) {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        }
        return ossClient;
    }

    @Override
    public String upload(String key, InputStream inputStream, long size, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            if (contentType != null) {
                metadata.setContentType(contentType);
            }
            PutObjectRequest request = new PutObjectRequest(bucketName, key, inputStream, metadata);
            getOssClient().putObject(request);
            return baseUrl + "/" + key;
        } catch (Exception e) {
            throw new BusinessException("阿里云OSS上传失败: " + e.getMessage());
        }
    }

    @Override
    public String upload(String key, InputStream inputStream, long size) {
        return upload(key, inputStream, size, null);
    }

    @Override
    public void download(String key, OutputStream outputStream) {
        try (InputStream inputStream = openInputStream(key)) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            throw new BusinessException("阿里云OSS下载失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream openInputStream(String key) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            getOssClient().getObject(bucketName, key).getObjectContent().transferTo(baos);
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            throw new BusinessException("阿里云OSS读取失败: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            getOssClient().deleteObject(bucketName, key);
            return true;
        } catch (Exception e) {
            throw new BusinessException("阿里云OSS删除失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return getOssClient().doesObjectExist(bucketName, key);
        } catch (Exception e) {
            throw new BusinessException("阿里云OSS检查失败: " + e.getMessage());
        }
    }

    @Override
    public String getUrl(String key) {
        return baseUrl + "/" + key;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.ALIYUN_OSS;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}