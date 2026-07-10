package com.gac.lms.module.question.controller;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.common.response.Result;
import com.gac.lms.module.question.dto.BatchDeleteCmd;
import com.gac.lms.module.question.dto.QuestionCreateCmd;
import com.gac.lms.module.question.dto.QuestionQuery;
import com.gac.lms.module.question.dto.QuestionUpdateCmd;
import com.gac.lms.module.question.service.QuestionService;
import com.gac.lms.module.question.vo.QuestionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 题目管理 Controller（3.3.3）。
 *
 * <p>路径前缀 {@code /api/v1/questions}。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "题目管理", description = "3.3.3 题目：CRUD / 选项 / 标签 / 发布 / 批量")
@Slf4j
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "健康检查（W1 验收用）")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("question-module-ok");
    }

    @Operation(summary = "分页查询题目")
    @GetMapping
    public Result<PageResult<QuestionVO>> page(QuestionQuery query) {
        return Result.ok(questionService.page(query));
    }

    @Operation(summary = "题目详情（含选项与标签）")
    @GetMapping("/{id}")
    public Result<QuestionVO> getById(@PathVariable Long id) {
        return Result.ok(questionService.getById(id));
    }

    @Operation(summary = "创建题目")
    @PostMapping
    public Result<QuestionVO> create(@Valid @RequestBody QuestionCreateCmd cmd) {
        return Result.ok(questionService.create(cmd));
    }

    @Operation(summary = "更新题目（不可改 type）")
    @PutMapping("/{id}")
    public Result<QuestionVO> update(@PathVariable Long id,
                                     @Valid @RequestBody QuestionUpdateCmd cmd) {
        log.info("[API] update question: id={}", id);
        return Result.ok(questionService.update(id, cmd));
    }

    @Operation(summary = "删除单个题目（已发布需先下架）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除题目（逐个校验，失败的 ID 在错误信息里）")
    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@Valid @RequestBody BatchDeleteCmd cmd) {
        questionService.batchDelete(cmd);
        return Result.ok();
    }

    @Operation(summary = "发布题目（status 0→1）")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        questionService.publish(id);
        return Result.ok();
    }

    @Operation(summary = "取消发布（status 1→0）")
    @PostMapping("/{id}/unpublish")
    public Result<Void> unpublish(@PathVariable Long id) {
        log.info("[API] unpublish question: id={}", id);
        questionService.unpublish(id);
        return Result.ok();
    }

    @Operation(summary = "批量发布")
    @PostMapping("/batch-publish")
    public Result<Void> batchPublish(
            @Parameter(description = "题目 ID 列表") @RequestParam("ids") List<Long> ids) {
        questionService.batchPublish(ids);
        return Result.ok();
    }

    // ============== W3 待补：Excel 导入导出 ==============

    // @PostMapping("/import")
    // public Result<Integer> importQuestions(@RequestParam("file") MultipartFile file) {
    //     // TODO W3 EasyExcel
    // }

    // @GetMapping("/export")
    // public void exportQuestions(QuestionQuery query, HttpServletResponse response) {
    //     // TODO W3 EasyExcel
    // }
}
