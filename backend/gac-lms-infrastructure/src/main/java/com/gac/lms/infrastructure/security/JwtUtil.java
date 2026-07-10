package com.gac.lms.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT 工具类：生成 / 解析 / 校验 Token。
 *
 * <p><b>Token Claims：</b></p>
 * <ul>
 *   <li>{@code sub}      —— 账号 ID</li>
 *   <li>{@code username} —— 登录名</li>
 *   <li>{@code roles}    —— 角色编码列表</li>
 *   <li>{@code perms}    —— 权限编码列表</li>
 *   <li>{@code iat / exp}—— 标准时间字段</li>
 * </ul>
 *
 * @author 王茗瑾
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    public static final String CLAIM_USER_ID = "sub";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMS = "perms";

    private final JwtProperties props;

    /**
     * 生成 Token。
     */
    public String generate(LoginUser user) {
        long now = System.currentTimeMillis();
        long expire = now + props.getExpireMinutes() * 60_000L;

        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_ROLES, user.getRoles())
                .claim(CLAIM_PERMS, user.getPermissions())
                .issuedAt(new Date(now))
                .expiration(new Date(expire))
                .signWith(buildKey())
                .compact();
    }

    /**
     * 解析 Token，失败返回 null（不抛异常）。
     *
     * <p>失败原因：签名错误 / Token 过期 / 格式错误。</p>
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(buildKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("[JwtUtil] parse token failed: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * 从 Claims 还原 LoginUser。
     */
    public LoginUser toLoginUser(Claims claims) {
        if (claims == null) {
            return null;
        }
        LoginUser u = new LoginUser();
        u.setUserId(Long.valueOf(claims.getSubject()));
        u.setUsername(claims.get(CLAIM_USERNAME, String.class));
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get(CLAIM_ROLES, List.class);
        @SuppressWarnings("unchecked")
        List<String> perms = claims.get(CLAIM_PERMS, List.class);
        u.setRoles(roles == null ? List.of() : roles);
        u.setPermissions(perms == null ? List.of() : perms);
        return u;
    }

    private SecretKey buildKey() {
        // 密钥至少 32 字节（HS256 最低要求），不足则补足
        byte[] bytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    /** 仅供调试用：返回 Map 形式的 Claims */
    public Map<String, Object> debugClaims(Claims claims) {
        return claims == null ? Map.of() : claims;
    }
}
