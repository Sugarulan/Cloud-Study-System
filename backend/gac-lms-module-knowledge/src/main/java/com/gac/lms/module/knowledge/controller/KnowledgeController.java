package com.gac.lms.module.knowledge.controller;

import com.gac.lms.common.constants.CommonConstants;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.common.response.Result;
import com.gac.lms.module.knowledge.dto.DocCreateRequest;
import com.gac.lms.module.knowledge.dto.ReviewRequest;
import com.gac.lms.module.knowledge.service.KnowledgeService;
import com.gac.lms.module.knowledge.vo.CategoryTreeNode;
import com.gac.lms.module.knowledge.vo.DocActionVO;
import com.gac.lms.module.knowledge.vo.DocDiffVO;
import com.gac.lms.module.knowledge.vo.DocVO;
import com.gac.lms.module.knowledge.vo.DocVersionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 知识管理 Controller（3.3.10）。
 *
 * @author 方雨菲
 */
@Slf4j
@Tag(name = "知识管理", description = "3.3.10 知识库：分类/文档/状态机/版本/AI 抽题")
@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("knowledge-module-ok");
    }

    @Operation(summary = "知识库目录树")
    @GetMapping("/tree")
    public Result<List<CategoryTreeNode>> tree() {
        return Result.ok(knowledgeService.getCategoryTree());
    }

    @Operation(summary = "创建文档（草稿）")
    @PostMapping("/docs")
    public Result<DocVO> create(
            @Valid @RequestBody DocCreateRequest req,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(knowledgeService.createDoc(req, userId));
    }

    @Operation(summary = "更新文档（仅 DRAFT/REJECTED 可编辑）")
    @PutMapping("/docs/{id}")
    public Result<DocVO> update(
            @PathVariable Long id,
            @Valid @RequestBody DocCreateRequest req,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(knowledgeService.updateDoc(id, req, userId));
    }

    @Operation(summary = "文档详情")
    @GetMapping("/docs/{id}")
    public Result<DocVO> detail(@PathVariable Long id) {
        return Result.ok(knowledgeService.getDoc(id));
    }

    @Operation(summary = "文档列表（按分类/状态筛选）")
    @GetMapping("/docs")
    public Result<PageResult<DocVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(knowledgeService.listDocs(categoryId, status, pageNum, pageSize));
    }

    @Operation(summary = "提交审核（DRAFT → PENDING）")
    @PostMapping("/docs/{id}/submit")
    public Result<DocActionVO> submit(
            @PathVariable Long id,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(knowledgeService.submitForReview(id, userId));
    }

    @Operation(summary = "审核通过（PENDING → PUBLISHED）")
    @PostMapping("/docs/{id}/approve")
    public Result<DocActionVO> approve(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewRequest req,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(knowledgeService.approve(id, req, userId));
    }

    @Operation(summary = "审核驳回（PENDING → REJECTED）")
    @PostMapping("/docs/{id}/reject")
    public Result<DocActionVO> reject(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewRequest req,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(knowledgeService.reject(id, req, userId));
    }

    @Operation(summary = "直接发布（跳过审核，仅 DRAFT/REJECTED）")
    @PostMapping("/docs/{id}/publish")
    public Result<DocActionVO> publish(
            @PathVariable Long id,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(knowledgeService.publish(id, userId));
    }

    @Operation(summary = "归档（PUBLISHED → ARCHIVED）")
    @PostMapping("/docs/{id}/archive")
    public Result<DocActionVO> archive(
            @PathVariable Long id,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        requireUser(userId);
        return Result.ok(knowledgeService.archive(id, userId));
    }

    @Operation(summary = "版本列表")
    @GetMapping("/docs/{id}/versions")
    public Result<List<DocVersionVO>> versions(@PathVariable Long id) {
        return Result.ok(knowledgeService.listVersions(id));
    }

    @Operation(summary = "版本对比")
    @GetMapping("/docs/{id}/diff")
    public Result<DocDiffVO> diff(
            @PathVariable Long id,
            @RequestParam Integer from,
            @RequestParam Integer to) {
        return Result.ok(knowledgeService.diffVersions(id, from, to));
    }

    private void requireUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未识别用户身份");
        }
    }
}
