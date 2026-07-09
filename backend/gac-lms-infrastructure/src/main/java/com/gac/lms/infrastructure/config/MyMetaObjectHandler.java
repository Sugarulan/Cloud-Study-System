package com.gac.lms.infrastructure.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.gac.lms.infrastructure.security.SecurityContextHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 审计字段自动填充。
 *
 * <p>对所有带 {@code @TableField(fill = FieldFill.INSERT/UPDATE)} 的字段：
 * <ul>
 *   <li>INSERT 时填充 createBy / createTime / updateBy / updateTime</li>
 *   <li>UPDATE 时填充 updateBy / updateTime</li>
 * </ul>
 * 使用 {@code strictInsertFill} / {@code strictUpdateFill}，<b>仅在字段值为 null 时填充</b>，
 * 避免覆盖业务代码手动设置的值。</p>
 *
 * <p>后台任务 / 启动初始化等无 SecurityContext 场景下，userId 取不到时回退为 {@code 0L}。</p>
 *
 * @author 王茗瑾
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Long userId = currentUserIdSafely();
        LocalDateTime now = LocalDateTime.now();

        this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long userId = currentUserIdSafely();
        LocalDateTime now = LocalDateTime.now();

        this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    /**
     * 安全地获取当前用户 ID；后台线程取不到时回退 0L（系统账号）。
     */
    private Long currentUserIdSafely() {
        try {
            Long uid = SecurityContextHelper.currentUserId();
            return uid != null ? uid : 0L;
        } catch (Exception ex) {
            return 0L;
        }
    }
}
