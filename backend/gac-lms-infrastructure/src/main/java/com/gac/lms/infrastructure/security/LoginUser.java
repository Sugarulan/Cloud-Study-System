package com.gac.lms.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 登录用户主体 —— 登录成功后写入 SecurityContext。
 *
 * <p>本对象作为 {@code Authentication.principal} 存储，
 * 业务代码可通过 {@link SecurityContextHelper#currentUser()} 取出。</p>
 *
 * @author 王茗瑾
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 账号 ID（雪花算法主键） */
    private Long userId;

    /** 登录名 */
    private String username;

    /** 姓名（取自 person 表，登录接口冗余返回） */
    private String displayName;

    /** 角色编码列表，如 ["ADMIN"] 或 ["STUDENT"] */
    private List<String> roles;

    /** 权限编码列表，如 ["account:list", "account:create"] */
    private List<String> permissions;

    public LoginUser(Long userId, String username, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.roles = roles == null ? Collections.emptyList() : roles;
    }
}
