package com.gac.lms.module.exam.service;

import com.gac.lms.module.exam.dto.SaveAllRequest;
import com.gac.lms.module.exam.dto.SaveAnswerRequest;
import com.gac.lms.module.exam.vo.PaperRenderVO;
import com.gac.lms.module.exam.vo.RemainingTimeVO;
import com.gac.lms.module.exam.vo.SubmitResultVO;
import com.gac.lms.module.exam.vo.TakingSnapshotVO;

/**
 * 在线作答 Service。
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li>试卷渲染（含首次进入初始化）</li>
 *   <li>答题暂存（Redis Hash + 乐观锁版本号）</li>
 *   <li>断点续答（读取 Redis 快照）</li>
 *   <li>交卷（持久化 + 触发评卷）</li>
 *   <li>剩余时间计算</li>
 * </ul>
 *
 * @author 方雨菲
 */
public interface ExamTakingService {

    /**
     * 获取试卷渲染数据（学员进入考试时调用）。
     *
     * <p>首次进入会初始化 Redis 快照（版本号=0）。</p>
     */
    PaperRenderVO renderPaper(Long examId, Long userId);

    /**
     * 单题暂存（含乐观锁版本检查）。
     *
     * @return 更新后的版本号
     */
    Integer saveAnswer(Long examId, Long userId, SaveAnswerRequest request);

    /**
     * 批量暂存。
     */
    Integer saveAll(Long examId, Long userId, SaveAllRequest request);

    /**
     * 获取当前答卷快照（断点续答）。
     */
    TakingSnapshotVO getSnapshot(Long examId, Long userId);

    /**
     * 获取考试剩余时间。
     */
    RemainingTimeVO getRemaining(Long examId, Long userId);

    /**
     * 交卷（持久化 + 清空 Redis 快照）。
     */
    SubmitResultVO submit(Long examId, Long userId);

    /**
     * 自动交卷（定时任务调用 / 超时触发）。
     */
    SubmitResultVO autoSubmit(Long examId, Long userId);
}
