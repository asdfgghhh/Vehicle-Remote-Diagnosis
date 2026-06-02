package com.vrd.bigdata.service.impl;

import com.vrd.bigdata.service.StorageService;
import com.vrd.bigdata.service.HdfsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("hdfsStorageService")
public class HdfsStorageServiceImpl implements StorageService {

    private final HdfsService hdfsService;

    public HdfsStorageServiceImpl(HdfsService hdfsService) {
        this.hdfsService = hdfsService;
    }

    @Override
    public void saveVehicleSignal(String vehicleId, Map<String, Object> signalData) {
        String path = "/vrd/data/signals/" + vehicleId + "/" + System.currentTimeMillis() + ".json";
        hdfsService.writeJsonToHdfs(path, signalData);
        log.info("Saved vehicle signal to HDFS: {}", path);
    }

    @Override
    public void saveEcuLog(String vehicleId, Map<String, Object> logData) {
        String path = "/vrd/data/logs/" + vehicleId + "/" + System.currentTimeMillis() + ".json";
        hdfsService.writeJsonToHdfs(path, logData);
        log.info("Saved ECU log to HDFS: {}", path);
    }

    @Override
    public void saveDiagnostic(String vehicleId, Map<String, Object> diagnosticData) {
        String path = "/vrd/data/diagnostics/" + vehicleId + "/" + System.currentTimeMillis() + ".json";
        hdfsService.writeJsonToHdfs(path, diagnosticData);
        log.info("Saved diagnostic to HDFS: {}", path);
    }

    @Override
    public Map<String, Object> queryVehicleSignals(String vehicleId, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("vehicleId", vehicleId);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("storageType", "hdfs");
        result.put("data", hdfsService.readJsonFromHdfs("/vrd/data/signals/" + vehicleId));
        return result;
    }

    @Override
    public Map<String, Object> queryEcuLogs(String vehicleId, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("vehicleId", vehicleId);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("storageType", "hdfs");
        result.put("data", hdfsService.readJsonFromHdfs("/vrd/data/logs/" + vehicleId));
        return result;
    }

    @Override
    public Map<String, Object> queryDiagnostics(String vehicleId, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("vehicleId", vehicleId);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("storageType", "hdfs");
        result.put("data", hdfsService.readJsonFromHdfs("/vrd/data/diagnostics/" + vehicleId));
        return result;
    }
}
