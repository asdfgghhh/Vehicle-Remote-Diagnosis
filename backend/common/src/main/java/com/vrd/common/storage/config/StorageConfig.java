package com.vrd.common.storage.config;

import com.vrd.common.storage.StorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "storage")
public class StorageConfig {

    private StorageType type = StorageType.LOCAL;

    private Local local = new Local();

    private AliyunOss aliyunOss = new AliyunOss();

    private HuaweiObs huaweiObs = new HuaweiObs();

    private Minio minio = new Minio();

    @Data
    public static class Local {
        private String basePath = "/data/vrd/storage";
        private String baseUrl = "http://localhost:8080/storage";
    }

    @Data
    public static class AliyunOss {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
        private String baseUrl;
    }

    @Data
    public static class HuaweiObs {
        private String endpoint;
        private String accessKeyId;
        private String secretAccessKey;
        private String bucketName;
        private String baseUrl;
    }

    @Data
    public static class Minio {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private String baseUrl;
    }
}