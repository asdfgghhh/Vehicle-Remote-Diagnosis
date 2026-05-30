package com.vrd.dbc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vrd.common.exception.BusinessException;
import com.vrd.dbc.entity.DbcFile;
import com.vrd.dbc.entity.DispatchLog;
import com.vrd.dbc.mapper.DbcFileMapper;
import com.vrd.dbc.mapper.DispatchLogMapper;
import com.vrd.dbc.service.DbcFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DbcFileServiceImpl extends ServiceImpl<DbcFileMapper, DbcFile> implements DbcFileService {

    @Autowired
    private DispatchLogMapper dispatchLogMapper;

    @Value("${file.dbc.upload-path}")
    private String uploadPath;

    @Override
    public Page<DbcFile> page(Integer current, Integer size, String keyword) {
        Page<DbcFile> page = new Page<>(current, size);
        LambdaQueryWrapper<DbcFile> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(DbcFile::getFileName, keyword);
        }
        
        IPage<DbcFile> result = page(page, wrapper);
        return (Page<DbcFile>) result;
    }

    @Override
    public DbcFile uploadAndParse(MultipartFile file, String version, String description) {
        try {
            String dateStr = LocalDate.now().toString();
            String uploadDir = uploadPath + File.separator + dateStr;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String originalFilename = file.getOriginalFilename();
            String filePath = uploadDir + File.separator + originalFilename;
            File destFile = new File(filePath);
            file.transferTo(destFile);
            
            String parseResult = parseDbcFile(filePath);
            
            DbcFile dbcFile = new DbcFile();
            dbcFile.setFileName(originalFilename);
            dbcFile.setFilePath(filePath);
            dbcFile.setFileSize(file.getSize());
            dbcFile.setVersion(version);
            dbcFile.setDescription(description);
            dbcFile.setParseResult(parseResult);
            dbcFile.setMessageCount(countMessages(parseResult));
            dbcFile.setSignalCount(countSignals(parseResult));
            dbcFile.setStatus(1);
            dbcFile.setDeleted(0);
            dbcFile.setCreateTime(LocalDateTime.now());
            dbcFile.setUpdateTime(LocalDateTime.now());
            
            save(dbcFile);
            
            return dbcFile;
        } catch (IOException e) {
            throw new BusinessException("上传DBC文件失败: " + e.getMessage());
        }
    }

    @Override
    public String parseDbcFile(String filePath) {
        StringBuilder parseResult = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith("BO_ ")) {
                    parseResult.append("MESSAGE: ").append(line).append("\n");
                } else if (line.startsWith("SG_ ")) {
                    parseResult.append("  SIGNAL: ").append(line).append("\n");
                } else if (line.startsWith("CM_ SG_")) {
                    parseResult.append("  COMMENT: ").append(line).append("\n");
                }
            }
        } catch (IOException e) {
            throw new BusinessException("解析DBC文件失败: " + e.getMessage());
        }
        
        return parseResult.toString();
    }

    @Override
    public List<String> getMessageNames(String parseResult) {
        List<String> messages = new ArrayList<>();
        Pattern pattern = Pattern.compile("MESSAGE:\\s*BO_\\s*\\d+\\s+(\\w+):");
        Matcher matcher = pattern.matcher(parseResult);
        
        while (matcher.find()) {
            messages.add(matcher.group(1));
        }
        
        return messages;
    }

    private int countMessages(String parseResult) {
        int count = 0;
        Pattern pattern = Pattern.compile("BO_ \\d+");
        java.util.regex.Matcher matcher = pattern.matcher(parseResult);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int countSignals(String parseResult) {
        int count = 0;
        Pattern pattern = Pattern.compile("SG_ \\w+");
        java.util.regex.Matcher matcher = pattern.matcher(parseResult);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    @Override
    public void dispatchToVehicle(Long dbcFileId, Long vehicleId) {
        DbcFile dbcFile = getById(dbcFileId);
        if (dbcFile == null) {
            throw new BusinessException("DBC文件不存在");
        }
        
        DispatchLog dispatchLog = new DispatchLog();
        dispatchLog.setDbcFileId(dbcFileId);
        dispatchLog.setVehicleId(vehicleId);
        dispatchLog.setDispatchType("SINGLE");
        dispatchLog.setStatus(1);
        dispatchLog.setDispatchTime(LocalDateTime.now());
        dispatchLog.setCreateTime(LocalDateTime.now());
        
        try {
            String result = sendToVehicle(dbcFile.getFilePath(), vehicleId);
            dispatchLog.setStatus(2);
            dispatchLog.setResult(result);
        } catch (Exception e) {
            dispatchLog.setStatus(3);
            dispatchLog.setResult("失败: " + e.getMessage());
        }
        
        dispatchLogMapper.insert(dispatchLog);
    }

    @Override
    public void dispatchToVehicles(Long dbcFileId, List<Long> vehicleIds) {
        for (Long vehicleId : vehicleIds) {
            dispatchToVehicle(dbcFileId, vehicleId);
        }
    }

    private String sendToVehicle(String filePath, Long vehicleId) {
        return "SUCCESS: DBC文件已下发到车辆 " + vehicleId;
    }
}
