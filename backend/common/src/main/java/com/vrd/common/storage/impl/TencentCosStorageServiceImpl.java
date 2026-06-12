package com.vrd.common.storage.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageService;
import com.vrd.common.storage.StorageType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class TencentCosStorageServiceImpl implements StorageService {

    private String region;
    private String secretId;
    private String secretKey;
    private String bucketName;
    private String baseUrl;

    private COSClient cosClient;

    private COSClient getCosClient() {
        if (cosClient == null) {
            COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            cosClient = new COSClient(credentials, clientConfig);
        }
        return cosClient;
    }

    @Override
    public String upload(String key, InputStream inputStream, long size, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            if (size >= 0) {
                metadata.setContentLength(size);
            }
            if (contentType != null) {
                metadata.setContentType(contentType);
            }
            PutObjectRequest request = new PutObjectRequest(bucketName, key, inputStream, metadata);
            getCosClient().putObject(request);
            return baseUrl + "/" + key;
        } catch (Exception e) {
            throw new BusinessException("腾讯云COS上传失败: " + e.getMessage());
        }
    }

    @Override
    public String upload(String key, InputStream inputStream, long size) {
        return upload(key, inputStream, size, null);
    }

    @Override
    public void download(String key, OutputStream outputStream) {
        try (COSObject object = getCosClient().getObject(bucketName, key);
             InputStream inputStream = object.getObjectContent()) {
            inputStream.transferTo(outputStream);
        } catch (Exception e) {
            throw new BusinessException("腾讯云COS下载失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream openInputStream(String key) {
        try {
            COSObject object = getCosClient().getObject(bucketName, key);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            object.getObjectContent().transferTo(baos);
            object.close();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            throw new BusinessException("腾讯云COS读取失败: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            getCosClient().deleteObject(bucketName, key);
            return true;
        } catch (Exception e) {
            throw new BusinessException("腾讯云COS删除失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return getCosClient().doesObjectExist(bucketName, key);
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
        return StorageType.TENCENT_COS;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
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
