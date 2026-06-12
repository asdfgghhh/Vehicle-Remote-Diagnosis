package com.vrd.common.clickhouse;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@ConditionalOnProperty(name = "clickhouse.enabled", havingValue = "true", matchIfMissing = true)
public class ClickHouseSchemaManager {

    private static final String MIGRATION_TABLE = "__schema_migrations";
    
    private final ClickHouseProperties properties;
    private final ClickHouseHttpClient clickHouseHttpClient;
    private final List<ClickHouseMigration> migrations = new ArrayList<>();

    public ClickHouseSchemaManager(ClickHouseProperties properties, ClickHouseHttpClient clickHouseHttpClient) {
        this.properties = properties;
        this.clickHouseHttpClient = clickHouseHttpClient;
        registerMigrations();
    }

    private void registerMigrations() {
        migrations.add(new ClickHouseMigration("1.0.0", "Create vehicle_signal_records table")
                .addSql("""
                        CREATE TABLE IF NOT EXISTS vehicle_signal_records (
                            id UInt64,
                            vin String,
                            vehicle_id UInt64 DEFAULT 0,
                            signal_name LowCardinality(String),
                            signal_value String,
                            numeric_value Float64,
                            unit LowCardinality(String),
                            timestamp UInt64,
                            signal_time DateTime,
                            message_name String,
                            message_id UInt32 DEFAULT 0,
                            create_time DateTime DEFAULT now()
                        ) ENGINE = MergeTree()
                        PARTITION BY toYYYYMM(signal_time)
                        ORDER BY (vin, signal_name, signal_time)
                        SETTINGS index_granularity = 8192
                        """));

        migrations.add(new ClickHouseMigration("1.0.1", "Create ecu_log_records table")
                .addSql("""
                        CREATE TABLE IF NOT EXISTS ecu_log_records (
                            id UInt64,
                            vin String,
                            ecu_type LowCardinality(String),
                            log_start_time DateTime,
                            log_end_time DateTime,
                            upload_start_time DateTime,
                            upload_end_time DateTime,
                            storage_address String,
                            storage_key String,
                            storage_type LowCardinality(String),
                            file_name String,
                            file_size UInt64,
                            file_md5 String,
                            create_time DateTime DEFAULT now()
                        ) ENGINE = MergeTree()
                        PARTITION BY toYYYYMM(upload_start_time)
                        ORDER BY (ecu_type, vin, upload_start_time)
                        SETTINGS index_granularity = 8192
                        """));

        migrations.add(new ClickHouseMigration("1.0.2", "Add index on vehicle_signal_records")
                .addSql("ALTER TABLE vehicle_signal_records ADD INDEX signal_name_idx signal_name TYPE minmax GRANULARITY 1"));

        migrations.add(new ClickHouseMigration("1.0.3", "Create diagnostic_records table")
                .addSql("""
                        CREATE TABLE IF NOT EXISTS diagnostic_records (
                            id UInt64,
                            vin String,
                            vehicle_id UInt64 DEFAULT 0,
                            diagnostic_code String,
                            diagnostic_name String,
                            severity LowCardinality(String),
                            status LowCardinality(String),
                            occur_time DateTime,
                            clear_time DateTime,
                            description String,
                            create_time DateTime DEFAULT now()
                        ) ENGINE = MergeTree()
                        PARTITION BY toYYYYMM(occur_time)
                        ORDER BY (vin, diagnostic_code, occur_time)
                        SETTINGS index_granularity = 8192
                        """));

        migrations.add(new ClickHouseMigration("1.0.4", "Create vehicle_status_snapshot table")
                .addSql("""
                        CREATE TABLE IF NOT EXISTS vehicle_status_snapshot (
                            id UInt64,
                            vin String,
                            vehicle_id UInt64 DEFAULT 0,
                            status LowCardinality(String),
                            battery_level Float64,
                            mileage UInt64,
                            fuel_level Float64,
                            engine_temp Float64,
                            snapshot_time DateTime,
                            create_time DateTime DEFAULT now()
                        ) ENGINE = MergeTree()
                        PARTITION BY toYYYYMM(snapshot_time)
                        ORDER BY (vin, snapshot_time)
                        SETTINGS index_granularity = 8192
                        """));
    }

    @PostConstruct
    public void migrate() {
        if (!properties.isEnabled()) {
            log.info("ClickHouse schema migration is disabled");
            return;
        }

        try {
            String db = properties.getDatabase();
            ensureDatabaseExists(db);
            ensureMigrationTableExists(db);
            
            Set<String> appliedMigrations = getAppliedMigrations(db);
            
            Collections.sort(migrations, Comparator.comparing(ClickHouseMigration::getVersion));
            
            for (ClickHouseMigration migration : migrations) {
                if (!migration.isRunAlways() && appliedMigrations.contains(migration.getVersion())) {
                    log.debug("Migration {} already applied, skipping", migration.getVersion());
                    continue;
                }
                
                applyMigration(db, migration);
                if (!migration.isRunAlways()) {
                    recordMigration(db, migration);
                }
                
                log.info("Applied migration {}: {}", migration.getVersion(), migration.getDescription());
            }
            
            log.info("ClickHouse schema migration completed successfully");
            
        } catch (Exception e) {
            log.error("ClickHouse schema migration failed", e);
        }
    }

    private void ensureDatabaseExists(String db) {
        clickHouseHttpClient.execute("CREATE DATABASE IF NOT EXISTS " + db);
    }

    private void ensureMigrationTableExists(String db) {
        String sql = String.format("""
                CREATE TABLE IF NOT EXISTS %s.%s (
                    version String,
                    description String,
                    applied_at DateTime DEFAULT now()
                ) ENGINE = MergeTree()
                ORDER BY (version)
                SETTINGS index_granularity = 8192
                """, db, MIGRATION_TABLE);
        clickHouseHttpClient.execute(sql);
    }

    private Set<String> getAppliedMigrations(String db) {
        Set<String> applied = new HashSet<>();
        try {
            String sql = String.format("SELECT version FROM %s.%s", db, MIGRATION_TABLE);
            String result = clickHouseHttpClient.query(sql);
            if (result != null && !result.isEmpty()) {
                for (String line : result.split("\n")) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        applied.add(line);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get applied migrations: {}", e.getMessage());
        }
        return applied;
    }

    private void applyMigration(String db, ClickHouseMigration migration) {
        for (String sql : migration.getSqlStatements()) {
            clickHouseHttpClient.execute(sql, db);
        }
    }

    private void recordMigration(String db, ClickHouseMigration migration) {
        String sql = String.format("INSERT INTO %s.%s (version, description) VALUES ('%s', '%s')",
                db, MIGRATION_TABLE, 
                migration.getVersion().replace("'", "\\'"), 
                migration.getDescription().replace("'", "\\'"));
        clickHouseHttpClient.execute(sql);
    }

    public void addMigration(ClickHouseMigration migration) {
        this.migrations.add(migration);
    }
}
