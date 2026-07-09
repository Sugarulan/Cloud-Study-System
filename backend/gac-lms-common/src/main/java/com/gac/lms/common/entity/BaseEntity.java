package com.gac.lms.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体公共基类。
 *
 * <p>所有业务实体统一继承本类，避免在每个 Entity 中重复定义审计字段。
 * 各字段对应 schema.sql 中约定的公共字段：</p>
 * <ul>
 *   <li>{@code create_by / create_time}   —— 创建人 / 创建时间（由 MetaObjectHandler 填充）</li>
 *   <li>{@code update_by / update_time}   —— 更新人 / 更新时间（由 MetaObjectHandler 填充）</li>
 *   <li>{@code deleted}                    —— 逻辑删除标记（MyBatis-Plus 自动过滤）</li>
 *   <li>{@code version}                    —— 乐观锁版本号</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * {@code
 * @TableName("account")
 * public class Account extends BaseEntity {
 *     private String username;
 *     private String passwordHash;
 * }
 * }
 * </pre>
 *
 * @author 王茗瑾
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 创建人 ID（由 MyBatis-Plus MetaObjectHandler 自动填充） */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间（由 MyBatis-Plus MetaObjectHandler 自动填充） */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 ID（由 MyBatis-Plus MetaObjectHandler 自动填充） */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 更新时间（由 MyBatis-Plus MetaObjectHandler 自动填充） */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记：0=未删，1=已删（MyBatis-Plus 自动过滤 deleted=0） */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /** 乐观锁版本号（更新时自动 +1，避免并发覆盖） */
    @Version
    @TableField("version")
    private Integer version;
}
