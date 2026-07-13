package com.gac.lms.module.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.module.question.entity.QuestionTag;
import com.gac.lms.module.question.entity.Tag;
import com.gac.lms.module.question.mapper.QuestionTagMapper;
import com.gac.lms.module.question.mapper.TagMapper;
import com.gac.lms.module.question.service.TagService;
import com.gac.lms.module.question.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签业务实现。
 *
 * @author 王茗瑾
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final QuestionTagMapper questionTagMapper;

    @Override
    public List<TagVO> listAll(String category) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(Tag::getCategory, category);
        }
        wrapper.orderByAsc(Tag::getCategory, Tag::getId);
        List<Tag> tags = tagMapper.selectList(wrapper);
        if (tags.isEmpty()) return Collections.emptyList();

        // 批量统计每个标签被多少题目引用（避免 N+1）
        List<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());
        List<QuestionTag> refs = questionTagMapper.selectList(
                new LambdaQueryWrapper<QuestionTag>().in(QuestionTag::getTagId, tagIds)
        );
        Map<Long, Long> useCount = refs.stream()
                .collect(Collectors.groupingBy(QuestionTag::getTagId, Collectors.counting()));

        return tags.stream()
                .map(t -> TagVO.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .category(t.getCategory())
                        .useCount(useCount.getOrDefault(t.getId(), 0L))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public TagVO create(String name, String category) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "标签名不能为空");
        }
        // 同名同 category 唯一校验
        Long exists = tagMapper.selectCount(
                new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getName, name.trim())
                        .eq(Tag::getCategory, category)
        );
        if (exists > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "标签已存在");
        }
        Tag tag = new Tag();
        tag.setName(name.trim());
        tag.setCategory(category);
        tagMapper.insert(tag);
        log.info("[Tag] created: id={} name={}", tag.getId(), tag.getName());
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .category(tag.getCategory())
                .useCount(0L)
                .build();
    }

    @Override
    public TagVO update(Long id, String name, String category) {
        Tag tag = mustGet(id);
        if (StringUtils.hasText(name)) tag.setName(name.trim());
        if (category != null) tag.setCategory(category);
        tagMapper.updateById(tag);
        log.info("[Tag] updated: id={}", id);
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .category(tag.getCategory())
                .useCount(0L)  // 简化：W3 可优化为统计引用数
                .build();
    }

    @Override
    public void delete(Long id) {
        mustGet(id);
        // 校验引用
        Long refs = questionTagMapper.selectCount(
                new LambdaQueryWrapper<QuestionTag>().eq(QuestionTag::getTagId, id)
        );
        if (refs > 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "该标签被 " + refs + " 个题目引用，无法删除");
        }
        tagMapper.deleteById(id);
        log.info("[Tag] deleted: id={}", id);
    }

    private Tag mustGet(Long id) {
        Tag t = tagMapper.selectById(id);
        if (t == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "标签不存在");
        }
        return t;
    }
}
