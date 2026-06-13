package com.vrd.common.bigdata.config;

import com.vrd.common.bigdata.BigDataClient;
import com.vrd.common.bigdata.BigDataProperties;
import com.vrd.common.bigdata.BigDataStorageType;
import com.vrd.common.bigdata.impl.ClickHouseBigDataClient;
import com.vrd.common.bigdata.impl.DorisBigDataClient;
import com.vrd.common.bigdata.impl.TDengineBigDataClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BigDataAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BigDataClient bigDataClient(BigDataProperties properties) {
        BigDataStorageType type = properties.getType();
        log.info("BigData storage type: {}", type);
        
        switch (type) {
            case DORIS:
                return new DorisBigDataClient(properties);
            case TDENGINE:
                return new TDengineBigDataClient(properties);
            case CLICKHOUSE:
            default:
                return new ClickHouseBigDataClient(properties);
        }
    }
}