package com.vrd.dbc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.dbc.entity.DbcFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DbcFileService extends IService<DbcFile> {
    Page<DbcFile> page(Integer current, Integer size, String keyword);
    
    DbcFile uploadAndParse(MultipartFile file, String version, String description);
    
    String parseDbcFile(String filePath);
    
    List<String> getMessageNames(String parseResult);
    
    void dispatchToVehicle(Long dbcFileId, Long vehicleId);
    
    void dispatchToVehicles(Long dbcFileId, List<Long> vehicleIds);
}
