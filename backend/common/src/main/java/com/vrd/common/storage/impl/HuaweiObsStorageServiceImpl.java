package com.vrd.common.storage.impl;

import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.obs.v1.ObsClient;
import com.huaweicloud.sdk.obs.v1.model.DeleteObjectRequest;
import com.huaweicloud.sdk.obs.v1.model.GetObjectMetadataRequest;
import com.huaweicloud.sdk.obs.v1.model.GetObjectRequest;
import com.huaweicloud.sdk.obs.v1.model.PutObjectRequest;
import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageService;
import com.vrd.common.storage.StorageType;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HuaweiObsStorageServiceImpl implements StorageService {

    @Value("${storage.huawei-obs.endpoint}")
    private String endpoint;

    @Value("${storage.huawei-obs.access-key-id}")
    private String accessKeyId;

    @Value("${storage.huawei-obs.secret-access-key}")
    private String secretAccessKey;

    @Value("${storage.huawei-obs.bucket-name}")
    private String bucketName;

    @Value("${storage.huawei-obs.base-url}")
    private String baseUrl;

    private ObsClient obsClient;

    private ObsClient getObsClient() {
        if (obsClient == null) {
            ICredential credential = new BasicCredentials()
                    .withAk(accessKeyId)
                    .withSk(secretAccessKey);
            obsClient = ObsClient.newBuilder()
                    .withCredential(credential)
                    .withEndpoint(endpoint)
                    .build();
        }
        return obsClient;
    }

    @Override
    public String upload(String key, InputStream inputStream, long size, String contentType) {
        try {
            PutObjectRequest request = new PutObjectRequest()
                    .withBucketName(bucketName)
                    .withObjectKey(key);
            request.setUploadStream(inputStream);
            getObsClient().putObject(request);
            return baseUrl + "/" + key;
        } catch (Exception e) {
            throw new BusinessException("华为云OBS上传失败: " + e.getMessage());
        }
    }

    @Override
    public String upload(String key, InputStream inputStream, long size) {
        return upload(key, inputStream, size, null);
    }

    @Override
    public void download(String key, OutputStream outputStream) {
        try {
            GetObjectRequest request = new GetObjectRequest()
                    .withBucketName(bucketName)
                    .withObjectKey(key);
            getObsClient().getObject(request).consumeDownloadStream(inputStream -> {
                try {
                    inputStream.transferTo(outputStream);
                } catch (Exception e) {
                    throw new BusinessException("华为云OBS下载失败: " + e.getMessage());
                }
            });
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("华为云OBS下载失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream openInputStream(String key) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GetObjectRequest request = new GetObjectRequest()
                    .withBucketName(bucketName)
                    .withObjectKey(key);
            getObsClient().getObject(request).consumeDownloadStream(inputStream -> {
                try {
                    inputStream.transferTo(baos);
                } catch (IOException e) {
                    throw new BusinessException("华为云OBS读取失败: " + e.getMessage());
                }
            });
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("华为云OBS读取失败: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            DeleteObjectRequest request = new DeleteObjectRequest()
                    .withBucketName(bucketName)
                    .withObjectKey(key);
            getObsClient().deleteObject(request);
            return true;
        } catch (Exception e) {
            throw new BusinessException("华为云OBS删除失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            GetObjectMetadataRequest request = new GetObjectMetadataRequest()
                    .withBucketName(bucketName)
                    .withObjectKey(key);
            getObsClient().getObjectMetadata(request);
            return true;
        } catch (ServiceResponseException e) {
            if (e.getHttpStatusCode() == 404) {
                return false;
            }
            throw new BusinessException("华为云OBS检查失败: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("华为云OBS检查失败: " + e.getMessage());
        }
    }

    @Override
    public String getUrl(String key) {
        return baseUrl + "/" + key;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.HUAWEI_OBS;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
