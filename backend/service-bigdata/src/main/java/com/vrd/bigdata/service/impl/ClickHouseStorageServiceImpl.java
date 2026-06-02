package com.vrd.bigdata.service.impl;

import com.vrd.bigdata.config.BigDataStorageProperties;
import com.vrd.bigdata.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("clickHouseStorageService")
public class ClickHouseStorageServiceImpl implements StorageService {

    private final JdbcTemplate jdbcTemplate;
    private final BigDataStorageProperties storageProperties;

    public ClickHouseStorageServiceImpl(DataSource dataSource, BigDataStorageProperties storageProperties) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.storageProperties = storageProperties;
        initTables();
    }

    private void initTables() {
        try {
            String createVehicleSignalsTable = """
                CREATE TABLE IF NOT EXISTS vehicle_signals (
                    vehicle_id String,
                    signal_time DateTime,
                    signal_name String,
                    signal_value Float64,
                    unit String,
                    create_time DateTime DEFAULT now()
                ) ENGINE = MergeTree()
                ORDER BY (vehicle_id, signal_time)
                PARTITION BY toYYYYMM(signal_time)
                """;

            String createEcuLogsTable = """
                CREATE TABLE IF NOT EXISTS ecu_logs (
                    vehicle_id String,
                    ecu_id String,
                    log_time DateTime,
                    log_level String,
                    log_message String,
                    create_time DateTime DEFAULT now()
                ) ENGINE = MergeTree()
                ORDER BY (vehicle_id, log_time)
                PARTITION BY toYYYYMM(log_time)
                """;

            String createDiagnosticsTable = """
                CREATE TABLE IF NOT EXISTS diagnostics (
                    vehicle_id String,
                    diagnostic_time DateTime,
                    fault_code String,
                    fault_description String,
                    severity String,
                    status String,
                    create_time DateTime DEFAULT now()
                ) ENGINE = MergeTree()
                ORDER BY (vehicle_id, diagnostic_time)
                PARTITION BY toYYYYMM(diagnostic_time)
                """;

            jdbcTemplate.execute(createVehicleSignalsTable);
            jdbcTemplate.execute(createEcuLogsTable);
            jdbcTemplate.execute(createDiagnosticsTable);
            log.info("ClickHouse tables initialized successfully");
        } catch (Exception e) {
            log.warn("Failed to initialize ClickHouse tables, assuming they exist: {}", e.getMessage());
        }
    }

    @Override
    public void saveVehicleSignal(String vehicleId, Map<String, Object> signalData) {
        String sql = "INSERT INTO vehicle_signals (vehicle_id, signal_time, signal_name, signal_value, unit) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                vehicleId,
                signalData.get("signal_time"),
                signalData.get("signal_name"),
                signalData.get("signal_value"),
                signalData.get("unit"));
    }

    @Override
    public void saveEcuLog(String vehicleId, Map<String, Object> logData) {
        String sql = "INSERT INTO ecu_logs (vehicle_id, ecu_id, log_time, log_level, log_message) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                vehicleId,
                logData.get("ecu_id"),
                logData.get("log_time"),
                logData.get("log_level"),
                logData.get("log_message"));
    }

    @Override
    public void saveDiagnostic(String vehicleId, Map<String, Object> diagnosticData) {
        String sql = "INSERT INTO diagnostics (vehicle_id, diagnostic_time, fault_code, fault_description, severity, status) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                vehicleId,
                diagnosticData.get("diagnostic_time"),
                diagnosticData.get("fault_code"),
                diagnosticData.get("fault_description"),
                diagnosticData.get("severity"),
                diagnosticData.get("status"));
    }

    @Override
    public Map<String, Object> queryVehicleSignals(String vehicleId, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("vehicleId", vehicleId);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("storageType", "clickhouse");
        result.put("data", jdbcTemplate.queryForList(
                "SELECT * FROM vehicle_signals WHERE vehicle_id = ? AND signal_time BETWEEN ? AND ?",
                vehicleId, startTime, endTime));
        return result;
    }

    @Override
    public Map<String, Object> queryEcuLogs(String vehicleId, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("vehicleId", vehicleId);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("storageType", "clickhouse");
        result.put("data", jdbcTemplate.queryForList(
                "SELECT * FROM ecu_logs WHERE vehicle_id = ? AND log_time BETWEEN ? AND ?",
                vehicleId, startTime, endTime));
        return result;
    }

    @Override
    public Map<String, Object> queryDiagnostics(String vehicleId, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("vehicleId", vehicleId);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("storageType", "clickhouse");
        result.put("data", jdbcTemplate.queryForList(
                "SELECT * FROM diagnostics WHERE vehicle_id = ? AND diagnostic_time BETWEEN ? AND ?",
                vehicleId, startTime, endTime));
        return result;
    }
}
