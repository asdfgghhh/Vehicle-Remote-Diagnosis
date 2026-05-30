package com.vrd.bigdata.service;

import java.util.List;
import java.util.Map;

public interface BigDataQueryService {
    Map<String, Object> querySignals(Long vehicleId, String startTime, String endTime);
    
    Map<String, Object> queryLogs(Long vehicleId, String startTime, String endTime);
    
    Map<String, Object> aggregateSignals(Long vehicleId, String signalName, String startTime, String endTime);
    
    List<String> getAvailableDates(String dataType);
    
    Map<String, Object> getStatistics(String dataType, String startTime, String endTime);
}
