package com.vrd.bigdata.service;

import java.util.List;
import java.util.Map;

public interface DorisQueryService {
    
    Map<String, Object> querySignals(Long vehicleId, String signalName, String startTime, String endTime);
    
    Map<String, Object> queryLogs(Long vehicleId, String ecuType, String startTime, String endTime);
    
    Map<String, Object> queryDiagnostics(Long vehicleId, String startTime, String endTime);
    
    Map<String, Object> aggregateSignals(Long vehicleId, String signalName, String startTime, String endTime);
    
    List<Map<String, Object>> getAvailableDates(String dataType);
    
    Map<String, Object> getStatistics(String dataType, String startTime, String endTime);
    
    Map<String, Object> getSignalTrend(Long vehicleId, String signalName, String startTime, String endTime);
    
    Map<String, Object> getVehicleHealthReport(Long vehicleId, String startTime, String endTime);
    
    List<Map<String, Object>> getTopErrorCodes(String startTime, String endTime, int limit);
}
