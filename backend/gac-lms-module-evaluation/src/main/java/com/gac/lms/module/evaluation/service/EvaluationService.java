package com.gac.lms.module.evaluation.service;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.evaluation.dto.AiEvaluateRequest;
import com.gac.lms.module.evaluation.dto.AutoEvaluateRequest;
import com.gac.lms.module.evaluation.dto.ManualEvaluateRequest;
import com.gac.lms.module.evaluation.vo.EvaluationActionVO;
import com.gac.lms.module.evaluation.vo.EvaluationResultVO;
import com.gac.lms.module.evaluation.vo.PendingItemVO;

/**
 * 评卷 Service。
 *
 * <h3>状态机</h3>
 * <pre>
 *   0=待评分 ─autoEvaluate─▶ 1=部分 ─aiEvaluate─▶ 1=部分（AI 已评）
 *                                       │
 *                                       └─manualEvaluate─▶ 2=已评
 *                                                                  │
 *                                                                  ├─review──▶ 3=已复核
 *                                                                  │
 *                                                                  └─publish─▶ 4=已发布
 * </pre>
 *
 * @author 方雨菲
 */
public interface EvaluationService {

    /**
     * 客观题自动评阅。
     */
    EvaluationResultVO autoEvaluate(AutoEvaluateRequest request);

    /**
     * 主观题 AI 评阅（调用 AI Provider，失败降级到 Mock）。
     */
    EvaluationResultVO aiEvaluate(AiEvaluateRequest request);

    /**
     * 人工评阅（覆盖 AI 结果或对客观题纠错）。
     */
    EvaluationResultVO manualEvaluate(ManualEvaluateRequest request, Long operatorId);

    /**
     * 评卷复核（仅状态 = 2 已评时可调用）。
     */
    EvaluationActionVO review(Long gradeId, Long operatorId);

    /**
     * 成绩发布（仅状态 = 3 已复核时可调用）。
     */
    EvaluationActionVO publish(Long gradeId, Long operatorId);

    /**
     * 待评卷列表（人工评卷工作台）。
     */
    PageResult<PendingItemVO> listPending(Long examId, Integer status, int pageNum, int pageSize);

    /**
     * 获取成绩详情。
     */
    EvaluationResultVO getDetail(Long gradeId);
}
