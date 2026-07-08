package com.gac.lms.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 配置。
 *
 * <p>访问地址：http://localhost:8080/doc.html</p>
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI gacLmsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("广汽云学习管理系统（GAC-LMS）API")
                        .description("GAC-LMS 后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("方雨菲").email("fangyufei@gac-lms.local")))
                .components(new Components().addSecuritySchemes("bearer-jwt",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
