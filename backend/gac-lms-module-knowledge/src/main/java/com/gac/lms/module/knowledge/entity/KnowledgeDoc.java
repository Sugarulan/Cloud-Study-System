package com.gac.lms.module.knowledge.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库文档实体。
 *
 * <p>状态机：DRAFT(0) → PENDING(1) → PUBLISHED(2) → ARCHIVED(3)；分支：REJECTED(4)。</p>
 *
 * @author 方雨菲
 */
@Data
@TableName("knowledge_doc")
public class KnowledgeDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long categoryId;
    private String title;
    private String summary;

    /** 富文本内容 */
    private String content;

    /** 标签，逗号分隔 */
    private String tags;

    /** 状态：0=DRAFT 1=PENDING 2=PUBLISHED 3=ARCHIVED 4=REJECTED */
    private Integer status;

    /** 文档版本号（每次更新 +1） */
    private Integer version;

    /** 作者 ID */
    private Long authorId;

    /** 审核人 ID */
    private Long reviewerId;

    /** 发布时间 */
    private LocalDateTime publishedAt;

    /** 归档时间 */
    private LocalDateTime archivedAt;

    // ===== 公共字段 =====

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    /** MyBatis-Plus 乐观锁 */
    @Version
    private Integer rowVersion;
}
