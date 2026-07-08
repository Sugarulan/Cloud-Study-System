import request from './request'

/**
 * 个人测评 API（方雨菲负责）
 */
export const selfTestApi = {
  /** 待考列表 */
  pendingExams: () => request.get('/self-test/exams/pending'),

  /** 已考列表 */
  finishedExams: () => request.get('/self-test/exams/finished'),

  /** 错题本 */
  wrongQuestions: () => request.get('/self-test/wrong-questions'),

  /** 错题详情 + AI 解析 */
  wrongQuestionDetail: (id: number) => request.get(`/self-test/wrong-questions/${id}`),

  /** 触发 AI 解析 */
  triggerAiExplain: (id: number) => request.post(`/self-test/wrong-questions/${id}/ai`)
}
