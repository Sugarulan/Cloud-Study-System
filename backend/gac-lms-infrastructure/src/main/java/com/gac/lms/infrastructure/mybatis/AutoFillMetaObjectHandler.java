package com.gac.lms.infrastructure.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.gac.lms.common.constants.CommonConstants;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充：
 * <ul>
 *   <li>{@code create_time} / {@code create_by}：插入时填充</li>
 *   <li>{@code update_time} / {@code update_by}：插入 + 更新时填充</li>
 * </ul>
 *
 * <p>W2 简化：用 {@link CommonConstants#SUPER_ADMIN_ID} 作为默认值。
 * 后续接入 JWT 后改为从 {@code SecurityContextHolder} 读取。</p>
 *
 * @author 方雨菲
 */
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", Long.class, CommonConstants.SUPER_ADMIN_ID);
        this.strictInsertFill(metaObject, "updateBy", Long.class, CommonConstants.SUPER_ADMIN_ID);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", Long.class, CommonConstants.SUPER_ADMIN_ID);
    }
}
