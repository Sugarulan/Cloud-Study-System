package com.gac.lms.infrastructure.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置属性（绑定 application.yml 的 gac.lms.jwt.*）。
 *
 * @author 王茗瑾
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "gac.lms.jwt")
public class JwtProperties {

    /**
     * HMAC-SHA 密钥（生产环境必须通过 JWT_SECRET 环境变量覆盖，至少 32 字节）。
     */
    private String secret;

    /**
     * Token 有效期（分钟）。
     */
    private Integer expireMinutes = 720;

    /**
     * HTTP Header 名，默认 {@code Authorization}。
     */
    private String header = "Authorization";

    /**
     * Token 前缀，默认 {@code "Bearer "}（注意末尾空格）。
     */
    private String prefix = "Bearer ";
}
