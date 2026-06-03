package com.vrd.bigdata.service;

import java.util.List;
import java.util.Map;

public interface HdfsService {
    void saveToHdfs(String data, String path);
    
    String readFromHdfs(String path);
    
    void writeJsonToHdfs(String path, Map<String, Object> data);
    
    Object readJsonFromHdfs(String path);
    
    List<String> listFiles(String directory);
    
    void deleteFromHdfs(String path);
    
    boolean exists(String path);
    
    long getFileSize(String path);
    
    void createDirectory(String path);
}
