package com.gac.lms.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 鉴权过滤器：从 Header 提取 Token，解析后写入 SecurityContext。
 *
 * <p>每个请求只执行一次（{@link OncePerRequestFilter}）。
 * Token 无效或缺失时，<b>不清空</b> SecurityContext，由后续 SecurityFilterChain 决定是否放行。</p>
 *
 * @author 王茗瑾
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProps;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            Claims claims = jwtUtil.parse(token);
            if (claims != null) {
                LoginUser user = jwtUtil.toLoginUser(claims);
                if (user != null) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    user,                         // principal
                                    null,                         // credentials
                                    toAuthorities(user));         // authorities
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("[JWT] authenticated user={} roles={}", user.getUsername(), user.getRoles());
                }
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * 从请求 Header 提取 Token（剥离 "Bearer " 前缀）。
     */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(jwtProps.getHeader());
        if (header == null || !header.startsWith(jwtProps.getPrefix())) {
            return null;
        }
        return header.substring(jwtProps.getPrefix().length()).trim();
    }

    /**
     * 把角色和权限拼成 Spring Security 所需的 GrantedAuthority 列表。
     * 角色加 ROLE_ 前缀，权限原样。
     */
    private List<SimpleGrantedAuthority> toAuthorities(LoginUser user) {
        return java.util.stream.Stream.concat(
                        safeStream(user.getRoles()).map(r -> "ROLE_" + r),
                        safeStream(user.getPermissions())
                )
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private <T> java.util.stream.Stream<T> safeStream(List<T> list) {
        return list == null ? java.util.stream.Stream.empty() : list.stream();
    }
}
