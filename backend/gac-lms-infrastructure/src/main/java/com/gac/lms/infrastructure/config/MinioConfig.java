package com.gac.lms.infrastructure.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "gac.lms.minio")
public class MinioConfig {

    /** MinIO 服务地址，例如 http://localhost:9000 */
    private String endpoint;

    /** 访问 Key */
    private String accessKey;

    /** 秘钥 */
    private String secretKey;

    /** 默认 Bucket */
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
