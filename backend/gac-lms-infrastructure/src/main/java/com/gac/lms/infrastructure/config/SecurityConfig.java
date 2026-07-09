package com.gac.lms.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.Result;
import com.gac.lms.infrastructure.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Spring Security 配置（W2 起启用 JWT 鉴权）。
 *
 * <p>放行路径：</p>
 * <ul>
 *   <li>{@code /api/v1/auth/**}          —— 登录相关</li>
 *   <li>{@code /v3/api-docs/**}, {@code /swagger-ui/**}, {@code /doc.html}, {@code /webjars/**} —— 接口文档</li>
 *   <li>{@code /actuator/health}         —— 健康检查</li>
 *   <li>{@code OPTIONS}                  —— CORS 预检</li>
 * </ul>
 * 其他路径必须携带 {@code Authorization: Bearer <token>} 才能访问。
 *
 * @author 王茗瑾
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 强度 10（与 data.sql 预置的密码哈希一致）
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（CORS 跨域场景）
                .csrf(AbstractHttpConfigurer::disable)
                // 使用自定义 CORS 配置
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                // 无状态会话（JWT）
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 异常处理：401 / 403 返回统一的 Result JSON
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, resp, ex) -> writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((req, resp, ex) -> writeError(resp, HttpServletResponse.SC_FORBIDDEN, ErrorCode.FORBIDDEN))
                )
                // 路由授权
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 登录
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 接口文档
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/doc.html", "/webjars/**", "/favicon.ico").permitAll()
                        // 健康检查
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // 其他模块的 /health（W1 验收占位）
                        .requestMatchers("/api/v1/*/health").permitAll()
                        // 其余全部需要鉴权
                        .anyRequest().authenticated()
                )
                // 插入 JWT 过滤器
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 配置（开发环境开放所有来源，生产应由 Nginx 处理）。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }

    /**
     * 把 Spring Security 抛出的 401 / 403 包装为统一的 Result JSON。
     */
    private void writeError(HttpServletResponse resp, int status, ErrorCode code) throws java.io.IOException {
        resp.setStatus(status);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write(objectMapper.writeValueAsString(Result.fail(code)));
    }
}
