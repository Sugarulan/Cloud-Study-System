package com.gac.lms.module.exam.service;

import com.gac.lms.module.exam.vo.PaperRenderVO;

/**
 * 试卷查询服务 —— 与王茗瑾的试卷模块对齐的接口。
 *
 * <p>W2 阶段：使用 Mock 实现。
 * W3 联调：替换为 {@code @RestTemplate} 调用王茗瑾�� {@code /api/v1/papers/{id}} 接口。</p>
 *
 * @author 方雨菲
 */
public interface PaperQueryService {

    /**
     * 获取试卷渲染数据（不含答案）。
     *
     * @param paperId 试卷 ID
     * @return 试卷渲染 VO
     */
    PaperRenderVO getPaper(Long paperId);

    /**
     * 获取试卷总分。
     */
    Integer getPaperTotalScore(Long paperId);

    /**
     * 获取试卷题数。
     */
    Integer getPaperQuestionCount(Long paperId);
}
