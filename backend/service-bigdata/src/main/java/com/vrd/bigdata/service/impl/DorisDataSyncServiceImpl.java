package com.vrd.bigdata.service.impl;

import com.vrd.bigdata.service.DorisDataSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DorisDataSyncServiceImpl implements DorisDataSyncService {

    private static final Logger logger = LoggerFactory.getLogger(DorisDataSyncServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public boolean insertSignals(List<Map<String, Object>> signals) {
        if (signals == null || signals.isEmpty()) {
            return true;
        }

        try {
            int batchSize = 1000;
            for (int i = 0; i < signals.size(); i += batchSize) {
                int end = Math.min(i + batchSize, signals.size());
                List<Map<String, Object>> batch = signals.subList(i, end);
                
                String sql = buildSignalInsertSql(batch);
                jdbcTemplate.execute(sql);
            }
            
            logger.info("成功同步 {} 条信号数据到Doris", signals.size());
            return true;
        } catch (Exception e) {
            logger.error("同步信号数据到Doris失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean insertLogs(List<Map<String, Object>> logs) {
        if (logs == null || logs.isEmpty()) {
            return true;
        }

        try {
            int batchSize = 1000;
            for (int i = 0; i < logs.size(); i += batchSize) {
                int end = Math.min(i + batchSize, logs.size());
                List<Map<String, Object>> batch = logs.subList(i, end);
                
                String sql = buildLogInsertSql(batch);
                jdbcTemplate.execute(sql);
            }
            
            logger.info("成功同步 {} 条日志数据到Doris", logs.size());
            return true;
        } catch (Exception e) {
            logger.error("同步日志数据到Doris失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean insertDiagnostics(List<Map<String, Object>> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return true;
        }

        try {
            int batchSize = 1000;
            for (int i = 0; i < diagnostics.size(); i += batchSize) {
                int end = Math.min(i + batchSize, diagnostics.size());
                List<Map<String, Object>> batch = diagnostics.subList(i, end);
                
                String sql = buildDiagnosticInsertSql(batch);
                jdbcTemplate.execute(sql);
            }
            
            logger.info("成功同步 {} 条诊断数据到Doris", diagnostics.size());
            return true;
        } catch (Exception e) {
            logger.error("同步诊断数据到Doris失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int batchInsert(String tableName, List<Map<String, Object>> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }

        try {
            String sql = buildGenericInsertSql(tableName, records);
            jdbcTemplate.execute(sql);
            return records.size();
        } catch (Exception e) {
            logger.error("批量插入到表 {} 失败: {}", tableName, e.getMessage(), e);
            throw e;
        }
    }

    private String buildSignalInsertSql(List<Map<String, Object>> records) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO vrd_bigdata.vehicle_signals ");
        sql.append("(vehicle_id, vin, signal_name, signal_value, signal_unit, signal_type, ");
        sql.append("longitude, latitude, speed, collect_time, dt, dt_hour) VALUES ");
        
        String values = records.stream()
            .map(this::convertSignalRecord)
            .collect(Collectors.joining(", "));
        
        sql.append(values);
        return sql.toString();
    }

    private String convertSignalRecord(Map<String, Object> record) {
        Long vehicleId = getLongValue(record, "vehicle_id");
        String vin = getStringValue(record, "vin", "");
        String signalName = getStringValue(record, "signal_name", "");
        String signalValue = getStringValue(record, "signal_value", "");
        String signalUnit = getStringValue(record, "signal_unit", "");
        Integer signalType = getIntValue(record, "signal_type", 1);
        Double longitude = getDoubleValue(record, "longitude");
        Double latitude = getDoubleValue(record, "latitude");
        Double speed = getDoubleValue(record, "speed");
        String collectTime = getStringValue(record, "collect_time", LocalDateTime.now().format(DATETIME_FORMATTER));
        
        LocalDateTime dt = LocalDateTime.parse(collectTime, DATETIME_FORMATTER);
        String dtStr = dt.toLocalDate().format(DATE_FORMATTER);
        int dtHour = dt.getHour();
        
        return String.format("(%d, '%s', '%s', '%s', '%s', %d, %s, %s, %s, '%s', '%s', %d)",
            vehicleId, vin, signalName, signalValue, signalUnit, signalType,
            longitude != null ? longitude.toString() : "NULL",
            latitude != null ? latitude.toString() : "NULL",
            speed != null ? speed.toString() : "NULL",
            collectTime, dtStr, dtHour);
    }

    private String buildLogInsertSql(List<Map<String, Object>> records) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO vrd_bigdata.ecu_logs ");
        sql.append("(vehicle_id, vin, ecu_type, error_code, error_level, error_desc, log_content, ");
        sql.append("longitude, latitude, speed, rpm, collect_time, dt) VALUES ");
        
        String values = records.stream()
            .map(this::convertLogRecord)
            .collect(Collectors.joining(", "));
        
        sql.append(values);
        return sql.toString();
    }

    private String convertLogRecord(Map<String, Object> record) {
        Long vehicleId = getLongValue(record, "vehicle_id");
        String vin = getStringValue(record, "vin", "");
        String ecuType = getStringValue(record, "ecu_type", "");
        String errorCode = getStringValue(record, "error_code", "");
        Integer errorLevel = getIntValue(record, "error_level", 1);
        String errorDesc = getStringValue(record, "error_desc", "").replace("'", "''");
        String logContent = getStringValue(record, "log_content", "").replace("'", "''");
        Double longitude = getDoubleValue(record, "longitude");
        Double latitude = getDoubleValue(record, "latitude");
        Double speed = getDoubleValue(record, "speed");
        Integer rpm = getIntValue(record, "rpm", 0);
        String collectTime = getStringValue(record, "collect_time", LocalDateTime.now().format(DATETIME_FORMATTER));
        
        String dtStr = collectTime.substring(0, 10);
        
        return String.format("(%d, '%s', '%s', '%s', %d, '%s', '%s', %s, %s, %s, %d, '%s', '%s')",
            vehicleId, vin, ecuType, errorCode, errorLevel, errorDesc, logContent,
            longitude != null ? longitude.toString() : "NULL",
            latitude != null ? latitude.toString() : "NULL",
            speed != null ? speed.toString() : "NULL",
            rpm, collectTime, dtStr);
    }

    private String buildDiagnosticInsertSql(List<Map<String, Object>> records) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO vrd_bigdata.diagnostics ");
        sql.append("(vehicle_id, vin, diagnostic_type, diagnostic_result, overall_health_score, ");
        sql.append("system_count, error_count, warning_count, diagnostic_detail, technician_id, ");
        sql.append("diagnostic_time, dt) VALUES ");
        
        String values = records.stream()
            .map(this::convertDiagnosticRecord)
            .collect(Collectors.joining(", "));
        
        sql.append(values);
        return sql.toString();
    }

    private String convertDiagnosticRecord(Map<String, Object> record) {
        Long vehicleId = getLongValue(record, "vehicle_id");
        String vin = getStringValue(record, "vin", "");
        String diagnosticType = getStringValue(record, "diagnostic_type", "");
        String diagnosticResult = getStringValue(record, "diagnostic_result", "PASS");
        Double healthScore = getDoubleValue(record, "overall_health_score");
        Integer systemCount = getIntValue(record, "system_count", 0);
        Integer errorCount = getIntValue(record, "error_count", 0);
        Integer warningCount = getIntValue(record, "warning_count", 0);
        String detail = getStringValue(record, "diagnostic_detail", "{}").replace("'", "''");
        Long technicianId = getLongValue(record, "technician_id");
        String diagnosticTime = getStringValue(record, "diagnostic_time", LocalDateTime.now().format(DATETIME_FORMATTER));
        
        String dtStr = diagnosticTime.substring(0, 10);
        
        return String.format("(%d, '%s', '%s', '%s', %s, %d, %d, %d, '%s', %s, '%s', '%s')",
            vehicleId, vin, diagnosticType, diagnosticResult,
            healthScore != null ? healthScore.toString() : "NULL",
            systemCount, errorCount, warningCount, detail,
            technicianId != null ? technicianId.toString() : "NULL",
            diagnosticTime, dtStr);
    }

    private String buildGenericInsertSql(String tableName, List<Map<String, Object>> records) {
        if (records.isEmpty()) {
            return "";
        }

        Map<String, Object> firstRecord = records.get(0);
        String columns = firstRecord.keySet().stream()
            .collect(Collectors.joining(", "));
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO vrd_bigdata.").append(tableName).append(" (").append(columns).append(") VALUES ");
        
        String values = records.stream()
            .map(this::convertGenericRecord)
            .collect(Collectors.joining(", "));
        
        sql.append(values);
        return sql.toString();
    }

    private String convertGenericRecord(Map<String, Object> record) {
        String values = record.values().stream()
            .map(this::escapeValue)
            .collect(Collectors.joining(", ");
        return "(" + values + ")";
    }

    private String escapeValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof String) {
            return "'" + ((String) value).replace("'", "''") + "'";
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "TRUE" : "FALSE";
        } else {
            return "'" + value.toString().replace("'", "''") + "'";
        }
    }

    private Long getLongValue(Map<String, Object> record, String key) {
        Object value = record.get(key);
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Integer getIntValue(Map<String, Object> record, String key, Integer defaultValue) {
        Object value = record.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Double getDoubleValue(Map<String, Object> record, String key) {
        Object value = record.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getStringValue(Map<String, Object> record, String key, String defaultValue) {
        Object value = record.get(key);
        if (value == null) return defaultValue;
        return value.toString();
    }
}
