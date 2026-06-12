package com.vrd.dbc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.dbc.entity.DbcFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DbcFileService extends IService<DbcFile> {
    Page<DbcFile> page(Integer current, Integer size, String keyword, Long modelId);
    
    DbcFile uploadAndParse(MultipartFile file, Long modelId, String modelName, String version, String description);
    
    String parseDbcFile(String filePath);
    
    List<String> getMessageNames(String parseResult);

    List<Map<String, String>> getSignalDefinitions(String parseResult);

    List<Map<String, String>> getSignalDetails(String parseResult);

    void updateMetadata(Long id, String version, String description);

    void publish(Long id);

    void revoke(Long id);

    List<Map<String, String>> getSignalDetailsByFileId(Long id);
    
    void dispatchToVehicle(Long dbcFileId, Long vehicleId);
    
    void dispatchToVehicles(Long dbcFileId, List<Long> vehicleIds);
}
