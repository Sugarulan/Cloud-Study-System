package com.gac.lms.module.grade.controller;

import com.alibaba.excel.EasyExcel;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.common.response.Result;
import com.gac.lms.module.grade.dto.GradeQueryRequest;
import com.gac.lms.module.grade.service.GradeService;
import com.gac.lms.module.grade.vo.GradeExportRow;
import com.gac.lms.module.grade.vo.GradeRowVO;
import com.gac.lms.module.grade.vo.GradeStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 成绩管理 Controller（3.3.7）。
 *
 * @author 方雨菲
 */
@Slf4j
@Tag(name = "成绩管理", description = "3.3.7 多条件筛选 / 统计 / Excel 导出")
@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("grade-module-ok");
    }

    @Operation(summary = "多条件分页筛选")
    @GetMapping
    public Result<PageResult<GradeRowVO>> query(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long paperId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isPassed,
            @RequestParam(required = false) java.math.BigDecimal minScore,
            @RequestParam(required = false) java.math.BigDecimal maxScore,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize) {
        GradeQueryRequest req = new GradeQueryRequest();
        req.setExamId(examId);
        req.setUserId(userId);
        req.setPaperId(paperId);
        req.setStatus(status);
        req.setIsPassed(isPassed);
        req.setMinScore(minScore);
        req.setMaxScore(maxScore);
        req.setPageNum(pageNum);
        req.setPageSize(pageSize);
        return Result.ok(gradeService.query(req));
    }

    @Operation(summary = "成绩详情")
    @GetMapping("/{id}")
    public Result<GradeRowVO> detail(@PathVariable Long id) {
        return Result.ok(gradeService.getDetail(id));
    }

    @Operation(summary = "成绩统计（均分 / 通过率 / 分数段分布）")
    @GetMapping("/statistics")
    public Result<GradeStatisticsVO> statistics(@RequestParam Long examId) {
        if (examId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "examId 不能为空");
        }
        return Result.ok(gradeService.statistics(examId));
    }

    @Operation(summary = "导出 Excel（默认导出当前筛选条件全部数据）")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isPassed,
            @RequestParam(required = false) java.math.BigDecimal minScore,
            @RequestParam(required = false) java.math.BigDecimal maxScore,
            HttpServletResponse response) throws IOException {
        GradeQueryRequest req = new GradeQueryRequest();
        req.setExamId(examId);
        req.setStatus(status);
        req.setIsPassed(isPassed);
        req.setMinScore(minScore);
        req.setMaxScore(maxScore);

        List<GradeExportRow> rows = gradeService.exportRows(req);

        String fileName = URLEncoder.encode("成绩_" + System.currentTimeMillis() + ".xlsx",
                StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename*=UTF-8''" + fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-disposition");

        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os, GradeExportRow.class)
                    .sheet("成绩")
                    .doWrite(rows);
        }
        log.info("[export] exported {} rows", rows.size());
    }
}
