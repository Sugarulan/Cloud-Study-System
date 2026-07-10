package com.gac.lms.infrastructure.security;

import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityContext 工具类：从当前线程的 SecurityContext 取登录信息。
 *
 * <p>业务代码统一通过本类访问当前用户，避免到处写 {@code SecurityContextHolder.getContext()}。</p>
 *
 * @author 王茗瑾
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {}

    /**
     * 取当前登录用户（未登录返回 null）。
     */
    public static LoginUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        return principal instanceof LoginUser ? (LoginUser) principal : null;
    }

    /**
     * 取当前用户 ID（未登录抛 UNAUTHORIZED）。
     */
    public static Long currentUserId() {
        LoginUser u = currentUser();
        if (u == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return u.getUserId();
    }

    /**
     * 取当前用户名（未登录抛 UNAUTHORIZED）。
     */
    public static String currentUsername() {
        LoginUser u = currentUser();
        if (u == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return u.getUsername();
    }

    /**
     * 判断当前用户是否拥有某个角色。
     */
    public static boolean hasRole(String role) {
        LoginUser u = currentUser();
        return u != null && u.getRoles() != null && u.getRoles().contains(role);
    }

    /**
     * 判断当前用户是否拥有某个权限编码。
     */
    public static boolean hasPermission(String permission) {
        LoginUser u = currentUser();
        return u != null && u.getPermissions() != null && u.getPermissions().contains(permission);
    }
}
