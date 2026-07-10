package com.gac.lms.module.grade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.grade.dto.GradeQueryRequest;
import com.gac.lms.module.grade.entity.GradeRecord;
import com.gac.lms.module.grade.mapper.GradeRecordMapper;
import com.gac.lms.module.grade.service.GradeService;
import com.gac.lms.module.grade.vo.GradeExportRow;
import com.gac.lms.module.grade.vo.GradeRowVO;
import com.gac.lms.module.grade.vo.GradeStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 成绩管理 Service 实现。
 *
 * <p>核心特性：</p>
 * <ul>
 *   <li>多条件动态拼接 SQL（MyBatis-Plus QueryWrapper）</li>
 *   <li>统计聚合（均分 / 通过率 / 最高最低 / 分数段分布）</li>
 *   <li>Excel 导出（easyexcel，上限 65535 行，超出需分批）</li>
 * </ul>
 *
 * @author 方雨菲
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    /** Excel 单 sheet 最大行数（含表头） */
    public static final int EXCEL_MAX_ROWS = 65535;

    private final GradeRecordMapper gradeRecordMapper;

    @Override
    public PageResult<GradeRowVO> query(GradeQueryRequest req) {
        log.debug("[query] examId={} userId={} status={} page={}/{}",
                req.getExamId(), req.getUserId(), req.getStatus(),
                req.getPageNum(), req.getPageSize());

        QueryWrapper<GradeRecord> qw = buildQueryWrapper(req);
        long total = gradeRecordMapper.selectCount(qw);

        // 重新克隆 wrapper，避免 selectCount 与 selectList 的 last() 冲突
        QueryWrapper<GradeRecord> pageQw = buildQueryWrapper(req);
        pageQw.orderByDesc("submitted_at");
        pageQw.last("LIMIT " + req.getPageSize() + " OFFSET " + ((req.getPageNum() - 1) * req.getPageSize()));

        List<GradeRecord> records = gradeRecordMapper.selectList(pageQw);
        List<GradeRowVO> rows = new ArrayList<>(records.size());
        for (GradeRecord r : records) {
            rows.add(toRowVO(r));
        }
        return new PageResult<>(total, req.getPageNum(), req.getPageSize(), rows);
    }

    @Override
    public GradeRowVO getDetail(Long gradeId) {
        GradeRecord record = gradeRecordMapper.selectById(gradeId);
        if (record == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "成绩记录不存在");
        }
        return toRowVO(record);
    }

    @Override
    public GradeStatisticsVO statistics(Long examId) {
        if (examId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "examId 不能为空");
        }
        GradeStatisticsVO vo = gradeRecordMapper.statistics(examId);
        if (vo == null) {
            vo = GradeStatisticsVO.builder().examId(examId).build();
        }
        // 分数段分布
        List<GradeStatisticsVO.ScoreBucket> dist = gradeRecordMapper.distribution(examId);
        vo.setDistribution(dist);
        return vo;
    }

    @Override
    public List<GradeExportRow> exportRows(GradeQueryRequest req) {
        // 导出无分页限制（但受 EXCEL_MAX_ROWS 约束）
        QueryWrapper<GradeRecord> qw = buildQueryWrapper(req);
        qw.orderByDesc("submitted_at");
        qw.last("LIMIT " + EXCEL_MAX_ROWS);

        List<GradeRecord> records = gradeRecordMapper.selectList(qw);
        if (records.size() >= EXCEL_MAX_ROWS) {
            log.warn("[exportRows] 达到 Excel 单 sheet 上限 {}, 建议分批导出", EXCEL_MAX_ROWS);
        }

        List<GradeExportRow> rows = new ArrayList<>(records.size());
        for (GradeRecord r : records) {
            rows.add(toExportRow(r));
        }
        return rows;
    }

    // ===== 私有方法 =====

    private QueryWrapper<GradeRecord> buildQueryWrapper(GradeQueryRequest req) {
        QueryWrapper<GradeRecord> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (req.getExamId() != null)     qw.eq("exam_id", req.getExamId());
        if (req.getUserId() != null)     qw.eq("user_id", req.getUserId());
        if (req.getPaperId() != null)    qw.eq("paper_id", req.getPaperId());
        if (req.getStatus() != null)     qw.eq("status", req.getStatus());
        if (req.getIsPassed() != null)   qw.eq("is_passed", req.getIsPassed());
        if (req.getMinScore() != null)   qw.ge("total_score", req.getMinScore());
        if (req.getMaxScore() != null)   qw.le("total_score", req.getMaxScore());
        if (req.getSubmittedFrom() != null) qw.ge("submitted_at", req.getSubmittedFrom());
        if (req.getSubmittedTo() != null)   qw.le("submitted_at", req.getSubmittedTo());
        // W3 接入账号模块后启用：
        // if (StringUtils.hasText(req.getKeyword())) {
        //     qw.and(w -> w.like("user_name", req.getKeyword()).or().like("exam_name", req.getKeyword()));
        // }
        return qw;
    }

    private GradeRowVO toRowVO(GradeRecord r) {
        return GradeRowVO.builder()
                .id(r.getId())
                .examId(r.getExamId())
                // .examName(...)   // W3 接入试卷/考试服务后填充
                .userId(r.getUserId())
                // .userName(...)   // W3 接入账号服务后填充
                .paperId(r.getPaperId())
                .totalScore(r.getTotalScore())
                .objectiveScore(r.getObjectiveScore())
                .subjectiveScore(r.getSubjectiveScore())
                .passScore(r.getPassScore())
                .isPassed(r.getIsPassed())
                .status(r.getStatus())
                .statusLabel(statusLabel(r.getStatus()))
                .submittedAt(r.getSubmittedAt())
                .publishedAt(r.getPublishedAt())
                .build();
    }

    private GradeExportRow toExportRow(GradeRecord r) {
        return GradeExportRow.builder()
                .id(r.getId())
                .examId(r.getExamId())
                .examName(null)
                .userId(r.getUserId())
                .userName(null)
                .totalScore(r.getTotalScore())
                .objectiveScore(r.getObjectiveScore())
                .subjectiveScore(r.getSubjectiveScore())
                .passScore(r.getPassScore())
                .isPassedLabel(r.getIsPassed() != null && r.getIsPassed() == 1 ? "通过" : "未通过")
                .statusLabel(statusLabel(r.getStatus()))
                .submittedAt(r.getSubmittedAt())
                .publishedAt(r.getPublishedAt())
                .build();
    }

    private String statusLabel(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待评分";
            case 1 -> "部分评分";
            case 2 -> "已评（待复核）";
            case 3 -> "已复核";
            case 4 -> "已发布";
            default -> "未知";
        };
    }
}
