package com.gac.lms.module.account.service;

import com.gac.lms.module.account.dto.LoginCmd;
import com.gac.lms.module.account.dto.LoginResp;

/**
 * 认证服务（登录态相关）。
 *
 * <p>对外暴露的能力：</p>
 * <ul>
 *   <li>{@link #login(LoginCmd)}           —— 用户名密码登录，返回 JWT</li>
 *   <li>{@link #currentUserInfo()}         —— 查询当前登录用户的完整信息（供 /auth/me）</li>
 * </ul>
 *
 * @author 王茗瑾
 */
public interface AuthService {

    /**
     * 登录。失败抛 {@code BusinessException(UNAUTHORIZED)} 或 {@code (FORBIDDEN)}。
     */
    LoginResp login(LoginCmd cmd);

    /**
     * 当前登录用户的完整信息（含权限列表）。
     */
    LoginResp currentUserInfo();
}
