package com.gac.lms.module.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.knowledge.dto.DocCreateRequest;
import com.gac.lms.module.knowledge.dto.ReviewRequest;
import com.gac.lms.module.knowledge.entity.KnowledgeCategory;
import com.gac.lms.module.knowledge.entity.KnowledgeDoc;
import com.gac.lms.module.knowledge.entity.KnowledgeDocVersion;
import com.gac.lms.module.knowledge.mapper.KnowledgeCategoryMapper;
import com.gac.lms.module.knowledge.mapper.KnowledgeDocMapper;
import com.gac.lms.module.knowledge.mapper.KnowledgeDocVersionMapper;
import com.gac.lms.module.knowledge.service.KnowledgeService;
import com.gac.lms.module.knowledge.vo.CategoryTreeNode;
import com.gac.lms.module.knowledge.vo.DocActionVO;
import com.gac.lms.module.knowledge.vo.DocDiffVO;
import com.gac.lms.module.knowledge.vo.DocVO;
import com.gac.lms.module.knowledge.vo.DocVersionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识管理 Service 实现。
 *
 * @author 方雨菲
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PUBLISHED = 2;
    public static final int STATUS_ARCHIVED = 3;
    public static final int STATUS_REJECTED = 4;

    private final KnowledgeDocMapper docMapper;
    private final KnowledgeCategoryMapper categoryMapper;
    private final KnowledgeDocVersionMapper versionMapper;

    // ========== 分类树 ==========

    @Override
    public List<CategoryTreeNode> getCategoryTree() {
        List<KnowledgeCategory> all = categoryMapper.selectList(
                new QueryWrapper<KnowledgeCategory>().eq("deleted", 0).orderByAsc("sort"));
        if (all.isEmpty()) {
            return Collections.emptyList();
        }

        // 统计每个分类下的文档数（仅 PUBLISHED）
        Map<Long, Long> docCountMap = new HashMap<>();
        List<KnowledgeDoc> docs = docMapper.selectList(
                new QueryWrapper<KnowledgeDoc>()
                        .eq("status", STATUS_PUBLISHED).eq("deleted", 0)
                        .select("category_id"));
        for (KnowledgeDoc d : docs) {
            if (d.getCategoryId() != null) {
                // 用 compute 而非 merge，避免 BiFunction 类型推断警告
                docCountMap.compute(d.getCategoryId(), (k, v) -> v == null ? 1L : v + 1L);
            }
        }

        // 构建树（递归）
        Map<Long, List<KnowledgeCategory>> childrenMap = new HashMap<>();
        for (KnowledgeCategory c : all) {
            childrenMap.computeIfAbsent(c.getParentId() == null ? 0L : c.getParentId(), k -> new ArrayList<>())
                    .add(c);
        }
        List<CategoryTreeNode> roots = new ArrayList<>();
        for (KnowledgeCategory c : all) {
            if (c.getParentId() == null || c.getParentId() == 0L) {
                roots.add(toTreeNode(c, childrenMap, docCountMap));
            }
        }
        return roots;
    }

    private CategoryTreeNode toTreeNode(KnowledgeCategory c,
                                        Map<Long, List<KnowledgeCategory>> childrenMap,
                                        Map<Long, Long> docCountMap) {
        List<CategoryTreeNode> children = new ArrayList<>();
        List<KnowledgeCategory> childCats = childrenMap.get(c.getId());
        if (childCats != null) {
            for (KnowledgeCategory child : childCats) {
                children.add(toTreeNode(child, childrenMap, docCountMap));
            }
        }
        return CategoryTreeNode.builder()
                .id(c.getId())
                .parentId(c.getParentId())
                .name(c.getName())
                .sort(c.getSort())
                .docCount(docCountMap.getOrDefault(c.getId(), 0L))
                .children(children)
                .build();
    }

    // ========== 文档 CRUD ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocVO createDoc(DocCreateRequest req, Long authorId) {
        log.info("[createDoc] author={} title={}", authorId, req.getTitle());

        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle(req.getTitle());
        doc.setCategoryId(req.getCategoryId());
        doc.setSummary(req.getSummary());
        doc.setContent(req.getContent());
        doc.setTags(req.getTags());
        doc.setStatus(STATUS_DRAFT);
        doc.setVersion(1);
        doc.setAuthorId(authorId);
        doc.setCreateBy(authorId);
        doc.setUpdateBy(authorId);
        docMapper.insert(doc);

        // 创建初始版本快照
        saveVersionSnapshot(doc, authorId, "初稿");

        return toDocVO(doc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocVO updateDoc(Long id, DocCreateRequest req, Long operatorId) {
        KnowledgeDoc doc = mustGet(id);
        ensureEditable(doc);

        doc.setTitle(req.getTitle());
        doc.setCategoryId(req.getCategoryId());
        doc.setSummary(req.getSummary());
        doc.setContent(req.getContent());
        doc.setTags(req.getTags());
        doc.setVersion(doc.getVersion() + 1);
        doc.setUpdateBy(operatorId);
        docMapper.updateById(doc);

        saveVersionSnapshot(doc, operatorId, req.getChangeLog());
        return toDocVO(doc);
    }

    @Override
    public DocVO getDoc(Long id) {
        return toDocVO(mustGet(id));
    }

    @Override
    public PageResult<DocVO> listDocs(Long categoryId, Integer status, int pageNum, int pageSize) {
        QueryWrapper<KnowledgeDoc> qw = new QueryWrapper<>();
        qw.eq("deleted", 0);
        if (categoryId != null) qw.eq("category_id", categoryId);
        if (status != null) qw.eq("status", status);
        qw.orderByDesc("update_time");

        long total = docMapper.selectCount(qw);

        QueryWrapper<KnowledgeDoc> pageQw = qw.clone()
                .last("LIMIT " + pageSize + " OFFSET " + ((pageNum - 1) * pageSize));
        List<KnowledgeDoc> records = docMapper.selectList(pageQw);

        List<DocVO> vos = new ArrayList<>(records.size());
        for (KnowledgeDoc d : records) vos.add(toDocVO(d));
        return new PageResult<>(total, pageNum, pageSize, vos);
    }

    // ========== 状态机 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocActionVO submitForReview(Long id, Long operatorId) {
        KnowledgeDoc doc = mustGet(id);
        if (doc.getStatus() != STATUS_DRAFT && doc.getStatus() != STATUS_REJECTED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "仅草稿/驳回状态可提交审核");
        }
        doc.setStatus(STATUS_PENDING);
        doc.setUpdateBy(operatorId);
        docMapper.updateById(doc);
        return actionVO(doc, "已提交审核", operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocActionVO approve(Long id, ReviewRequest req, Long reviewerId) {
        KnowledgeDoc doc = mustGet(id);
        if (doc.getStatus() != STATUS_PENDING) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "仅待审核可批准");
        }
        if (doc.getAuthorId() != null && doc.getAuthorId().equals(reviewerId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "作者不能审核自己的文档");
        }
        doc.setStatus(STATUS_PUBLISHED);
        doc.setReviewerId(reviewerId);
        doc.setPublishedAt(LocalDateTime.now());
        doc.setUpdateBy(reviewerId);
        docMapper.updateById(doc);
        return actionVO(doc, "审核通过，已发布", reviewerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocActionVO reject(Long id, ReviewRequest req, Long reviewerId) {
        KnowledgeDoc doc = mustGet(id);
        if (doc.getStatus() != STATUS_PENDING) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "仅待审核可驳回");
        }
        doc.setStatus(STATUS_REJECTED);
        doc.setReviewerId(reviewerId);
        doc.setUpdateBy(reviewerId);
        docMapper.updateById(doc);
        String reason = req == null || req.getReason() == null ? "" : "（" + req.getReason() + "）";
        return actionVO(doc, "审核驳回" + reason, reviewerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocActionVO publish(Long id, Long operatorId) {
        KnowledgeDoc doc = mustGet(id);
        if (doc.getStatus() != STATUS_DRAFT && doc.getStatus() != STATUS_REJECTED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "仅草稿/驳回可直接发布");
        }
        doc.setStatus(STATUS_PUBLISHED);
        doc.setPublishedAt(LocalDateTime.now());
        doc.setUpdateBy(operatorId);
        docMapper.updateById(doc);
        return actionVO(doc, "已发布", operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocActionVO archive(Long id, Long operatorId) {
        KnowledgeDoc doc = mustGet(id);
        if (doc.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "仅已发布可归档");
        }
        doc.setStatus(STATUS_ARCHIVED);
        doc.setArchivedAt(LocalDateTime.now());
        doc.setUpdateBy(operatorId);
        docMapper.updateById(doc);
        return actionVO(doc, "已归档", operatorId);
    }

    // ========== 版本 ==========

    @Override
    public List<DocVersionVO> listVersions(Long docId) {
        // 确认文档存在
        mustGet(docId);
        List<KnowledgeDocVersion> versions = versionMapper.selectList(
                new QueryWrapper<KnowledgeDocVersion>()
                        .eq("doc_id", docId).eq("deleted", 0)
                        .orderByDesc("version"));

        List<DocVersionVO> vos = new ArrayList<>(versions.size());
        for (KnowledgeDocVersion v : versions) {
            vos.add(DocVersionVO.builder()
                    .id(v.getId())
                    .docId(v.getDocId())
                    .version(v.getVersion())
                    .title(v.getTitle())
                    .changeLog(v.getChangeLog())
                    .operatorId(v.getOperatorId())
                    .createTime(v.getCreateTime())
                    .build());
        }
        return vos;
    }

    @Override
    public DocDiffVO diffVersions(Long docId, Integer from, Integer to) {
        if (from == null || to == null || from >= to) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "from / to 必须有效且 from < to");
        }
        KnowledgeDocVersion fromVer = getVersionOrThrow(docId, from);
        KnowledgeDocVersion toVer = getVersionOrThrow(docId, to);

        // W2 简化：返回两个原始内容 + 简单字数差异
        // W3 可升级为结构化 diff（逐行 + LCS）
        String fromContent = fromVer.getContent() == null ? "" : fromVer.getContent();
        String toContent = toVer.getContent() == null ? "" : toVer.getContent();

        int removed = fromContent.length();
        int added = toContent.length();

        return DocDiffVO.builder()
                .docId(docId)
                .fromVersion(from)
                .toVersion(to)
                .fromContent(fromContent)
                .toContent(toContent)
                .unifiedDiff(simpleUnifiedDiff(fromContent, toContent))
                .addedChars(Math.max(0, added - removed))
                .removedChars(Math.max(0, removed - added))
                .build();
    }

    // ========== 私有 ==========

    private KnowledgeDoc mustGet(Long id) {
        KnowledgeDoc doc = docMapper.selectById(id);
        if (doc == null || doc.getDeleted() != null && doc.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "文档不存在");
        }
        return doc;
    }

    private void ensureEditable(KnowledgeDoc doc) {
        if (doc.getStatus() != STATUS_DRAFT && doc.getStatus() != STATUS_REJECTED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "当前状态（" + statusLabel(doc.getStatus()) + "）不可编辑");
        }
    }

    private void saveVersionSnapshot(KnowledgeDoc doc, Long operatorId, String changeLog) {
        KnowledgeDocVersion v = new KnowledgeDocVersion();
        v.setDocId(doc.getId());
        v.setVersion(doc.getVersion());
        v.setTitle(doc.getTitle());
        v.setContent(doc.getContent());
        v.setChangeLog(changeLog);
        v.setOperatorId(operatorId);
        v.setCreateTime(LocalDateTime.now());
        versionMapper.insert(v);
    }

    private KnowledgeDocVersion getVersionOrThrow(Long docId, Integer version) {
        KnowledgeDocVersion v = versionMapper.selectOne(new QueryWrapper<KnowledgeDocVersion>()
                .eq("doc_id", docId).eq("version", version).eq("deleted", 0));
        if (v == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "版本不存在: v" + version);
        }
        return v;
    }

    private DocVO toDocVO(KnowledgeDoc d) {
        return DocVO.builder()
                .id(d.getId())
                .categoryId(d.getCategoryId())
                .title(d.getTitle())
                .summary(d.getSummary())
                .content(d.getContent())
                .tags(d.getTags())
                .status(d.getStatus())
                .statusLabel(statusLabel(d.getStatus()))
                .version(d.getVersion())
                .authorId(d.getAuthorId())
                .reviewerId(d.getReviewerId())
                .publishedAt(d.getPublishedAt())
                .archivedAt(d.getArchivedAt())
                .createTime(d.getCreateTime())
                .updateTime(d.getUpdateTime())
                .build();
    }

    private DocActionVO actionVO(KnowledgeDoc d, String msg, Long operatorId) {
        return DocActionVO.builder()
                .docId(d.getId())
                .status(d.getStatus())
                .statusLabel(statusLabel(d.getStatus()))
                .version(d.getVersion())
                .actionTime(LocalDateTime.now())
                .operatorId(operatorId)
                .message(msg)
                .build();
    }

    private String statusLabel(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case STATUS_DRAFT -> "草稿";
            case STATUS_PENDING -> "待审核";
            case STATUS_PUBLISHED -> "已发布";
            case STATUS_ARCHIVED -> "已归档";
            case STATUS_REJECTED -> "已驳回";
            default -> "未知";
        };
    }

    /**
     * 极简 unified diff（仅做演示用，不追求完美）。
     */
    private String simpleUnifiedDiff(String from, String to) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- v").append("from").append('\n');
        sb.append("+++ v").append("to").append('\n');
        if (from.equals(to)) {
            sb.append("(内容相同)");
            return sb.toString();
        }
        sb.append("- (省略 ").append(from.length()).append(" 字符)\n");
        sb.append("+ (省略 ").append(to.length()).append(" 字符)");
        return sb.toString();
    }
}
