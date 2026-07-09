# API 接口契约（初稿）

> 本文档为 W1 末冻结的接口契约初稿，**冻结后字段不再随意变更**。
> 所有模块接口统一前缀 `/api/v1`，统一响应格式见 §1。
> 王茗瑾负责的 5 个模块接口见 §2，方雨菲负责的 5 个模块接口见 §3，AI 模块接口见 §4。

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

## 2. 王茗瑾���责的 5 个模块

### 2.1 账号管理（3.3.1）

```
POST   /api/v1/auth/login                # 登录（返回 JWT）
POST   /api/v1/auth/logout               # 登出
GET    /api/v1/accounts                  # 账号列表（分页 + 筛选）
POST   /api/v1/accounts                  # 创建账号
GET    /api/v1/accounts/{id}             # 账号详情
PUT    /api/v1/accounts/{id}             # 更新账号
DELETE /api/v1/accounts/{id}             # 停用账号
POST   /api/v1/accounts/{id}/reset-password  # 重置密码
POST   /api/v1/accounts/{id}/roles       # 分配角色
GET    /api/v1/roles                     # 角色列表
POST   /api/v1/roles                     # 创建角色
```

#### 登录请求

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "EncryptedPwd@123"
}
```

#### 登录响应

```json
{
  "code": 0,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 7200,
    "userInfo": {
      "id": 1001,
      "username": "zhangsan",
      "realName": "张三",
      "roles": ["STUDENT"]
    }
  }
}
```

### 2.2 人员信息管理（3.3.2）

```
GET    /api/v1/persons                   # 人员列表（按系统/部门筛选）
POST   /api/v1/persons                   # 创建人员
GET    /api/v1/persons/{id}              # 人员详情
PUT    /api/v1/persons/{id}              # 更新人员
POST   /api/v1/persons/{id}/link-account # 关联账号
POST   /api/v1/persons/import            # Excel 批量导入
GET    /api/v1/persons/export            # Excel 导出
```

### 2.3 题目管理（3.3.3）

```
GET    /api/v1/questions                 # 题目列表（分页 + 分类筛选）
POST   /api/v1/questions                 # 创建题目
GET    /api/v1/questions/{id}            # 题目详情
PUT    /api/v1/questions/{id}            # 更新题目
DELETE /api/v1/questions/{id}            # 删除题目
POST   /api/v1/questions/import          # 批量导入
GET    /api/v1/questions/export          # 批量导出
GET    /api/v1/questions/tags            # 标签列表
GET    /api/v1/questions/categories      # 分类树
```

### 2.4 试卷管理（3.3.4）

```
GET    /api/v1/papers                    # 试卷列表
POST   /api/v1/papers                    # 创建试卷（手动选题）
POST   /api/v1/papers/auto               # 抽题组卷（按策略自动）
GET    /api/v1/papers/{id}               # 试卷详情
PUT    /api/v1/papers/{id}               # 更新试卷
DELETE /api/v1/papers/{id}               # 删除试卷
POST   /api/v1/papers/{id}/publish       # 发布试卷
POST   /api/v1/papers/{id}/unpublish     # 取消发布
```

### 2.5 考试管理（3.3.5）

```
GET    /api/v1/exams                     # 考试列表
POST   /api/v1/exams                     # 创建考试
GET    /api/v1/exams/{id}                # 考试详情
PUT    /api/v1/exams/{id}                # 更新考试
DELETE /api/v1/exams/{id}                # 删除考试
POST   /api/v1/exams/{id}/publish        # 发布考试
POST   /api/v1/exams/{id}/close          # 关闭考试
GET    /api/v1/exams/{id}/scope          # 参考人员范围
PUT    /api/v1/exams/{id}/scope          # 设置参考人员
GET    /api/v1/exams/{id}/submit-rules   # 交卷规则
PUT    /api/v1/exams/{id}/submit-rules   # 设置交卷规则
```

#### 交卷规则

```json
{
  "autoSubmit": true,
  "autoSubmitOnTimeout": true,
  "allowManualSubmit": true,
  "allowSaveProgress": true
}
```

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

## 5. 系统集成模块（3.3.11，方雨菲负责）

> 横跨基础与扩展两侧，本节列出**方雨菲负责部分**（AI 集成、站内信、Webhook）。
> 邮件发送底层由 SMTP 实现，考试提醒、成绩发布由方雨菲编写，调用方为王茗瑾的账号/考试模块。

### 5.1 邮件通知（W4 实现）

```
POST /api/v1/integration/email/send              # 通用邮件发送
POST /api/v1/integration/email/template          # 模板邮件（考试提醒/成绩发布）
POST /api/v1/integration/email/batch             # 批量发送
GET  /api/v1/integration/email/logs              # 发送日志查询
```

#### 模板邮件（考试提醒）

```http
POST /api/v1/integration/email/template
Content-Type: application/json

{
  "templateCode": "EXAM_REMIND",
  "to": ["zhangsan@gac.local"],
  "params": {
    "examName": "Java 高级开发考试",
    "startTime": "2026-07-15 14:00",
    "duration": 60
  }
}
```

### 5.2 站内信（W4 实现）

```
POST /api/v1/integration/message/push            # 推送单条站内信
POST /api/v1/integration/message/batch           # 批量推送
GET  /api/v1/integration/message/unread-count    # 当前用户未读数
GET  /api/v1/integration/message/list            # 收件箱列表
POST /api/v1/integration/message/{id}/read       # 标记已读
```

#### 推送站内信

```http
POST /api/v1/integration/message/push
Content-Type: application/json

{
  "userId": 1001,
  "type": "EXAM_REMIND",
  "title": "【考试提醒】您有一场待考",
  "content": "《Java 高级开发》考试将于明日 14:00 开始，请准时参加。"
}
```

### 5.3 Webhook 推送（W5 实现）

```
GET    /api/v1/integration/webhooks              # Webhook 配置列表
POST   /api/v1/integration/webhooks              # 新增 Webhook
PUT    /api/v1/integration/webhooks/{id}         # 更新配置
DELETE /api/v1/integration/webhooks/{id}         # 删除配置
POST   /api/v1/integration/webhooks/{id}/test    # 测试推送
GET    /api/v1/integration/webhooks/{id}/logs    # 推送历史
```

### 5.4 系统集成事件总线（W5 实现）

业务模块（考试发布、成绩发布、错题入库）通过 Spring `ApplicationEventPublisher`
发布事件，由 `3.3.11` 监听并触发邮件 / 站内信 / Webhook 推送：

```java
// 业务模块发布
applicationEventPublisher.publishEvent(new ExamPublishedEvent(examId, examName));

// 3.3.11 监听
@EventListener
public void onExamPublished(ExamPublishedEvent event) {
    // 1. 给所有参考人员发站内信
    // 2. 触发邮件模板（如果开启了邮件通知）
    // 3. 推送 Webhook
}
```

---

## 6. 待王茗瑾对齐的依赖接口

| 你需要调用 | 接口 | 用途 |
|-----------|------|------|
| 试卷数据 | `GET /api/v1/papers/{id}` | 在线作答试卷渲染 |
| 考试数据 | `GET /api/v1/exams/{id}` | 校验考试周期 / 参考范围 |
| 题目数据 | `GET /api/v1/questions/{id}` | 在线作答加载题目 |
| 用户身份 | `GET /api/v1/auth/me` | 当前用户 |

> ⚠️ 以上接口**字段命名和路径**需要在 W1 末由王茗瑾最终确认，W2 冻结。
