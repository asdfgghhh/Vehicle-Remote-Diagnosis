package com.vrd.bigdata.config;

import com.clickhouse.jdbc.ClickHouseDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class ClickHouseDataSourceConfig {

    private final BigDataStorageProperties storageProperties;

    public ClickHouseDataSourceConfig(BigDataStorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Bean
    @Primary
    public DataSource clickHouseDataSource() throws SQLException {
        BigDataStorageProperties.ClickHouseProperties props = storageProperties.getClickhouse();
        
        String url = String.format("jdbc:clickhouse://%s:%d/%s",
                props.getHost(),
                props.getPort(),
                props.getDatabase());

        Properties properties = new Properties();
        properties.setProperty("user", props.getUsername());
        properties.setProperty("password", props.getPassword());
        properties.setProperty("connectionTimeout", String.valueOf(props.getConnectionTimeout()));
        properties.setProperty("socketTimeout", String.valueOf(props.getSocketTimeout()));

        return new ClickHouseDataSource(url, properties);
    }
}
