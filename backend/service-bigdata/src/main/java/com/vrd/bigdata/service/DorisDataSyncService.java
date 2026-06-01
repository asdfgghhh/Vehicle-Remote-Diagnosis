package com.vrd.bigdata.service;

import java.util.List;
import java.util.Map;

public interface DorisDataSyncService {
    
    boolean insertSignals(List<Map<String, Object>> signals);
    
    boolean insertLogs(List<Map<String, Object>> logs);
    
    boolean insertDiagnostics(List<Map<String, Object>> diagnostics);
    
    int batchInsert(String tableName, List<Map<String, Object>> records);
}
