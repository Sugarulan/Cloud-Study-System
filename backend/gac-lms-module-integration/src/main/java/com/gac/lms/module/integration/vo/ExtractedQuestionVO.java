package com.gac.lms.module.integration.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AI 抽取出来的题目。
 *
 * @author 方雨菲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedQuestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long docId;

    /** 生���的题目列表 */
    private List<Question> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Question implements Serializable {
        private static final long serialVersionUID = 1L;

        /** SINGLE / MULTI / JUDGE / FILL / ESSAY */
        private String type;

        /** 题干 */
        private String stem;

        /** 选项（选择题） */
        private List<Option> options;

        /** 正确答案 */
        private String correctAnswer;

        /** 知识点标签 */
        private String knowledgePoint;

        /** 难度 */
        private String difficulty;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option implements Serializable {
        private static final long serialVersionUID = 1L;
        private String key;
        private String value;
    }
}
