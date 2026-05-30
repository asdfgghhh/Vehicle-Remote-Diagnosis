package com.vrd.bigdata.service.impl;

import com.vrd.bigdata.service.BigDataQueryService;
import com.vrd.bigdata.service.HdfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BigDataQueryServiceImpl implements BigDataQueryService {

    @Autowired
    private HdfsService hdfsService;

    @Value("${bigdata.storage.partitions[0].path}")
    private String signalsPath;

    @Value("${bigdata.storage.partitions[1].path}")
    private String logsPath;

    @Override
    public Map<String, Object> querySignals(Long vehicleId, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String dateStr = startTime.substring(0, 10);
            String queryPath = signalsPath + "/" + dateStr;
            
            if (hdfsService.exists(queryPath)) {
                List<String> files = hdfsService.listFiles(queryPath);
                List<Map<String, Object>> signals = new ArrayList<>();
                
                for (String file : files) {
                    String content = hdfsService.readFromHdfs(file);
                    if (content != null && !content.isEmpty()) {
                        signals.add(parseJsonToMap(content));
                    }
                }
                
                result.put("data", signals);
                result.put("count", signals.size());
                result.put("success", true);
            } else {
                result.put("data", Collections.emptyList());
                result.put("success", true);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> queryLogs(Long vehicleId, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String dateStr = startTime.substring(0, 10);
            String queryPath = logsPath + "/" + dateStr;
            
            if (hdfsService.exists(queryPath)) {
                List<String> files = hdfsService.listFiles(queryPath);
                List<Map<String, Object>> logs = new ArrayList<>();
                
                for (String file : files) {
                    String content = hdfsService.readFromHdfs(file);
                    if (content != null && !content.isEmpty()) {
                        logs.add(parseJsonToMap(content));
                    }
                }
                
                result.put("data", logs);
                result.put("count", logs.size());
                result.put("success", true);
            } else {
                result.put("data", Collections.emptyList());
                result.put("success", true);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> aggregateSignals(Long vehicleId, String signalName, String startTime, String endTime) {
        Map<String, Object> result = querySignals(vehicleId, startTime, endTime);
        
        if (Boolean.TRUE.equals(result.get("success"))) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
            
            List<Map<String, Object>> filtered = new ArrayList<>();
            for (Map<String, Object> signal : data) {
                if (signalName.equals(signal.get("signalName"))) {
                    filtered.add(signal);
                }
            }
            
            result.put("data", filtered);
            result.put("count", filtered.size());
        }
        
        return result;
    }

    @Override
    public List<String> getAvailableDates(String dataType) {
        String path = dataType.equals("signals") ? signalsPath : logsPath;
        List<String> dates = new ArrayList<>();
        
        try {
            if (hdfsService.exists(path)) {
                List<String> subDirs = hdfsService.listFiles(path);
                for (String subDir : subDirs) {
                    String date = subDir.substring(subDir.lastIndexOf("/") + 1);
                    dates.add(date);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return dates;
    }

    @Override
    public Map<String, Object> getStatistics(String dataType, String startTime, String endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("dataType", dataType);
        stats.put("startTime", startTime);
        stats.put("endTime", endTime);
        stats.put("totalFiles", 0);
        stats.put("totalSize", 0);
        stats.put("recordCount", 0);
        
        return stats;
    }

    private Map<String, Object> parseJsonToMap(String json) {
        try {
            return com.alibaba.fastjson2.JSON.parseObject(json);
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("rawData", json);
            return fallback;
        }
    }
}
