package com.gac.lms.module.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.question.dto.BatchDeleteCmd;
import com.gac.lms.module.question.dto.QuestionCreateCmd;
import com.gac.lms.module.question.dto.QuestionQuery;
import com.gac.lms.module.question.dto.QuestionUpdateCmd;
import com.gac.lms.module.question.entity.Question;
import com.gac.lms.module.question.entity.QuestionOption;
import com.gac.lms.module.question.entity.QuestionTag;
import com.gac.lms.module.question.mapper.QuestionMapper;
import com.gac.lms.module.question.mapper.QuestionOptionMapper;
import com.gac.lms.module.question.mapper.QuestionTagMapper;
import com.gac.lms.module.question.service.QuestionService;
import com.gac.lms.module.question.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目业务实现。
 *
 * <p><b>设计要点：</b></p>
 * <ul>
 *   <li>选项与标签用替换式写入（先清旧再写新），保证原子性</li>
 *   <li>答案按 type 校验格式，避免脏数据落库</li>
 *   <li>已发布且被引用的题目不能删除（W2 简化版只校验发布状态，W4 加 paper 引用校验）</li>
 *   <li>answer_json 存储原始 JSON 字符串，VO 层用 {@link JsonNode} 透传</li>
 * </ul>
 *
 * @author 王茗瑾
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final QuestionTagMapper questionTagMapper;
    private final ObjectMapper objectMapper;

    // ============== 查询 ==============

    @Override
    public PageResult<QuestionVO> page(QuestionQuery query) {
        Page<Question> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(Question::getType, query.getType());
        }
        if (query.getDifficulty() != null) {
            wrapper.eq(Question::getDifficulty, query.getDifficulty());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Question::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Question::getStem, query.getKeyword());
        }
        // tagId 过滤：JOIN question_tag —— W3 再做，前端先二次过滤
        wrapper.orderByDesc(Question::getCreateTime);

        Page<Question> result = questionMapper.selectPage(page, wrapper);
        List<QuestionVO> records = result.getRecords().stream()
                .map(this::toBaseVO).collect(Collectors.toList());
        fillTags(records);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public QuestionVO getById(Long id) {
        Question q = mustGet(id);
        QuestionVO vo = toFullVO(q);
        fillOptions(List.of(vo));
        fillTags(List.of(vo));
        return vo;
    }

    @Override
    public List<QuestionVO> listByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<Question> qs = questionMapper.selectBatchIds(ids);
        if (qs.isEmpty()) return Collections.emptyList();
        List<QuestionVO> vos = qs.stream().map(this::toBaseVO).collect(Collectors.toList());
        fillTags(vos);
        return vos;
    }

    @Override
    public Map<Long, QuestionVO> mapByIds(Collection<Long> ids) {
        List<QuestionVO> list = listByIds(ids);
        return list.stream().collect(Collectors.toMap(QuestionVO::getId, v -> v));
    }

    // ============== 写入 ==============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionVO create(QuestionCreateCmd cmd) {
        // 1) 校验答案格式
        validateAnswer(cmd.getType(), cmd.getAnswer());
        // 2) 校验选项（SINGLE/MULTI）
        validateOptions(cmd.getType(), cmd.getOptions());

        // 3) 写入题目
        Question q = new Question();
        q.setType(cmd.getType());
        q.setStem(cmd.getStem());
        q.setAnalysis(cmd.getAnalysis());
        q.setDifficulty(cmd.getDifficulty());
        q.setDefaultScore(cmd.getDefaultScore());
        q.setCategoryId(cmd.getCategoryId());
        q.setAnswerJson(serializeAnswer(cmd.getAnswer()));
        q.setStatus(0);  // 默认草稿
        questionMapper.insert(q);
        log.info("[Question] created: id={} type={}", q.getId(), q.getType());

        // 4) 写入选项
        if (cmd.getOptions() != null && !cmd.getOptions().isEmpty()) {
            saveOptions(q.getId(), cmd.getOptions());
        }
        // 5) 关联标签
        if (cmd.getTagIds() != null && !cmd.getTagIds().isEmpty()) {
            saveTags(q.getId(), cmd.getTagIds());
        }

        return getById(q.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionVO update(Long id, QuestionUpdateCmd cmd) {
        Question q = mustGet(id);
        String type = q.getType();  // 不可改 type
        // 校验答案
        if (cmd.getAnswer() != null) {
            validateAnswer(type, cmd.getAnswer());
        }
        // 校验选项
        if (cmd.getOptions() != null) {
            validateOptions(type, cmd.getOptions());
        }

        q.setStem(cmd.getStem());
        q.setAnalysis(cmd.getAnalysis());
        q.setDifficulty(cmd.getDifficulty());
        q.setDefaultScore(cmd.getDefaultScore());
        q.setCategoryId(cmd.getCategoryId());
        if (cmd.getAnswer() != null) {
            q.setAnswerJson(serializeAnswer(cmd.getAnswer()));
        }
        questionMapper.updateById(q);

        // 选项替换式
        if (cmd.getOptions() != null) {
            questionOptionMapper.delete(
                    new LambdaQueryWrapper<QuestionOption>().eq(QuestionOption::getQuestionId, id));
            if (!cmd.getOptions().isEmpty()) {
                saveOptions(id, cmd.getOptions());
            }
        }
        // 标签替换式
        if (cmd.getTagIds() != null) {
            questionTagMapper.delete(
                    new LambdaQueryWrapper<QuestionTag>().eq(QuestionTag::getQuestionId, id));
            if (!cmd.getTagIds().isEmpty()) {
                saveTags(id, cmd.getTagIds());
            }
        }
        log.info("[Question] updated: id={}", id);
        return getById(id);
    }

    @Override
    public void delete(Long id) {
        Question q = mustGet(id);
        // 已发布且被引用禁止删除（W2 简化版只校验发布状态，W4 加 paper 引用校验）
        if (q.getStatus() != null && q.getStatus() == 1) {
            // TODO W4: 校验 paper_question 引用
            // Long refs = paperQuestionMapper.selectCount(...);
            // if (refs > 0) throw new BusinessException(...);
            // 简化：已发布直接拒绝删除，强制先下架
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "已发布的题目不能删除，请先下架");
        }
        questionMapper.deleteById(id);
        log.info("[Question] deleted: id={}", id);
    }

    @Override
    public void batchDelete(BatchDeleteCmd cmd) {
        List<Long> ids = cmd.getIds();
        int success = 0;
        List<Long> failed = new ArrayList<>();
        for (Long id : ids) {
            try {
                delete(id);
                success++;
            } catch (BusinessException ex) {
                failed.add(id);
            }
        }
        if (!failed.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "成功 " + success + " 条，失败 " + failed.size() + " 条。失败 ID：" + failed);
        }
        log.info("[Question] batchDelete: ids={} success={}", ids, success);
    }

    @Override
    public void publish(Long id) {
        Question q = mustGet(id);
        if (q.getStatus() != null && q.getStatus() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "题目已发布，无需重复操作");
        }
        // 发布前校验：必须有答案
        if (q.getAnswerJson() == null || q.getAnswerJson().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "题目无答案，不能发布");
        }
        q.setStatus(1);
        questionMapper.updateById(q);
        log.info("[Question] published: id={}", id);
    }

    @Override
    public void batchPublish(List<Long> ids) {
        for (Long id : ids) {
            try {
                publish(id);
            } catch (BusinessException ignored) {
                // 单条失败不影响其他
            }
        }
    }

    @Override
    public void unpublish(Long id) {
        Question q = mustGet(id);
        if (q.getStatus() == null || q.getStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "题目未发布，无需取消");
        }
        // TODO W4: 校验 paper_question 引用
        // Long refs = paperQuestionMapper.selectCount(...);
        // if (refs > 0) throw new BusinessException(...);
        q.setStatus(0);
        questionMapper.updateById(q);
        log.info("[Question] unpublished: id={}", id);
    }

    // ============== 私有工具 ==============

    private Question mustGet(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "题目不存在");
        }
        return q;
    }

    private QuestionVO toBaseVO(Question q) {
        return QuestionVO.builder()
                .id(q.getId())
                .type(q.getType())
                .stem(q.getStem())
                .analysis(q.getAnalysis())
                .difficulty(q.getDifficulty())
                .defaultScore(q.getDefaultScore())
                .categoryId(q.getCategoryId())
                .status(q.getStatus())
                .answer(parseAnswer(q.getAnswerJson()))
                .createTime(q.getCreateTime())
                .options(Collections.emptyList())
                .tagIds(Collections.emptyList())
                .build();
    }

    private QuestionVO toFullVO(Question q) {
        return toBaseVO(q);
    }

    /**
     * 批量填充题目选项。
     */
    private void fillOptions(List<QuestionVO> vos) {
        if (vos.isEmpty()) return;
        List<Long> qIds = vos.stream().map(QuestionVO::getId).collect(Collectors.toList());
        List<QuestionOption> opts = questionOptionMapper.selectList(
                new LambdaQueryWrapper<QuestionOption>().in(QuestionOption::getQuestionId, qIds)
        );
        if (opts.isEmpty()) return;
        Map<Long, List<QuestionVO.OptionVO>> map = opts.stream()
                .sorted((a, b) -> Integer.compare(
                        a.getSort() == null ? 0 : a.getSort(),
                        b.getSort() == null ? 0 : b.getSort()))
                .collect(Collectors.groupingBy(
                        QuestionOption::getQuestionId,
                        Collectors.mapping(o -> QuestionVO.OptionVO.builder()
                                .id(o.getId())
                                .optKey(o.getOptKey())
                                .optValue(o.getOptValue())
                                .isCorrect(o.getIsCorrect())
                                .sort(o.getSort())
                                .build(), Collectors.toList())));
        vos.forEach(vo -> vo.setOptions(map.getOrDefault(vo.getId(), Collections.emptyList())));
    }

    /**
     * 批量填充题目的标签 ID 列表。
     */
    private void fillTags(List<QuestionVO> vos) {
        if (vos.isEmpty()) return;
        List<Long> qIds = vos.stream().map(QuestionVO::getId).collect(Collectors.toList());
        List<QuestionTag> qts = questionTagMapper.selectList(
                new LambdaQueryWrapper<QuestionTag>().in(QuestionTag::getQuestionId, qIds)
        );
        if (qts.isEmpty()) return;
        Map<Long, List<Long>> map = qts.stream().collect(Collectors.groupingBy(
                QuestionTag::getQuestionId,
                Collectors.mapping(QuestionTag::getTagId, Collectors.toList())));
        vos.forEach(vo -> vo.setTagIds(map.getOrDefault(vo.getId(), Collections.emptyList())));
    }

    private void saveOptions(Long questionId, List<QuestionCreateCmd.QuestionOptionCmd> options) {
        int idx = 1;
        for (QuestionCreateCmd.QuestionOptionCmd oc : options) {
            QuestionOption opt = new QuestionOption();
            opt.setQuestionId(questionId);
            opt.setOptKey(oc.getOptKey());
            opt.setOptValue(oc.getOptValue());
            opt.setIsCorrect(oc.getIsCorrect() != null ? oc.getIsCorrect() : false);
            opt.setSort(oc.getSort() != null ? oc.getSort() : idx++);
            questionOptionMapper.insert(opt);
        }
    }

    private void saveTags(Long questionId, List<Long> tagIds) {
        Set<Long> distinct = tagIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        for (Long tagId : distinct) {
            QuestionTag qt = new QuestionTag();
            qt.setQuestionId(questionId);
            qt.setTagId(tagId);
            questionTagMapper.insert(qt);
        }
    }

    private void validateAnswer(String type, Object answer) {
        if (answer == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "答案不能为空");
        }
        switch (type) {
            case "SINGLE":
                if (!(answer instanceof String)) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "单选题答案必须为字符串（选项 key）");
                }
                break;
            case "MULTI":
                if (!(answer instanceof List)) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "多选题答案必须为字符串数组");
                }
                break;
            case "JUDGE":
                if (!(answer instanceof Boolean)) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "判断题答案必须为 true/false");
                }
                break;
            case "ESSAY":
            case "FILL":
                // 接受 String 或 List<String>
                if (!(answer instanceof String) && !(answer instanceof List)) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "答案必须为字符串或字符串数组");
                }
                break;
            default:
                throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的题型：" + type);
        }
    }

    private void validateOptions(String type, List<QuestionCreateCmd.QuestionOptionCmd> options) {
        boolean needOptions = "SINGLE".equals(type) || "MULTI".equals(type);
        if (!needOptions) {
            return;  // JUDGE/ESSAY/FILL 不需要选项
        }
        if (options == null || options.size() < 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "单选/多选题至少需要 2 个选项");
        }
        long correctCount = options.stream()
                .filter(o -> o.getIsCorrect() != null && o.getIsCorrect())
                .count();
        if (correctCount < 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "至少需要 1 个正确答案");
        }
        if ("SINGLE".equals(type) && correctCount > 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "单选题只能有 1 个正确答案");
        }
    }

    private String serializeAnswer(Object answer) {
        if (answer == null) return null;
        try {
            return objectMapper.writeValueAsString(answer);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "答案序列化失败：" + e.getMessage());
        }
    }

    private JsonNode parseAnswer(String json) {
        if (json == null || json.isEmpty()) {
            return JsonNodeFactory.instance.nullNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.warn("[Question] parse answer failed: {}", e.getMessage());
            return JsonNodeFactory.instance.nullNode();
        }
    }

    // 防止 import 报错（HashMap 在 mapByIds 用到）
    @SuppressWarnings("unused")
    private Map<String, String> unused() { return new HashMap<>(); }
}
