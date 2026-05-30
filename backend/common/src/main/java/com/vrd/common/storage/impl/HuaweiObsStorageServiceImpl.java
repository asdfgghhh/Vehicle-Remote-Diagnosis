package com.vrd.common.storage.impl;

import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.obs.v2.ObsClient;
import com.huaweicloud.sdk.obs.v2.model.PutObjectRequest;
import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageService;
import com.vrd.common.storage.StorageType;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
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
            PutObjectRequest request = new PutObjectRequest();
            request.setBucketName(bucketName);
            request.setObjectKey(key);
            request.setSourceStream(inputStream);
            if (contentType != null) {
                request.setContentType(contentType);
            }
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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            getObsClient().getObject(bucketName, key).getObjectContent().transferTo(baos);
            outputStream.write(baos.toByteArray());
        } catch (Exception e) {
            throw new BusinessException("华为云OBS下载失败: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            getObsClient().deleteObject(bucketName, key);
            return true;
        } catch (Exception e) {
            throw new BusinessException("华为云OBS删除失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return getObsClient().doesObjectExist(bucketName, key);
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