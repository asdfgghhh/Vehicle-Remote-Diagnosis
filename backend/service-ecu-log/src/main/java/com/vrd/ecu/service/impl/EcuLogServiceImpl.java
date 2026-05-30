package com.vrd.ecu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.common.exception.BusinessException;
import com.vrd.common.storage.StorageService;
import com.vrd.common.storage.StorageType;
import com.vrd.ecu.entity.EcuLogFile;
import com.vrd.ecu.entity.UploadChunk;
import com.vrd.ecu.mapper.EcuLogFileMapper;
import com.vrd.ecu.mapper.UploadChunkMapper;
import com.vrd.ecu.service.EcuLogService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class EcuLogServiceImpl extends ServiceImpl<EcuLogFileMapper, EcuLogFile> implements EcuLogService {

    @Autowired
    private UploadChunkMapper uploadChunkMapper;

    @Autowired(required = false)
    private StorageService storageService;

    @Value("${file.log.upload-path:/data/vrd/logs/upload}")
    private String uploadPath;

    @Value("${file.log.temp-path:/data/vrd/logs/temp}")
    private String tempPath;

    @Override
    public Page<EcuLogFile> page(Integer current, Integer size, String keyword, Long vehicleId) {
        Page<EcuLogFile> page = new Page<>(current, size);
        LambdaQueryWrapper<EcuLogFile> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(EcuLogFile::getFileName, keyword);
        }
        
        if (vehicleId != null) {
            wrapper.eq(EcuLogFile::getVehicleId, vehicleId);
        }
        
        IPage<EcuLogFile> result = page(page, wrapper);
        return (Page<EcuLogFile>) result;
    }

    @Override
    public String initUpload(String fileName, Long fileSize, String md5, Long vehicleId, String vin, String ecuType) {
        EcuLogFile existFile = lambdaQuery()
                .eq(EcuLogFile::getMd5, md5)
                .eq(EcuLogFile::getDeleted, 0)
                .one();
        
        if (existFile != null) {
            return existFile.getId().toString();
        }
        
        String chunkId = UUID.randomUUID().toString();
        
        return chunkId;
    }

    @Override
    public String uploadChunk(String chunkId, String fileMd5, Integer chunkNumber, Long chunkSize, InputStream inputStream) {
        try {
            String chunkDir = tempPath + File.separator + fileMd5;
            File dir = new File(chunkDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String chunkFileName = chunkDir + File.separator + chunkNumber;
            File chunkFile = new File(chunkFileName);
            
            try (OutputStream outputStream = new FileOutputStream(chunkFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            UploadChunk chunk = new UploadChunk();
            chunk.setChunkId(chunkId);
            chunk.setFileMd5(fileMd5);
            chunk.setChunkNumber(chunkNumber);
            chunk.setChunkSize(chunkSize);
            chunk.setChunkPath(chunkFileName);
            chunk.setStatus(1);
            chunk.setCreateTime(LocalDateTime.now());
            chunk.setUpdateTime(LocalDateTime.now());
            
            uploadChunkMapper.insert(chunk);
            
            return chunkFileName;
        } catch (IOException e) {
            throw new BusinessException("上传分片失败: " + e.getMessage());
        }
    }

    @Override
    public EcuLogFile mergeChunks(String fileMd5, String fileName, Long vehicleId, String vin, String ecuType) {
        try {
            List<UploadChunk> chunks = uploadChunkMapper.selectList(
                    new LambdaQueryWrapper<UploadChunk>()
                            .eq(UploadChunk::getFileMd5, fileMd5)
                            .eq(UploadChunk::getStatus, 1)
                            .orderByAsc(UploadChunk::getChunkNumber)
            );
            
            if (chunks.isEmpty()) {
                throw new BusinessException("没有找到分片文件");
            }
            
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String finalKey = "logs/" + dateStr + "/" + fileName;
            String finalFilePath;
            long fileSize = 0;
            
            if (storageService != null && storageService.getStorageType() != StorageType.LOCAL) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (UploadChunk chunk : chunks) {
                    File chunkFile = new File(chunk.getChunkPath());
                    if (chunkFile.exists()) {
                        FileUtils.copyFile(chunkFile, baos);
                        fileSize += chunkFile.length();
                        chunkFile.delete();
                    }
                }
                
                finalFilePath = storageService.upload(finalKey, new ByteArrayInputStream(baos.toByteArray()), baos.size());
            } else {
                String uploadDir = uploadPath + File.separator + dateStr;
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                
                finalFilePath = uploadDir + File.separator + fileName;
                File finalFile = new File(finalFilePath);
                
                try (OutputStream outputStream = new FileOutputStream(finalFile)) {
                    for (UploadChunk chunk : chunks) {
                        File chunkFile = new File(chunk.getChunkPath());
                        if (chunkFile.exists()) {
                            FileUtils.copyFile(chunkFile, outputStream);
                            chunkFile.delete();
                        }
                    }
                }
                
                fileSize = new File(finalFilePath).length();
            }
            
            EcuLogFile logFile = new EcuLogFile();
            logFile.setFileName(fileName);
            logFile.setFilePath(finalFilePath);
            logFile.setFileSize(fileSize);
            logFile.setMd5(fileMd5);
            logFile.setVehicleId(vehicleId);
            logFile.setVin(vin);
            logFile.setEcuType(ecuType);
            logFile.setUploadStatus(2);
            logFile.setUploadedSize(fileSize);
            logFile.setDeleted(0);
            logFile.setCreateTime(LocalDateTime.now());
            logFile.setUpdateTime(LocalDateTime.now());
            
            save(logFile);
            
            String chunkDir = tempPath + File.separator + fileMd5;
            File dir = new File(chunkDir);
            if (dir.exists()) {
                dir.delete();
            }
            
            return logFile;
        } catch (IOException e) {
            throw new BusinessException("合并分片失败: " + e.getMessage());
        }
    }

    @Override
    public File downloadLog(Long fileId) {
        EcuLogFile logFile = getById(fileId);
        if (logFile == null || logFile.getDeleted() == 1) {
            throw new BusinessException("文件不存在");
        }
        
        String filePath = logFile.getFilePath();
        
        if (storageService != null && storageService.getStorageType() != StorageType.LOCAL) {
            try {
                String fileName = logFile.getFileName();
                File tempFile = new File(tempPath, fileName);
                
                if (!tempFile.getParentFile().exists()) {
                    tempFile.getParentFile().mkdirs();
                }
                
                try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                    String key = filePath.replace(storageService.getUrl(""), "").replaceFirst("^/", "");
                    storageService.download(key, outputStream);
                }
                
                return tempFile;
            } catch (IOException e) {
                throw new BusinessException("从云存储下载文件失败: " + e.getMessage());
            }
        } else {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new BusinessException("文件已被删除");
            }
            return file;
        }
    }

    @Override
    public CheckUploadResult checkUpload(String fileMd5) {
        CheckUploadResult result = new CheckUploadResult();
        
        EcuLogFile existFile = lambdaQuery()
                .eq(EcuLogFile::getMd5, fileMd5)
                .eq(EcuLogFile::getDeleted, 0)
                .one();
        
        if (existFile != null) {
            result.setUploaded(true);
            return result;
        }
        
        List<UploadChunk> chunks = uploadChunkMapper.selectList(
                new LambdaQueryWrapper<UploadChunk>()
                        .eq(UploadChunk::getFileMd5, fileMd5)
                        .eq(UploadChunk::getStatus, 1)
        );
        
        result.setUploaded(false);
        result.setUploadedChunks(chunks.size());
        
        return result;
    }

    public String calculateMD5(InputStream inputStream) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
