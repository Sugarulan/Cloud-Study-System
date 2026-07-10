package com.gac.lms.module.knowledge.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库文档历史版本。
 *
 * <p>每次更新文档会创建一条快照，用于版本回滚与 diff 对比。</p>
 *
 * @author 方雨菲
 */
@Data
@TableName("knowledge_doc_version")
public class KnowledgeDocVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 文档 ID */
    private Long docId;

    /** 版本号 */
    private Integer version;

    /** 当时的标题 */
    private String title;

    /** 当时的内容快照 */
    private String content;

    /** 变更日志 */
    private String changeLog;

    /** 操作人 */
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
