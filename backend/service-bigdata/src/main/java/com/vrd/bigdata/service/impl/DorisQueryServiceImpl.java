package com.vrd.bigdata.service.impl;

import com.vrd.bigdata.service.DorisQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DorisQueryServiceImpl implements DorisQueryService {

    private static final Logger logger = LoggerFactory.getLogger(DorisQueryServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${doris.query.timeout:60}")
    private int queryTimeout;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public Map<String, Object> querySignals(Long vehicleId, String signalName, String startTime, String endTime) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT id, vehicle_id, vin, signal_name, signal_value, signal_unit, ");
            sql.append("signal_type, longitude, latitude, speed, collect_time ");
            sql.append("FROM vrd_bigdata.vehicle_signals ");
            sql.append("WHERE vehicle_id = ").append(vehicleId);
            sql.append(" AND dt >= '").append(startTime.substring(0, 10)).append("'");
            sql.append(" AND dt <= '").append(endTime.substring(0, 10)).append("'");
            
            if (signalName != null && !signalName.isEmpty()) {
                sql.append(" AND signal_name = '").append(signalName).append("'");
            }
            
            sql.append(" AND collect_time >= '").append(startTime).append("'");
            sql.append(" AND collect_time <= '").append(endTime).append("'");
            sql.append(" ORDER BY collect_time DESC");
            sql.append(" LIMIT 10000");
            
            List<Map<String, Object>> data = executeQuery(sql.toString());
            
            result.put("data", data);
            result.put("count", data.size());
            result.put("success", true);
            result.put("queryTime", System.currentTimeMillis() - start);
            
            logger.info("查询车辆 {} 信号数据，耗时 {}ms，返回 {} 条记录", 
                vehicleId, System.currentTimeMillis() - start, data.size());
            
        } catch (Exception e) {
            logger.error("查询信号数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("queryTime", System.currentTimeMillis() - start);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> queryLogs(Long vehicleId, String ecuType, String startTime, String endTime) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT id, vehicle_id, vin, ecu_type, error_code, error_level, ");
            sql.append("error_desc, log_content, longitude, latitude, speed, rpm, collect_time ");
            sql.append("FROM vrd_bigdata.ecu_logs ");
            sql.append("WHERE vehicle_id = ").append(vehicleId);
            sql.append(" AND dt >= '").append(startTime.substring(0, 10)).append("'");
            sql.append(" AND dt <= '").append(endTime.substring(0, 10)).append("'");
            
            if (ecuType != null && !ecuType.isEmpty()) {
                sql.append(" AND ecu_type = '").append(ecuType).append("'");
            }
            
            sql.append(" AND collect_time >= '").append(startTime).append("'");
            sql.append(" AND collect_time <= '").append(endTime).append("'");
            sql.append(" ORDER BY collect_time DESC");
            sql.append(" LIMIT 1000");
            
            List<Map<String, Object>> data = executeQuery(sql.toString());
            
            result.put("data", data);
            result.put("count", data.size());
            result.put("success", true);
            result.put("queryTime", System.currentTimeMillis() - start);
            
            logger.info("查询车辆 {} 日志数据，耗时 {}ms，返回 {} 条记录", 
                vehicleId, System.currentTimeMillis() - start, data.size());
            
        } catch (Exception e) {
            logger.error("查询日志数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("queryTime", System.currentTimeMillis() - start);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> queryDiagnostics(Long vehicleId, String startTime, String endTime) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT id, vehicle_id, vin, diagnostic_type, diagnostic_result, ");
            sql.append("overall_health_score, system_count, error_count, warning_count, ");
            sql.append("diagnostic_time ");
            sql.append("FROM vrd_bigdata.diagnostics ");
            sql.append("WHERE vehicle_id = ").append(vehicleId);
            sql.append(" AND dt >= '").append(startTime.substring(0, 10)).append("'");
            sql.append(" AND dt <= '").append(endTime.substring(0, 10)).append("'");
            sql.append(" AND diagnostic_time >= '").append(startTime).append("'");
            sql.append(" AND diagnostic_time <= '").append(endTime).append("'");
            sql.append(" ORDER BY diagnostic_time DESC");
            sql.append(" LIMIT 100");
            
            List<Map<String, Object>> data = executeQuery(sql.toString());
            
            result.put("data", data);
            result.put("count", data.size());
            result.put("success", true);
            result.put("queryTime", System.currentTimeMillis() - start);
            
            logger.info("查询车辆 {} 诊断数据，耗时 {}ms，返回 {} 条记录", 
                vehicleId, System.currentTimeMillis() - start, data.size());
            
        } catch (Exception e) {
            logger.error("查询诊断数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("queryTime", System.currentTimeMillis() - start);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> aggregateSignals(Long vehicleId, String signalName, String startTime, String endTime) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append("COUNT(*) as total_count, ");
            sql.append("AVG(CAST(signal_value AS DOUBLE)) as avg_value, ");
            sql.append("MIN(CAST(signal_value AS DOUBLE)) as min_value, ");
            sql.append("MAX(CAST(signal_value AS DOUBLE)) as max_value, ");
            sql.append("dt as date, ");
            sql.append("dt_hour as hour ");
            sql.append("FROM vrd_bigdata.vehicle_signals ");
            sql.append("WHERE vehicle_id = ").append(vehicleId);
            sql.append(" AND signal_name = '").append(signalName).append("'");
            sql.append(" AND dt >= '").append(startTime.substring(0, 10)).append("'");
            sql.append(" AND dt <= '").append(endTime.substring(0, 10)).append("'");
            sql.append(" GROUP BY dt, dt_hour ");
            sql.append(" ORDER BY dt, dt_hour");
            
            List<Map<String, Object>> data = executeQuery(sql.toString());
            
            result.put("data", data);
            result.put("count", data.size());
            result.put("success", true);
            result.put("queryTime", System.currentTimeMillis() - start);
            
            logger.info("聚合车辆 {} 信号 {}，耗时 {}ms", 
                vehicleId, signalName, System.currentTimeMillis() - start);
            
        } catch (Exception e) {
            logger.error("聚合信号数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("queryTime", System.currentTimeMillis() - start);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getAvailableDates(String dataType) {
        try {
            String tableName;
            switch (dataType) {
                case "signals":
                    tableName = "vehicle_signals";
                    break;
                case "logs":
                    tableName = "ecu_logs";
                    break;
                case "diagnostics":
                    tableName = "diagnostics";
                    break;
                default:
                    tableName = "vehicle_signals";
            }
            
            String sql = String.format(
                "SELECT DISTINCT dt as date FROM vrd_bigdata.%s ORDER BY dt DESC LIMIT 30", 
                tableName);
            
            return executeQuery(sql);
        } catch (Exception e) {
            logger.error("获取可用日期失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> getStatistics(String dataType, String startTime, String endTime) {
        long start = System.currentTimeMillis();
        Map<String, Object> stats = new HashMap<>();
        
        try {
            String tableName;
            switch (dataType) {
                case "signals":
                    tableName = "vehicle_signals";
                    break;
                case "logs":
                    tableName = "ecu_logs";
                    break;
                case "diagnostics":
                    tableName = "diagnostics";
                    break;
                default:
                    tableName = "vehicle_signals";
            }
            
            String startDate = startTime.substring(0, 10);
            String endDate = endTime.substring(0, 10);
            
            String countSql = String.format(
                "SELECT COUNT(*) as total_count FROM vrd_bigdata.%s WHERE dt >= '%s' AND dt <= '%s'",
                tableName, startDate, endDate);
            
            List<Map<String, Object>> countResult = executeQuery(countSql);
            if (!countResult.isEmpty()) {
                stats.put("totalRecords", countResult.get(0).get("total_count"));
            }
            
            String vehicleCountSql = String.format(
                "SELECT COUNT(DISTINCT vehicle_id) as vehicle_count FROM vrd_bigdata.%s WHERE dt >= '%s' AND dt <= '%s'",
                tableName, startDate, endDate);
            
            List<Map<String, Object>> vehicleResult = executeQuery(vehicleCountSql);
            if (!vehicleResult.isEmpty()) {
                stats.put("vehicleCount", vehicleResult.get(0).get("vehicle_count"));
            }
            
            stats.put("dataType", dataType);
            stats.put("startDate", startDate);
            stats.put("endDate", endDate);
            stats.put("success", true);
            stats.put("queryTime", System.currentTimeMillis() - start);
            
            logger.info("统计 {} 数据，耗时 {}ms", dataType, System.currentTimeMillis() - start);
            
        } catch (Exception e) {
            logger.error("获取统计数据失败: {}", e.getMessage(), e);
            stats.put("success", false);
            stats.put("error", e.getMessage());
            stats.put("queryTime", System.currentTimeMillis() - start);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getSignalTrend(Long vehicleId, String signalName, String startTime, String endTime) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append("dt as date, ");
            sql.append("AVG(CAST(signal_value AS DOUBLE)) as avg_value, ");
            sql.append("MIN(CAST(signal_value AS DOUBLE)) as min_value, ");
            sql.append("MAX(CAST(signal_value AS DOUBLE)) as max_value, ");
            sql.append("COUNT(*) as data_count ");
            sql.append("FROM vrd_bigdata.vehicle_signals ");
            sql.append("WHERE vehicle_id = ").append(vehicleId);
            sql.append(" AND signal_name = '").append(signalName).append("'");
            sql.append(" AND dt >= '").append(startTime.substring(0, 10)).append("'");
            sql.append(" AND dt <= '").append(endTime.substring(0, 10)).append("'");
            sql.append(" GROUP BY dt ");
            sql.append(" ORDER BY dt");
            
            List<Map<String, Object>> data = executeQuery(sql.toString());
            
            result.put("data", data);
            result.put("count", data.size());
            result.put("success", true);
            result.put("queryTime", System.currentTimeMillis() - start);
            
        } catch (Exception e) {
            logger.error("获取信号趋势失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getVehicleHealthReport(Long vehicleId, String startTime, String endTime) {
        long start = System.currentTimeMillis();
        Map<String, Object> report = new HashMap<>();
        
        try {
            String startDate = startTime.substring(0, 10);
            String endDate = endTime.substring(0, 10);
            
            String healthSql = String.format(
                "SELECT " +
                "  AVG(overall_health_score) as avg_health_score, " +
                "  COUNT(*) as diagnostic_count, " +
                "  SUM(error_count) as total_errors, " +
                "  SUM(warning_count) as total_warnings " +
                "FROM vrd_bigdata.diagnostics " +
                "WHERE vehicle_id = %d AND dt >= '%s' AND dt <= '%s'",
                vehicleId, startDate, endDate);
            
            List<Map<String, Object>> healthData = executeQuery(healthSql);
            if (!healthData.isEmpty()) {
                report.put("healthSummary", healthData.get(0));
            }
            
            String trendSql = String.format(
                "SELECT dt as date, overall_health_score " +
                "FROM vrd_bigdata.diagnostics " +
                "WHERE vehicle_id = %d AND dt >= '%s' AND dt <= '%s' " +
                "ORDER BY dt",
                vehicleId, startDate, endDate);
            
            List<Map<String, Object>> trendData = executeQuery(trendSql);
            report.put("healthTrend", trendData);
            
            String errorSql = String.format(
                "SELECT error_code, error_level, COUNT(*) as count " +
                "FROM vrd_bigdata.ecu_logs " +
                "WHERE vehicle_id = %d AND dt >= '%s' AND dt <= '%s' " +
                "GROUP BY error_code, error_level " +
                "ORDER BY count DESC " +
                "LIMIT 10",
                vehicleId, startDate, endDate);
            
            List<Map<String, Object>> errorData = executeQuery(errorSql);
            report.put("topErrors", errorData);
            
            report.put("vehicleId", vehicleId);
            report.put("startDate", startDate);
            report.put("endDate", endDate);
            report.put("success", true);
            report.put("queryTime", System.currentTimeMillis() - start);
            
            logger.info("生成车辆 {} 健康报告，耗时 {}ms", vehicleId, System.currentTimeMillis() - start);
            
        } catch (Exception e) {
            logger.error("生成健康报告失败: {}", e.getMessage(), e);
            report.put("success", false);
            report.put("error", e.getMessage());
            report.put("queryTime", System.currentTimeMillis() - start);
        }
        
        return report;
    }

    @Override
    public List<Map<String, Object>> getTopErrorCodes(String startTime, String endTime, int limit) {
        try {
            String startDate = startTime.substring(0, 10);
            String endDate = endTime.substring(0, 10);
            
            String sql = String.format(
                "SELECT error_code, ecu_type, error_level, COUNT(*) as error_count " +
                "FROM vrd_bigdata.ecu_logs " +
                "WHERE dt >= '%s' AND dt <= '%s' " +
                "GROUP BY error_code, ecu_type, error_level " +
                "ORDER BY error_count DESC " +
                "LIMIT %d",
                startDate, endDate, limit);
            
            return executeQuery(sql);
        } catch (Exception e) {
            logger.error("获取Top错误码失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> executeQuery(String sql) {
        try {
            jdbcTemplate.setQueryTimeout(queryTimeout);
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            logger.error("执行SQL失败: {}, SQL: {}", e.getMessage(), sql);
            throw e;
        }
    }
}
