package com.vrd.ecu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.ecu.entity.EcuLogFile;

import java.io.File;
import java.io.InputStream;

public interface EcuLogService extends IService<EcuLogFile> {
    Page<EcuLogFile> page(Integer current, Integer size, String keyword, Long vehicleId);
    
    String initUpload(String fileName, Long fileSize, String md5, Long vehicleId, String vin, String ecuType);
    
    String uploadChunk(String chunkId, String fileMd5, Integer chunkNumber, Long chunkSize, InputStream inputStream);
    
    EcuLogFile mergeChunks(String fileMd5, String fileName, Long vehicleId, String vin, String ecuType);
    
    File downloadLog(Long fileId);
    
    CheckUploadResult checkUpload(String fileMd5);
    
    class CheckUploadResult {
        private boolean uploaded;
        private Integer uploadedChunks;
        
        public boolean isUploaded() { return uploaded; }
        public void setUploaded(boolean uploaded) { this.uploaded = uploaded; }
        public Integer getUploadedChunks() { return uploadedChunks; }
        public void setUploadedChunks(Integer uploadedChunks) { this.uploadedChunks = uploadedChunks; }
    }
}
