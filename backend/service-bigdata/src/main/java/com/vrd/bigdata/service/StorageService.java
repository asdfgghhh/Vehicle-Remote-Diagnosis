package com.vrd.bigdata.service;

import java.util.Map;

public interface StorageService {

    void saveVehicleSignal(String vehicleId, Map<String, Object> signalData);

    void saveEcuLog(String vehicleId, Map<String, Object> logData);

    void saveDiagnostic(String vehicleId, Map<String, Object> diagnosticData);

    Map<String, Object> queryVehicleSignals(String vehicleId, String startTime, String endTime);

    Map<String, Object> queryEcuLogs(String vehicleId, String startTime, String endTime);

    Map<String, Object> queryDiagnostics(String vehicleId, String startTime, String endTime);
}
