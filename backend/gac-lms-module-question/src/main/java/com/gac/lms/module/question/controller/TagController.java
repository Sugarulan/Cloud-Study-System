package com.gac.lms.module.question.controller;

import com.gac.lms.common.response.Result;
import com.gac.lms.module.question.service.TagService;
import com.gac.lms.module.question.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签管理 Controller（3.3.3）。
 *
 * <p>路径前缀 {@code /api/v1/tags}（题目模块下的标签）。
 * 试卷 / 文档模块的标签按需另建（共用同一张 tag 表）。</p>
 *
 * @author 王茗瑾
 */
@Tag(name = "标签管理", description = "3.3.3 题目标签：CRUD + 使用次数")
@Validated
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "查询标签列表")
    @GetMapping
    public Result<List<TagVO>> list(
            @RequestParam(required = false, defaultValue = "题目") String category) {
        return Result.ok(tagService.listAll(category));
    }

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<TagVO> create(@RequestParam @NotBlank String name,
                                @RequestParam(required = false, defaultValue = "题目") String category) {
        return Result.ok(tagService.create(name, category));
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<TagVO> update(@PathVariable Long id,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String category) {
        return Result.ok(tagService.update(id, name, category));
    }

    @Operation(summary = "删除标签（被引用时禁止）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.ok();
    }
}
