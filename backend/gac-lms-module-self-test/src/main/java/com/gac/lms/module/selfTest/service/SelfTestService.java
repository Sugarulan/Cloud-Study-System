package com.gac.lms.module.selfTest.service;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.selfTest.vo.ExamItemVO;
import com.gac.lms.module.selfTest.vo.WrongQuestionVO;

/**
 * 个人测评 Service。
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li>待考列表：参考范围 - 已考 ＝ 待考</li>
 *   <li>已考列表：发布状态的 grade_record + 错题数</li>
 *   <li>错题本：分页 + 按知识点筛选（W3 接入题目服务）</li>
 *   <li>AI 解析：调用 LLMService，结果回写 + 缓存</li>
 * </ul>
 *
 * @author 方雨菲
 */
public interface SelfTestService {

    /**
     * 待考列表（已排除已考/进行中）。
     *
     * @param userId 学员 ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     */
    PageResult<ExamItemVO> listPendingExams(Long userId, long pageNum, long pageSize);

    /**
     * 已考列表（含成绩概要）。
     */
    PageResult<ExamItemVO> listFinishedExams(Long userId, long pageNum, long pageSize);

    /**
     * 错题本（分页）。
     *
     * @param userId 学员 ID
     * @param isMastered 是否已掌握（null = 全部）
     */
    PageResult<WrongQuestionVO> listWrongQuestions(Long userId, Integer isMastered,
                                                    long pageNum, long pageSize);

    /**
     * 错题详情 + AI 解析（若已生成）。
     */
    WrongQuestionVO getWrongQuestion(Long id, Long userId);

    /**
     * 触发 AI 解析（同步返回；W4 可改为异步）。
     */
    WrongQuestionVO triggerAiExplanation(Long id, Long userId);

    /**
     * 标记错题为已掌握。
     */
    void markAsMastered(Long id, Long userId);
}
