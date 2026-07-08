package com.gac.lms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * GAC-LMS 启动类。
 *
 * <p>W1 阶段：基础骨架，演示 5 个模块 + AI 模块 + 公共组件可启动。</p>
 * <p>W2 阶段：接入 MySQL / Redis 数据，开放业务接口。</p>
 *
 * @author 方雨菲
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.gac.lms.common",
        "com.gac.lms.infrastructure",
        "com.gac.lms.ai",
        "com.gac.lms.module",
        "com.gac.lms"
})
@MapperScan("com.gac.lms.**.mapper")
public class GacLmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GacLmsApplication.class, args);
        System.out.println("""

                ====================================================
                  GAC-LMS 启动成功（W1 基座）
                  Swagger UI: http://localhost:8080/doc.html
                  Knife4j   : http://localhost:8080/swagger-ui.html
                  AI 健康   : http://localhost:8080/api/v1/ai/invoke (POST)
                ====================================================
                """);
    }
}
