package com.vrd.bigdata.kafka;

import com.alibaba.fastjson2.JSON;
import com.vrd.bigdata.service.HdfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class KafkaDataConsumer {

    @Autowired
    private HdfsService hdfsService;

    @Value("${bigdata.storage.base-path}")
    private String basePath;

    @Value("${bigdata.storage.partitions[0].path}")
    private String signalsPath;

    @Value("${bigdata.storage.partitions[1].path}")
    private String logsPath;

    @KafkaListener(topics = "vehicle-signals", groupId = "bigdata-signals")
    public void consumeSignals(String message) {
        try {
            String dateStr = LocalDateTime.now().toLocalDate().toString();
            String path = signalsPath + "/" + dateStr + "/signals_" + System.currentTimeMillis() + ".json";
            
            hdfsService.saveToHdfs(message, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "ecu-logs", groupId = "bigdata-logs")
    public void consumeLogs(String message) {
        try {
            String dateStr = LocalDateTime.now().toLocalDate().toString();
            String path = logsPath + "/" + dateStr + "/logs_" + System.currentTimeMillis() + ".json";
            
            hdfsService.saveToHdfs(message, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
