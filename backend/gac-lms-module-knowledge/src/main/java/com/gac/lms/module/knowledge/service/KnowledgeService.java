package com.gac.lms.module.knowledge.service;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.knowledge.dto.DocCreateRequest;
import com.gac.lms.module.knowledge.dto.ReviewRequest;
import com.gac.lms.module.knowledge.vo.CategoryTreeNode;
import com.gac.lms.module.knowledge.vo.DocActionVO;
import com.gac.lms.module.knowledge.vo.DocDiffVO;
import com.gac.lms.module.knowledge.vo.DocVO;
import com.gac.lms.module.knowledge.vo.DocVersionVO;

import java.util.List;

/**
 * 知识管理 Service。
 *
 * <h3>文档状态机</h3>
 * <pre>
 *   DRAFT ──submit──▶ PENDING ──approve──▶ PUBLISHED ──archive──▶ ARCHIVED
 *                          └──reject───▶ REJECTED ──(回到 DRAFT)─▶ DRAFT
 * </pre>
 *
 * @author 方雨菲
 */
public interface KnowledgeService {

    /** 获取知识库目录树（含文档数） */
    List<CategoryTreeNode> getCategoryTree();

    /** 创建文档（DRAFT） */
    DocVO createDoc(DocCreateRequest req, Long authorId);

    /** 更新文档（仅 DRAFT/REJECTED 状态可编辑） */
    DocVO updateDoc(Long id, DocCreateRequest req, Long operatorId);

    /** 获取文档详情 */
    DocVO getDoc(Long id);

    /** 文档列表（按分类筛选） */
    PageResult<DocVO> listDocs(Long categoryId, Integer status, int pageNum, int pageSize);

    /** 提交审核（DRAFT → PENDING） */
    DocActionVO submitForReview(Long id, Long operatorId);

    /** 审核通过（PENDING → PUBLISHED） */
    DocActionVO approve(Long id, ReviewRequest req, Long reviewerId);

    /** 审核驳回（PENDING → REJECTED） */
    DocActionVO reject(Long id, ReviewRequest req, Long reviewerId);

    /** 发布（仅 DRAFT 可直接发布，跳过审核） */
    DocActionVO publish(Long id, Long operatorId);

    /** 归档（PUBLISHED → ARCHIVED） */
    DocActionVO archive(Long id, Long operatorId);

    /** 版本列表 */
    List<DocVersionVO> listVersions(Long docId);

    /** 版本对比 */
    DocDiffVO diffVersions(Long docId, Integer from, Integer to);
}
