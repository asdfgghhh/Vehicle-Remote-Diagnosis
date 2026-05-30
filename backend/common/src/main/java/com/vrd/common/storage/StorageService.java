package com.vrd.common.storage;

import java.io.InputStream;
import java.io.OutputStream;

public interface StorageService {
    
    String upload(String key, InputStream inputStream, long size, String contentType);
    
    String upload(String key, InputStream inputStream, long size);
    
    void download(String key, OutputStream outputStream);
    
    boolean delete(String key);
    
    boolean exists(String key);
    
    String getUrl(String key);
    
    StorageType getStorageType();
}