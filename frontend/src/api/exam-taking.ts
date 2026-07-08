import request from './request'

/**
 * 试卷渲染数据
 */
export interface PaperRenderVO {
  examId: number
  paperId: number
  title: string
  durationMinutes: number
  questions: QuestionVO[]
}

export interface QuestionVO {
  id: number
  type: 'SINGLE' | 'MULTI' | 'JUDGE' | 'FILL' | 'ESSAY' | 'CODE'
  stem: string
  options?: { key: string; value: string }[]
  score: number
}

/**
 * 在线作答 API（方雨菲负责）
 */
export const examTakingApi = {
  /** 获取试卷 */
  getPaper: (examId: number) =>
    request.get<unknown, PaperRenderVO>(`/exam-taking/${examId}/paper`),

  /** 单题暂存 */
  saveAnswer: (examId: number, payload: { questionId: number; answer: string; version: number }) =>
    request.post(`/exam-taking/${examId}/save`, payload),

  /** 批量暂存 */
  saveAll: (examId: number, answers: Record<number, string>) =>
    request.post(`/exam-taking/${examId}/save-all`, { answers }),

  /** 交卷 */
  submit: (examId: number) => request.post(`/exam-taking/${examId}/submit`),

  /** 获取当前快照（断点续答） */
  getSnapshot: (examId: number) => request.get(`/exam-taking/${examId}/snapshot`),

  /** 剩余时间 */
  getRemaining: (examId: number) => request.get(`/exam-taking/${examId}/remaining`)
}
