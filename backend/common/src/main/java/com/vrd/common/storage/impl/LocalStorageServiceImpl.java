package com.vrd.common.storage.impl;

import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageService;
import com.vrd.common.storage.StorageType;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalStorageServiceImpl implements StorageService {

    @Value("${storage.local.base-path:/data/vrd/storage}")
    private String basePath;

    @Value("${storage.local.base-url:http://localhost:8080/storage}")
    private String baseUrl;

    @Override
    public String upload(String key, InputStream inputStream, long size, String contentType) {
        try {
            Path filePath = Paths.get(basePath, key);
            Path parentDir = filePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            try (OutputStream outputStream = Files.newOutputStream(filePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return baseUrl + "/" + key;
        } catch (IOException e) {
            throw new BusinessException("本地存储上传失败: " + e.getMessage());
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
            throw new BusinessException("本地存储下载失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream openInputStream(String key) {
        try {
            Path filePath = Paths.get(basePath, key);
            if (!Files.exists(filePath)) {
                throw new BusinessException("文件不存在");
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new BusinessException("本地存储读取失败: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            Path filePath = Paths.get(basePath, key);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new BusinessException("本地存储删除失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String key) {
        Path filePath = Paths.get(basePath, key);
        return Files.exists(filePath);
    }

    @Override
    public String getUrl(String key) {
        return baseUrl + "/" + key;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.LOCAL;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}