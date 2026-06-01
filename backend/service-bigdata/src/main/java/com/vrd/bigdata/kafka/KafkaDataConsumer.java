package com.vrd.bigdata.kafka;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.bigdata.service.DorisDataSyncService;
import com.vrd.bigdata.service.HdfsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class KafkaDataConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaDataConsumer.class);

    @Autowired
    private HdfsService hdfsService;

    @Autowired
    private DorisDataSyncService dorisDataSyncService;

    @Value("${bigdata.storage.base-path}")
    private String basePath;

    @Value("${bigdata.storage.partitions[0].path}")
    private String signalsPath;

    @Value("${bigdata.storage.partitions[1].path}")
    private String logsPath;

    @Value("${bigdata.sync.doris.enabled:true}")
    private boolean dorisSyncEnabled;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @KafkaListener(topics = "vehicle-signals", groupId = "bigdata-signals")
    public void consumeSignals(String message) {
        try {
            logger.debug("接收到信号数据: {}", message);
            
            String dateStr = LocalDateTime.now().toLocalDate().toString();
            String path = signalsPath + "/" + dateStr + "/signals_" + System.currentTimeMillis() + ".json";
            
            hdfsService.saveToHdfs(message, path);
            
            if (dorisSyncEnabled) {
                Map<String, Object> signalData = parseSignalData(message);
                List<Map<String, Object>> signals = Collections.singletonList(signalData);
                dorisDataSyncService.insertSignals(signals);
            }
            
            logger.info("成功处理信号数据");
        } catch (Exception e) {
            logger.error("处理信号数据失败: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "ecu-logs", groupId = "bigdata-logs")
    public void consumeLogs(String message) {
        try {
            logger.debug("接收到ECU日志数据: {}", message);
            
            String dateStr = LocalDateTime.now().toLocalDate().toString();
            String path = logsPath + "/" + dateStr + "/logs_" + System.currentTimeMillis() + ".json";
            
            hdfsService.saveToHdfs(message, path);
            
            if (dorisSyncEnabled) {
                Map<String, Object> logData = parseLogData(message);
                List<Map<String, Object>> logs = Collections.singletonList(logData);
                dorisDataSyncService.insertLogs(logs);
            }
            
            logger.info("成功处理ECU日志数据");
        } catch (Exception e) {
            logger.error("处理ECU日志数据失败: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "diagnostics", groupId = "bigdata-diagnostics")
    public void consumeDiagnostics(String message) {
        try {
            logger.debug("接收到诊断数据: {}", message);
            
            if (dorisSyncEnabled) {
                Map<String, Object> diagnosticData = parseDiagnosticData(message);
                List<Map<String, Object>> diagnostics = Collections.singletonList(diagnosticData);
                dorisDataSyncService.insertDiagnostics(diagnostics);
            }
            
            logger.info("成功处理诊断数据");
        } catch (Exception e) {
            logger.error("处理诊断数据失败: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> parseSignalData(String message) {
        Map<String, Object> data = new HashMap<>();
        try {
            JSONObject json = JSON.parseObject(message);
            data.put("vehicle_id", json.getLong("vehicleId"));
            data.put("vin", json.getString("vin"));
            data.put("signal_name", json.getString("signalName"));
            data.put("signal_value", json.getString("signalValue"));
            data.put("signal_unit", json.getString("signalUnit"));
            data.put("signal_type", json.getInteger("signalType"));
            data.put("longitude", json.getDouble("longitude"));
            data.put("latitude", json.getDouble("latitude"));
            data.put("speed", json.getDouble("speed"));
            data.put("collect_time", json.getString("collectTime"));
            
            if (data.get("collect_time") == null) {
                data.put("collect_time", LocalDateTime.now().format(DATETIME_FORMATTER));
            }
        } catch (Exception e) {
            logger.error("解析信号数据失败: {}", e.getMessage());
            data.put("raw_data", message);
            data.put("collect_time", LocalDateTime.now().format(DATETIME_FORMATTER));
        }
        return data;
    }

    private Map<String, Object> parseLogData(String message) {
        Map<String, Object> data = new HashMap<>();
        try {
            JSONObject json = JSON.parseObject(message);
            data.put("vehicle_id", json.getLong("vehicleId"));
            data.put("vin", json.getString("vin"));
            data.put("ecu_type", json.getString("ecuType"));
            data.put("error_code", json.getString("errorCode"));
            data.put("error_level", json.getInteger("errorLevel"));
            data.put("error_desc", json.getString("errorDesc"));
            data.put("log_content", json.getString("logContent"));
            data.put("longitude", json.getDouble("longitude"));
            data.put("latitude", json.getDouble("latitude"));
            data.put("speed", json.getDouble("speed"));
            data.put("rpm", json.getInteger("rpm"));
            data.put("collect_time", json.getString("collectTime"));
            
            if (data.get("collect_time") == null) {
                data.put("collect_time", LocalDateTime.now().format(DATETIME_FORMATTER));
            }
        } catch (Exception e) {
            logger.error("解析日志数据失败: {}", e.getMessage());
            data.put("raw_data", message);
            data.put("collect_time", LocalDateTime.now().format(DATETIME_FORMATTER));
        }
        return data;
    }

    private Map<String, Object> parseDiagnosticData(String message) {
        Map<String, Object> data = new HashMap<>();
        try {
            JSONObject json = JSON.parseObject(message);
            data.put("vehicle_id", json.getLong("vehicleId"));
            data.put("vin", json.getString("vin"));
            data.put("diagnostic_type", json.getString("diagnosticType"));
            data.put("diagnostic_result", json.getString("diagnosticResult"));
            data.put("overall_health_score", json.getDouble("overallHealthScore"));
            data.put("system_count", json.getInteger("systemCount"));
            data.put("error_count", json.getInteger("errorCount"));
            data.put("warning_count", json.getInteger("warningCount"));
            data.put("diagnostic_detail", json.getJSONObject("diagnosticDetail") != null 
                ? json.getJSONObject("diagnosticDetail").toJSONString() 
                : "{}");
            data.put("technician_id", json.getLong("technicianId"));
            data.put("diagnostic_time", json.getString("diagnosticTime"));
            
            if (data.get("diagnostic_time") == null) {
                data.put("diagnostic_time", LocalDateTime.now().format(DATETIME_FORMATTER));
            }
        } catch (Exception e) {
            logger.error("解析诊断数据失败: {}", e.getMessage());
            data.put("raw_data", message);
            data.put("diagnostic_time", LocalDateTime.now().format(DATETIME_FORMATTER));
        }
        return data;
    }
}
