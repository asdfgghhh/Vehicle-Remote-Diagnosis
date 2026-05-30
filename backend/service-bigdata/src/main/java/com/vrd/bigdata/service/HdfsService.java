package com.vrd.bigdata.service;

import org.apache.hadoop.fs.FileStatus;

import java.util.List;

public interface HdfsService {
    void saveToHdfs(String data, String path);
    
    String readFromHdfs(String path);
    
    List<String> listFiles(String directory);
    
    void deleteFromHdfs(String path);
    
    boolean exists(String path);
    
    long getFileSize(String path);
    
    void createDirectory(String path);
}
