package com.gac.lms.module.grade.service;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.grade.dto.GradeQueryRequest;
import com.gac.lms.module.grade.vo.GradeExportRow;
import com.gac.lms.module.grade.vo.GradeRowVO;
import com.gac.lms.module.grade.vo.GradeStatisticsVO;

import java.util.List;

/**
 * 成绩管理 Service。
 *
 * @author 方雨菲
 */
public interface GradeService {

    /**
     * 多条件分页筛选。
     */
    PageResult<GradeRowVO> query(GradeQueryRequest request);

    /**
     * 单人成绩详情。
     */
    GradeRowVO getDetail(Long gradeId);

    /**
     * 统计（均分 / 通过率 / 分数段分布）。
     */
    GradeStatisticsVO statistics(Long examId);

    /**
     * 导出 Excel（流式写入，避免大结果集 OOM）。
     *
     * @return 用于 easyexcel 的导出数据列表（最大 65535 行）
     */
    List<GradeExportRow> exportRows(GradeQueryRequest request);
}
