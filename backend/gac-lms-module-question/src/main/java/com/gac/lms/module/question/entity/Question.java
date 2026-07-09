package com.gac.lms.module.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gac.lms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 题目实体（对应表 question）。
 *
 * <p>对应 schema.sql §三.3.3.3 题目表。
 * 题型 type：SINGLE / MULTI / JUDGE / ESSAY / FILL。
 * 答案字段 answer_json 用 JSON 字符串存储：
 * <ul>
 *   <li>SINGLE → {"answer":"C"}</li>
 *   <li>MULTI  → {"answer":["A","B"]}</li>
 *   <li>JUDGE  → {"answer":true}</li>
 *   <li>ESSAY / FILL → {"answer":"参考答案"}</li>
 * </ul>
 * </p>
 *
 * @author 王茗瑾
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question")
public class Question extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 题型：SINGLE / MULTI / JUDGE / ESSAY / FILL */
    private String type;

    /** 题干（支持富文本/Markdown） */
    private String stem;

    /** 解析 */
    private String analysis;

    /** 难度：1=易，2=较易，3=中，4=较难，5=难 */
    private Integer difficulty;

    /** 默认分数 */
    private BigDecimal defaultScore;

    /** 分类 ID（预留） */
    private Long categoryId;

    /** 答案 JSON（按 type 解释） */
    private String answerJson;

    /** 状态：0=草稿，1=已发布 */
    private Integer status;
}
