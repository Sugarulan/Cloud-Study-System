import request from './request'

/**
 * 知识管理 API（方雨菲负责）
 */
export const knowledgeApi = {
  /** 目录树 */
  getTree: () => request.get('/knowledge/tree'),

  /** 文档列表 */
  listDocs: (categoryId?: number) =>
    request.get('/knowledge/docs', { params: { categoryId } }),

  /** 文档版本 */
  versions: (id: number) => request.get(`/knowledge/docs/${id}/versions`),

  /** 版本对比 */
  diff: (id: number, from: number, to: number) =>
    request.get(`/knowledge/docs/${id}/diff`, { params: { from, to } }),

  /** AI 文档 → 题目 */
  aiExtract: (docId: number) => request.post('/knowledge/ai/extract', { docId })
}
