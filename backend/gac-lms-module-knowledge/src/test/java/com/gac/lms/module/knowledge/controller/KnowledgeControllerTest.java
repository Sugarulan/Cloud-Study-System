package com.gac.lms.module.knowledge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gac.lms.common.exception.BusinessException;
import com.gac.lms.common.exception.GlobalExceptionHandler;
import com.gac.lms.common.response.ErrorCode;
import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.knowledge.dto.DocCreateRequest;
import com.gac.lms.module.knowledge.service.KnowledgeService;
import com.gac.lms.module.knowledge.vo.CategoryTreeNode;
import com.gac.lms.module.knowledge.vo.DocActionVO;
import com.gac.lms.module.knowledge.vo.DocDiffVO;
import com.gac.lms.module.knowledge.vo.DocVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * KnowledgeController 单元测试。
 *
 * @author 方雨菲
 */
class KnowledgeControllerTest {

    private MockMvc mockMvc;
    private KnowledgeService knowledgeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Long AUTHOR_ID = 100L;
    private static final Long DOC_ID = 200L;
    private static final String USER_HEADER = "X-User-Id";

    @BeforeEach
    void setUp() {
        knowledgeService = mock(KnowledgeService.class);
        KnowledgeController controller = new KnowledgeController(knowledgeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /health")
    void health_ok() throws Exception {
        mockMvc.perform(get("/api/v1/knowledge/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("knowledge-module-ok"));
    }

    @Test
    @DisplayName("GET /tree - 正常返回分类树")
    void tree_success() throws Exception {
        when(knowledgeService.getCategoryTree()).thenReturn(Collections.singletonList(
                CategoryTreeNode.builder().id(1L).name("产品手册").docCount(5L).build()));

        mockMvc.perform(get("/api/v1/knowledge/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("产品手册"))
                .andExpect(jsonPath("$.data[0].docCount").value(5));
    }

    @Test
    @DisplayName("POST /docs - 创建文档")
    void create_success() throws Exception {
        DocVO vo = DocVO.builder().id(DOC_ID).title("测试").version(1)
                .status(0).build();
        when(knowledgeService.createDoc(any(DocCreateRequest.class), eq(AUTHOR_ID))).thenReturn(vo);

        DocCreateRequest req = new DocCreateRequest();
        req.setTitle("测试");
        req.setContent("内容");

        mockMvc.perform(post("/api/v1/knowledge/docs")
                        .header(USER_HEADER, AUTHOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(200))
                .andExpect(jsonPath("$.data.version").value(1));
    }

    @Test
    @DisplayName("POST /docs - 缺 X-User-Id：UNAUTHORIZED")
    void create_missingUser() throws Exception {
        DocCreateRequest req = new DocCreateRequest();
        req.setTitle("测试");

        mockMvc.perform(post("/api/v1/knowledge/docs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    @DisplayName("POST /docs - 缺 title：参数校验失败")
    void create_validationFail() throws Exception {
        DocCreateRequest req = new DocCreateRequest();
        // 缺 title

        mockMvc.perform(post("/api/v1/knowledge/docs")
                        .header(USER_HEADER, AUTHOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()));
    }

    @Test
    @DisplayName("PUT /docs/{id} - 更新文档")
    void update_success() throws Exception {
        DocVO vo = DocVO.builder().id(DOC_ID).version(2).build();
        when(knowledgeService.updateDoc(eq(DOC_ID), any(DocCreateRequest.class), eq(AUTHOR_ID))).thenReturn(vo);

        DocCreateRequest req = new DocCreateRequest();
        req.setTitle("新标题");

        mockMvc.perform(put("/api/v1/knowledge/docs/{id}", DOC_ID)
                        .header(USER_HEADER, AUTHOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value(2));
    }

    @Test
    @DisplayName("GET /docs/{id} - 详情")
    void detail_success() throws Exception {
        DocVO vo = DocVO.builder().id(DOC_ID).title("测试").build();
        when(knowledgeService.getDoc(eq(DOC_ID))).thenReturn(vo);

        mockMvc.perform(get("/api/v1/knowledge/docs/{id}", DOC_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("测试"));
    }

    @Test
    @DisplayName("GET /docs - 列表")
    void list_success() throws Exception {
        PageResult<DocVO> page = new PageResult<>(0L, 1L, 20L, Collections.emptyList());
        when(knowledgeService.listDocs(any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/knowledge/docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageNum").value(1));
    }

    @Test
    @DisplayName("POST /docs/{id}/submit - 提交审核")
    void submit_success() throws Exception {
        DocActionVO vo = DocActionVO.builder().docId(DOC_ID).status(1).build();
        when(knowledgeService.submitForReview(eq(DOC_ID), eq(AUTHOR_ID))).thenReturn(vo);

        mockMvc.perform(post("/api/v1/knowledge/docs/{id}/submit", DOC_ID)
                        .header(USER_HEADER, AUTHOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    @DisplayName("POST /docs/{id}/approve - 审核通过")
    void approve_success() throws Exception {
        DocActionVO vo = DocActionVO.builder().docId(DOC_ID).status(2).build();
        when(knowledgeService.approve(eq(DOC_ID), any(), eq(AUTHOR_ID))).thenReturn(vo);

        mockMvc.perform(post("/api/v1/knowledge/docs/{id}/approve", DOC_ID)
                        .header(USER_HEADER, AUTHOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(2));
    }

    @Test
    @DisplayName("POST /docs/{id}/reject - 状态不允许：业务码 OPERATION_NOT_ALLOWED")
    void reject_wrongStatus() throws Exception {
        when(knowledgeService.reject(eq(DOC_ID), any(), eq(AUTHOR_ID)))
                .thenThrow(new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED));

        mockMvc.perform(post("/api/v1/knowledge/docs/{id}/reject", DOC_ID)
                        .header(USER_HEADER, AUTHOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.OPERATION_NOT_ALLOWED.getCode()));
    }

    @Test
    @DisplayName("POST /docs/{id}/publish - 直接发布")
    void publish_success() throws Exception {
        DocActionVO vo = DocActionVO.builder().docId(DOC_ID).status(2).build();
        when(knowledgeService.publish(eq(DOC_ID), eq(AUTHOR_ID))).thenReturn(vo);

        mockMvc.perform(post("/api/v1/knowledge/docs/{id}/publish", DOC_ID)
                        .header(USER_HEADER, AUTHOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(2));
    }

    @Test
    @DisplayName("POST /docs/{id}/archive - 归档")
    void archive_success() throws Exception {
        DocActionVO vo = DocActionVO.builder().docId(DOC_ID).status(3).build();
        when(knowledgeService.archive(eq(DOC_ID), eq(AUTHOR_ID))).thenReturn(vo);

        mockMvc.perform(post("/api/v1/knowledge/docs/{id}/archive", DOC_ID)
                        .header(USER_HEADER, AUTHOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(3));
    }

    @Test
    @DisplayName("GET /docs/{id}/versions - 版本列表")
    void versions_success() throws Exception {
        when(knowledgeService.listVersions(eq(DOC_ID))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/knowledge/docs/{id}/versions", DOC_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("GET /docs/{id}/diff - 版本对比")
    void diff_success() throws Exception {
        DocDiffVO diff = DocDiffVO.builder()
                .docId(DOC_ID).fromVersion(1).toVersion(2)
                .addedChars(5).removedChars(0).build();
        when(knowledgeService.diffVersions(eq(DOC_ID), eq(1), eq(2))).thenReturn(diff);

        mockMvc.perform(get("/api/v1/knowledge/docs/{id}/diff", DOC_ID)
                        .param("from", "1").param("to", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.addedChars").value(5));
    }
}
