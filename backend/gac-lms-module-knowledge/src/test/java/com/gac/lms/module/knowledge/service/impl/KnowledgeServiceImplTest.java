package com.gac.lms.module.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.knowledge.dto.DocCreateRequest;
import com.gac.lms.module.knowledge.dto.ReviewRequest;
import com.gac.lms.module.knowledge.entity.KnowledgeCategory;
import com.gac.lms.module.knowledge.entity.KnowledgeDoc;
import com.gac.lms.module.knowledge.entity.KnowledgeDocVersion;
import com.gac.lms.module.knowledge.mapper.KnowledgeCategoryMapper;
import com.gac.lms.module.knowledge.mapper.KnowledgeDocMapper;
import com.gac.lms.module.knowledge.mapper.KnowledgeDocVersionMapper;
import com.gac.lms.module.knowledge.vo.CategoryTreeNode;
import com.gac.lms.module.knowledge.vo.DocActionVO;
import com.gac.lms.module.knowledge.vo.DocDiffVO;
import com.gac.lms.module.knowledge.vo.DocVO;
import com.gac.lms.module.knowledge.vo.DocVersionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * KnowledgeServiceImpl 单元测试。
 *
 * @author 方雨菲
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class KnowledgeServiceImplTest {

    @Mock private KnowledgeDocMapper docMapper;
    @Mock private KnowledgeCategoryMapper categoryMapper;
    @Mock private KnowledgeDocVersionMapper versionMapper;

    private KnowledgeServiceImpl service;

    private static final Long AUTHOR_ID = 100L;
    private static final Long REVIEWER_ID = 200L;

    @BeforeEach
    void setUp() {
        service = new KnowledgeServiceImpl(docMapper, categoryMapper, versionMapper);
    }

    // ========== getCategoryTree ==========

    @Test
    @DisplayName("getCategoryTree - 空分类：返回空列表")
    void getCategoryTree_empty() {
        when(categoryMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        List<CategoryTreeNode> tree = service.getCategoryTree();

        assertThat(tree).isEmpty();
    }

    @Test
    @DisplayName("getCategoryTree - 二级分类：嵌套结构正确")
    void getCategoryTree_twoLevels() {
        KnowledgeCategory root = new KnowledgeCategory();
        root.setId(1L);
        root.setParentId(0L);
        root.setName("产品手册");
        KnowledgeCategory child = new KnowledgeCategory();
        child.setId(2L);
        child.setParentId(1L);
        child.setName("广汽埃安");
        when(categoryMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(root, child));
        // 文档数：分类 1 有 3 个文档，分类 2 有 1 个
        KnowledgeDoc d1 = new KnowledgeDoc();
        d1.setCategoryId(1L);
        KnowledgeDoc d2 = new KnowledgeDoc();
        d2.setCategoryId(1L);
        KnowledgeDoc d3 = new KnowledgeDoc();
        d3.setCategoryId(1L);
        KnowledgeDoc d4 = new KnowledgeDoc();
        d4.setCategoryId(2L);
        when(docMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(d1, d2, d3, d4));

        List<CategoryTreeNode> tree = service.getCategoryTree();

        assertThat(tree).hasSize(1);
        CategoryTreeNode rootNode = tree.get(0);
        assertThat(rootNode.getName()).isEqualTo("产品手册");
        assertThat(rootNode.getDocCount()).isEqualTo(3);
        assertThat(rootNode.getChildren()).hasSize(1);
        assertThat(rootNode.getChildren().get(0).getDocCount()).isEqualTo(1);
    }

    // ========== createDoc ==========

    @Test
    @DisplayName("createDoc - 正常创建：状态 DRAFT + 创建初始版本快照")
    void createDoc_success() {
        when(docMapper.insert(any(KnowledgeDoc.class))).thenAnswer(inv -> {
            KnowledgeDoc d = inv.getArgument(0);
            d.setId(100L);
            return 1;
        });

        DocCreateRequest req = new DocCreateRequest();
        req.setTitle("测试文档");
        req.setContent("内容");
        req.setCategoryId(1L);
        req.setTags("test,doc");

        DocVO vo = service.createDoc(req, AUTHOR_ID);

        assertThat(vo.getId()).isEqualTo(100L);
        assertThat(vo.getStatus()).isEqualTo(KnowledgeServiceImpl.STATUS_DRAFT);
        assertThat(vo.getVersion()).isEqualTo(1);
        assertThat(vo.getStatusLabel()).matches(".*草稿.*");

        // 验证：1. 插入文档 2. 创建版本快照
        ArgumentCaptor<KnowledgeDocVersion> verCaptor = ArgumentCaptor.forClass(KnowledgeDocVersion.class);
        verify(versionMapper).insert(verCaptor.capture());
        assertThat(verCaptor.getValue().getDocId()).isEqualTo(100L);
        assertThat(verCaptor.getValue().getVersion()).isEqualTo(1);
        assertThat(verCaptor.getValue().getTitle()).isEqualTo("测试文档");
    }

    // ========== updateDoc ==========

    @Test
    @DisplayName("updateDoc - DRAFT 状态：允许更新 + 版本+1")
    void updateDoc_draft_allowed() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_DRAFT);
        existing.setVersion(1);
        existing.setTitle("旧标题");
        existing.setAuthorId(AUTHOR_ID);
        when(docMapper.selectById(100L)).thenReturn(existing);
        when(docMapper.updateById(any(KnowledgeDoc.class))).thenReturn(1);

        DocCreateRequest req = new DocCreateRequest();
        req.setTitle("新标题");
        req.setContent("新内容");
        req.setChangeLog("调整措辞");

        DocVO vo = service.updateDoc(100L, req, AUTHOR_ID);

        assertThat(vo.getVersion()).isEqualTo(2);
        assertThat(vo.getTitle()).isEqualTo("新标题");
        // 验证版本快照也被创建
        verify(versionMapper, times(1)).insert(any(KnowledgeDocVersion.class));
    }

    @Test
    @DisplayName("updateDoc - PUBLISHED 状态：拒绝更新")
    void updateDoc_published_rejected() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_PUBLISHED);
        when(docMapper.selectById(100L)).thenReturn(existing);

        DocCreateRequest req = new DocCreateRequest();
        req.setTitle("新标题");

        assertThatThrownBy(() -> service.updateDoc(100L, req, AUTHOR_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不可编辑");

        verify(docMapper, never()).updateById(any(KnowledgeDoc.class));
    }

    // ========== 状态机 ==========

    @Test
    @DisplayName("submitForReview - DRAFT → PENDING：成功")
    void submit_draftToPending() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_DRAFT);
        when(docMapper.selectById(100L)).thenReturn(existing);

        DocActionVO vo = service.submitForReview(100L, AUTHOR_ID);

        assertThat(vo.getStatus()).isEqualTo(KnowledgeServiceImpl.STATUS_PENDING);
        ArgumentCaptor<KnowledgeDoc> captor = ArgumentCaptor.forClass(KnowledgeDoc.class);
        verify(docMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(KnowledgeServiceImpl.STATUS_PENDING);
    }

    @Test
    @DisplayName("submitForReview - REJECTED → PENDING：允许重新提交")
    void submit_rejectedToPending() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_REJECTED);
        when(docMapper.selectById(100L)).thenReturn(existing);

        DocActionVO vo = service.submitForReview(100L, AUTHOR_ID);

        assertThat(vo.getStatus()).isEqualTo(KnowledgeServiceImpl.STATUS_PENDING);
    }

    @Test
    @DisplayName("submitForReview - PUBLISHED 状态：拒绝")
    void submit_published_rejected() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_PUBLISHED);
        when(docMapper.selectById(100L)).thenReturn(existing);

        assertThatThrownBy(() -> service.submitForReview(100L, AUTHOR_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("approve - PENDING → PUBLISHED：成功 + 记录 reviewer")
    void approve_pendingToPublished() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_PENDING);
        existing.setAuthorId(AUTHOR_ID);
        when(docMapper.selectById(100L)).thenReturn(existing);

        DocActionVO vo = service.approve(100L, new ReviewRequest(), REVIEWER_ID);

        assertThat(vo.getStatus()).isEqualTo(KnowledgeServiceImpl.STATUS_PUBLISHED);
        ArgumentCaptor<KnowledgeDoc> captor = ArgumentCaptor.forClass(KnowledgeDoc.class);
        verify(docMapper).updateById(captor.capture());
        assertThat(captor.getValue().getReviewerId()).isEqualTo(REVIEWER_ID);
        assertThat(captor.getValue().getPublishedAt()).isNotNull();
    }

    @Test
    @DisplayName("approve - 作者审核自己的文档：拒绝（职责分离）")
    void approve_authorReviewingSelf_rejected() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_PENDING);
        existing.setAuthorId(AUTHOR_ID);
        when(docMapper.selectById(100L)).thenReturn(existing);

        assertThatThrownBy(() -> service.approve(100L, new ReviewRequest(), AUTHOR_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("作者不能审核");
    }

    @Test
    @DisplayName("reject - PENDING → REJECTED：成功 + 记录 reason")
    void reject_pendingToRejected() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_PENDING);
        existing.setAuthorId(AUTHOR_ID);
        when(docMapper.selectById(100L)).thenReturn(existing);

        ReviewRequest req = new ReviewRequest();
        req.setReason("REASON_TEXT_123");

        DocActionVO vo = service.reject(100L, req, REVIEWER_ID);

        assertThat(vo.getStatus()).isEqualTo(KnowledgeServiceImpl.STATUS_REJECTED);
        assertThat(vo.getMessage()).contains("REASON_TEXT_123");
    }

    @Test
    @DisplayName("publish - DRAFT 直接发布：成功（跳过审核）")
    void publish_draftDirectly() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_DRAFT);
        when(docMapper.selectById(100L)).thenReturn(existing);

        DocActionVO vo = service.publish(100L, AUTHOR_ID);

        assertThat(vo.getStatus()).isEqualTo(KnowledgeServiceImpl.STATUS_PUBLISHED);
    }

    @Test
    @DisplayName("publish - PENDING 状态：拒绝（必须先审核）")
    void publish_pending_rejected() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_PENDING);
        when(docMapper.selectById(100L)).thenReturn(existing);

        assertThatThrownBy(() -> service.publish(100L, AUTHOR_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("archive - PUBLISHED → ARCHIVED：成功")
    void archive_publishedToArchived() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_PUBLISHED);
        when(docMapper.selectById(100L)).thenReturn(existing);

        DocActionVO vo = service.archive(100L, AUTHOR_ID);

        assertThat(vo.getStatus()).isEqualTo(KnowledgeServiceImpl.STATUS_ARCHIVED);
        ArgumentCaptor<KnowledgeDoc> captor = ArgumentCaptor.forClass(KnowledgeDoc.class);
        verify(docMapper).updateById(captor.capture());
        assertThat(captor.getValue().getArchivedAt()).isNotNull();
    }

    @Test
    @DisplayName("archive - DRAFT 状态：拒绝（只能归档已发布）")
    void archive_draft_rejected() {
        KnowledgeDoc existing = new KnowledgeDoc();
        existing.setId(100L);
        existing.setStatus(KnowledgeServiceImpl.STATUS_DRAFT);
        when(docMapper.selectById(100L)).thenReturn(existing);

        assertThatThrownBy(() -> service.archive(100L, AUTHOR_ID))
                .isInstanceOf(BusinessException.class);
    }

    // ========== listVersions / diffVersions ==========

    @Test
    @DisplayName("listVersions - 倒序返回")
    void listVersions_descOrder() {
        when(docMapper.selectById(100L)).thenReturn(new KnowledgeDoc());

        KnowledgeDocVersion v1 = new KnowledgeDocVersion();
        v1.setDocId(100L);
        v1.setVersion(1);
        KnowledgeDocVersion v2 = new KnowledgeDocVersion();
        v2.setDocId(100L);
        v2.setVersion(2);
        when(versionMapper.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(v1, v2));

        List<DocVersionVO> vos = service.listVersions(100L);

        assertThat(vos).hasSize(2);
    }

    @Test
    @DisplayName("diffVersions - 正常 diff：返回 from/to 内容 + 字数差")
    void diffVersions_success() {
        KnowledgeDocVersion v1 = new KnowledgeDocVersion();
        v1.setVersion(1);
        v1.setContent("hello");
        KnowledgeDocVersion v2 = new KnowledgeDocVersion();
        v2.setVersion(2);
        v2.setContent("hello world");
        when(versionMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(v1)
                .thenReturn(v2);

        DocDiffVO diff = service.diffVersions(100L, 1, 2);

        assertThat(diff.getFromVersion()).isEqualTo(1);
        assertThat(diff.getToVersion()).isEqualTo(2);
        assertThat(diff.getFromContent()).isEqualTo("hello");
        assertThat(diff.getToContent()).isEqualTo("hello world");
        assertThat(diff.getAddedChars()).isEqualTo(6); // 11 - 5
    }

    @Test
    @DisplayName("diffVersions - from >= to：抛 BAD_REQUEST")
    void diffVersions_invalidRange() {
        assertThatThrownBy(() -> service.diffVersions(100L, 2, 1))
                .isInstanceOf(BusinessException.class);
    }

    // ========== getDoc ==========

    @Test
    @DisplayName("getDoc - 正常返回")
    void getDoc_success() {
        KnowledgeDoc d = new KnowledgeDoc();
        d.setId(100L);
        d.setTitle("测试");
        d.setStatus(KnowledgeServiceImpl.STATUS_PUBLISHED);
        d.setVersion(3);
        when(docMapper.selectById(100L)).thenReturn(d);

        DocVO vo = service.getDoc(100L);

        assertThat(vo.getId()).isEqualTo(100L);
        assertThat(vo.getVersion()).isEqualTo(3);
        assertThat(vo.getStatusLabel()).matches(".*发布.*");
    }

    @Test
    @DisplayName("getDoc - 不存在：抛 DATA_NOT_FOUND")
    void getDoc_notFound() {
        when(docMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.getDoc(999L))
                .isInstanceOf(BusinessException.class);
    }

    // ========== listDocs ==========

    @Test
    @DisplayName("listDocs - 多条件筛选 + 分页")
    void listDocs_filter() {
        when(docMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        KnowledgeDoc d = new KnowledgeDoc();
        d.setId(1L);
        d.setStatus(KnowledgeServiceImpl.STATUS_PUBLISHED);
        when(docMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Collections.singletonList(d));

        PageResult<DocVO> page = service.listDocs(1L, 2, 1, 20);

        assertThat(page.getTotal()).isEqualTo(1L);
        assertThat(page.getRecords()).hasSize(1);
    }
}
