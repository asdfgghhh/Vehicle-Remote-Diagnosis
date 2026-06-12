package com.vrd.common.clickhouse;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClickHouseMigration {
    
    private String version;
    private String description;
    private List<String> sqlStatements = new ArrayList<>();
    private boolean runAlways = false;

    public ClickHouseMigration(String version, String description) {
        this.version = version;
        this.description = description;
    }

    public ClickHouseMigration addSql(String sql) {
        this.sqlStatements.add(sql);
        return this;
    }

    public ClickHouseMigration runAlways() {
        this.runAlways = true;
        return this;
    }
}
