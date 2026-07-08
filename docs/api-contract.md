# API 接口契约（初稿）

> 本文档为 W1 末冻结的接口契约初稿，**冻结后字段不再随意变更**。
> 所有模块接口统一前缀 `/api/v1`，统一响应格式见 §1。
> 方雨菲负责的 5 个模块接口见 §3。

---

## 1. 统一规范

### 1.1 响应格式

```json
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "traceId": "abc123"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 0=成功，非 0=失败 |
| `message` | string | 提示信息 |
| `data` | object | 业务数据 |
| `traceId` | string | 链路追踪 ID |

### 1.2 分页请求

```
GET /api/v1/xxx?pageNum=1&pageSize=10&keyword=xxx
```

```json
{
  "code": 0,
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "records": [ ... ]
  }
}
```

### 1.3 错误码

| 范围 | 含义 |
|------|------|
| 0 | 成功 |
| 1xxxx | 通用错误（参数、权限、系统） |
| 2xxxx | 业务错误 |
| 3xxxx | 第三方错误（AI、邮件） |

---

## 2. 王茗瑾负责的模块（参考，完整契约见王茗瑾文档）

| 模块 | 关键接口（占位） |
|------|----------------|
| 账号管理 | `POST /api/v1/auth/login`、`GET /api/v1/accounts` |
| 人员管理 | `GET /api/v1/persons`、`POST /api/v1/persons/import` |
| 题目管理 | `GET /api/v1/questions`、`POST /api/v1/questions` |
| 试卷管理 | `POST /api/v1/papers`、`POST /api/v1/papers/{id}/publish` |
| 考试管理 | `POST /api/v1/exams`、`POST /api/v1/exams/{id}/publish` |

---

## 3. 方雨菲负责的 5 个模块

### 3.1 评卷模块（3.3.6）

```
POST   /api/v1/evaluation/auto           # 自动评阅（客观题）
POST   /api/v1/evaluation/ai             # AI 评阅（主观题）
POST   /api/v1/evaluation/manual         # 人工评阅
POST   /api/v1/evaluation/{id}/review    # 评卷复核
POST   /api/v1/evaluation/{id}/publish   # 成绩发布
GET    /api/v1/evaluation/pending        # 待评卷列表（人工工作台）
```

#### 自动评阅请求

```http
POST /api/v1/evaluation/auto
Content-Type: application/json

{
  "examId": 1001,
  "userId": 8888,
  "answers": [
    { "questionId": 1, "answer": "A" },
    { "questionId": 2, "answer": ["B", "C"] }
  ]
}
```

#### 响应

```json
{
  "code": 0,
  "data": {
    "totalScore": 85.0,
    "objectiveScore": 60.0,
    "subjectiveScore": 25.0,
    "pendingSubjectiveCount": 2,
    "details": [
      { "questionId": 1, "correct": true, "score": 5 },
      { "questionId": 2, "correct": false, "score": 0 }
    ]
  }
}
```

---

### 3.2 成绩管理（3.3.7）

```
GET    /api/v1/grades                     # 多条件筛选
GET    /api/v1/grades/statistics         # 统计：分布/通过率/均分
GET    /api/v1/grades/export             # Excel 导出
GET    /api/v1/grades/{id}               # 单人成绩详情
```

#### 筛选参数

```
GET /api/v1/grades?examId=1001&userId=8888&minScore=60&maxScore=100&pageNum=1&pageSize=10
```

---

### 3.3 在线作答（3.3.8）

```
GET    /api/v1/exam-taking/{examId}/paper        # 试卷渲染
POST   /api/v1/exam-taking/{examId}/save         # 单题暂存
POST   /api/v1/exam-taking/{examId}/save-all     # 批量暂存
POST   /api/v1/exam-taking/{examId}/submit       # 交卷
GET    /api/v1/exam-taking/{examId}/snapshot     # 断点续答：获取当前快照
GET    /api/v1/exam-taking/{examId}/remaining    # 剩余时间
```

#### 单题暂存

```http
POST /api/v1/exam-taking/1001/save
Content-Type: application/json

{
  "questionId": 5,
  "answer": "B",
  "version": 3
}
```

#### 响应（乐观锁冲突）

```json
{
  "code": 1401,
  "message": "答题版本冲突，请刷新后重试",
  "data": { "currentVersion": 4 }
}
```

---

### 3.4 个人测评（3.3.9）

```
GET    /api/v1/self-test/exams/pending            # 待考列表
GET    /api/v1/self-test/exams/finished           # 已考列表
GET    /api/v1/self-test/wrong-questions          # 错题本
GET    /api/v1/self-test/wrong-questions/{id}     # 错题详情 + AI 解析
POST   /api/v1/self-test/wrong-questions/{id}/ai  # 触发 AI 解析
```

#### 错题详情响应

```json
{
  "code": 0,
  "data": {
    "questionId": 5,
    "stem": "下列关于 JVM 内存模型的描述正确的是？",
    "options": ["A. ...", "B. ...", "C. ...", "D. ..."],
    "userAnswer": "B",
    "correctAnswer": "C",
    "explanation": "AI 自动解析：JVM 内存模型主要包含...",
    "knowledgePoints": ["JVM", "内存模型"]
  }
}
```

---

### 3.5 知识管理（3.3.10）

```
GET    /api/v1/knowledge/tree                    # 知识库目录树
POST   /api/v1/knowledge/docs                    # 创建文档
PUT    /api/v1/knowledge/docs/{id}               # 更新文档
POST   /api/v1/knowledge/docs/{id}/submit        # 提交审核
POST   /api/v1/knowledge/docs/{id}/approve       # 审核通过
POST   /api/v1/knowledge/docs/{id}/publish       # 发布
POST   /api/v1/knowledge/docs/{id}/archive       # 归档
GET    /api/v1/knowledge/docs/{id}/versions      # 版本列表
GET    /api/v1/knowledge/docs/{id}/diff          # 版本对比
POST   /api/v1/knowledge/ai/extract              # AI 文档→题目
```

#### 文档状态机

```
DRAFT ─submit→ PENDING ─approve→ PUBLISHED ─archive→ ARCHIVED
                    └─reject→ REJECTED
```

---

## 4. AI 模块（5.1-5.9）

```
POST /api/v1/ai/invoke                  # 通用 LLM 调用
POST /api/v1/ai/grading                 # AI 评卷
POST /api/v1/ai/explain                 # AI 错题解析
POST /api/v1/ai/extract-questions       # AI 文档→题目
```

#### 通用调用

```http
POST /api/v1/ai/invoke
Content-Type: application/json

{
  "prompt": "请评阅以下主观题答案...",
  "temperature": 0.3,
  "maxTokens": 1000
}
```

---

## 5. 待王茗瑾对齐的依赖接口

| 你需要调用 | 接口 | 用途 |
|-----------|------|------|
| 试卷数据 | `GET /api/v1/papers/{id}` | 在线作答试卷渲染 |
| 考试数据 | `GET /api/v1/exams/{id}` | 校验考试周期 / 参考范围 |
| 题目数据 | `GET /api/v1/questions/{id}` | 在线作答加载题目 |
| 用户身份 | `GET /api/v1/auth/me` | 当前用户 |
| 邮件通知 | `POST /api/v1/integrations/email` | 考试提醒 |

> ⚠️ 以上接口**字段命名和路径**需要在 W1 末由王茗瑾最终确认，W2 冻结。
