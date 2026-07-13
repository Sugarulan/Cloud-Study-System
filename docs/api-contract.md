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

## 2. 王茗瑾负责的模块

> **状态**：W2 已实现，已冻结。后续字段变更需 PR + 双方 review。

### 2.1 账号管理（3.3.1）

#### 2.1.1 认证接口 `/api/v1/auth/**`（放行，无需 JWT）

##### POST `/api/v1/auth/login` — 登录

**请求体**：
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应**（200 / code=0）：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "expireMinutes": 720,
    "userId": 1,
    "username": "admin",
    "displayName": "admin",
    "roles": ["ADMIN"],
    "permissions": ["account", "account:list", "account:create", ...]
  }
}
```

| 错误码 | 含义 |
|--------|------|
| 10002 | 用户名或密码错误 |
| 20003 | 账号已停用（status=0） |

##### GET `/api/v1/auth/me` — 当前用户

**请求头**：`Authorization: Bearer {token}`

**响应**（200 / code=0）：
```json
{
  "code": 0,
  "data": {
    "userId": 1,
    "username": "admin",
    "displayName": "admin",
    "roles": ["ADMIN"],
    "permissions": [...]
  }
}
```

| 错误码 | 含义 |
|--------|------|
| 10002 | 未登录或 token 已过期 |

##### POST `/api/v1/auth/logout` — 登出

**响应**：200 / code=0（前端清 token 即可，服务端可选黑名单）

---

#### 2.1.2 账号 CRUD `/api/v1/accounts/**`（需 JWT）

##### GET `/api/v1/accounts` — 分页

**Query 参数**：
| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | long | 页码，默认 1 |
| pageSize | long | 每页大小，默认 10 |
| keyword | string | 模糊匹配 username/email/phone |
| status | int | 0=禁用，1=启用 |

**响应**：
```json
{
  "code": 0,
  "data": {
    "total": 4,
    "pageNum": 1,
    "pageSize": 10,
    "records": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@gac-lms.local",
        "phone": "13800000001",
        "status": 1,
        "lastLoginAt": "2026-07-09 12:00:00",
        "createTime": "2026-07-09 12:00:00",
        "roles": [
          { "id": 1, "code": "ADMIN", "name": "超级管理员" }
        ]
      }
    ]
  }
}
```

##### GET `/api/v1/accounts/{id}` — 详情

**响应**：同上 records 单条元素。

##### POST `/api/v1/accounts` — 创建

**请求体**：
```json
{
  "username": "newuser01",
  "password": "Initial@1234",
  "email": "newuser01@gac-lms.local",
  "phone": "13900000000",
  "roleIds": [3]
}
```

| 错误码 | 含义 |
|--------|------|
| 20002 | 登录名已存在 |

##### PUT `/api/v1/accounts/{id}` — 更新

**请求体**（只改这些字段，username/password 不可改）：
```json
{
  "email": "newemail@gac-lms.local",
  "phone": "13900000000",
  "status": 1
}
```

##### POST `/api/v1/accounts/{id}/enable` — 启用

##### POST `/api/v1/accounts/{id}/disable` — 停用

##### POST `/api/v1/accounts/{id}/reset-password` — 管理员重置

**请求体**：`{ "newPassword": "Reset@1234" }`

##### POST `/api/v1/accounts/{id}/change-password` — 自己改密

**请求体**：
```json
{
  "oldPassword": "Old@1234",
  "newPassword": "New@1234"
}
```

| 错误码 | 含义 |
|--------|------|
| 10003 | FORBIDDEN - 只能修改自己的密码 |
| 20003 | 旧密码不正确 |

##### GET `/api/v1/accounts/{id}/roles` — 查账号角色

**响应**：
```json
{
  "code": 0,
  "data": [
    { "id": 2, "code": "TEACHER", "name": "教师" },
    { "id": 3, "code": "STUDENT", "name": "学员" }
  ]
}
```

##### POST `/api/v1/accounts/{id}/roles` — 分配角色（替换式）

**请求体**：`{ "roleIds": [2, 3] }`

**响应**：`{ "code": 0, "data": 2 }`（data = 成功分配的角色数）

| 错误码 | 含义 |
|--------|------|
| 20001 | 账号不存在 |
| 20001 | 部分角色 ID 不存在 |

##### GET `/api/v1/accounts/health` — 健康检查（无需 JWT）

---

### 2.2 角色权限管理（3.3.1）

#### `/api/v1/roles/**`（需 JWT）

##### GET `/api/v1/roles/all` — 所有启用角色（下拉用）

**Query**：`?category=` 可选过滤（不传查全部）

**响应**：
```json
{
  "code": 0,
  "data": [
    { "id": 1, "code": "ADMIN", "name": "超级管理员", "description": "拥有系统全部权限", "sort": 1, "status": 1, "permissionIds": [1, 2, 3, ...] }
  ]
}
```

##### GET `/api/v1/roles` — 分页

**Query**：`pageNum, pageSize, keyword`

##### GET `/api/v1/roles/{id}` — 详情

##### POST `/api/v1/roles` — 创建

**请求体**：
```json
{
  "code": "HR",
  "name": "HR经理",
  "description": "人力资源管理",
  "sort": 10
}
```

| 错误码 | 含义 |
|--------|------|
| 20002 | 角色编码已存在 |

##### PUT `/api/v1/roles/{id}` — 更新（不可改 code）

##### DELETE `/api/v1/roles/{id}` — 删除

| 错误码 | 含义 |
|--------|------|
| 20004 | 该角色被 N 个账号引用，无法删除 |

##### POST `/api/v1/roles/{id}/permissions` — 分配权限（替换式）

**请求体**：`{ "permissionIds": [1, 2, 3] }`

---

### 2.3 题目管理（3.3.3）

#### 2.3.1 题目 CRUD `/api/v1/questions/**`（需 JWT）

##### GET `/api/v1/questions` — 分页

**Query 参数**：
| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | long | 页码，默认 1 |
| pageSize | long | 每页大小，默认 10 |
| type | string | SINGLE/MULTI/JUDGE/ESSAY/FILL |
| difficulty | int | 1-5 |
| tagId | long | 按标签过滤（W3 优化 SQL JOIN） |
| keyword | string | 模糊匹配 stem |
| status | int | 0=草稿，1=已发布 |

##### GET `/api/v1/questions/{id}` — 详情

**响应**：
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "type": "SINGLE",
    "stem": "Java 中，用于保证多个线程间共享变量可见性的关键字是？",
    "analysis": "volatile 强制线程从主内存读写变量的最新值。",
    "difficulty": 2,
    "defaultScore": 5.00,
    "categoryId": null,
    "status": 1,
    "answer": "C",                          // 字符串 / 数组 / 布尔（按 type 解释）
    "options": [
      { "id": 1, "optKey": "A", "optValue": "synchronized", "isCorrect": false, "sort": 1 },
      { "id": 3, "optKey": "C", "optValue": "volatile",    "isCorrect": true,  "sort": 3 }
    ],
    "tagIds": [1, 4],
    "createTime": "2026-07-09 12:00:00"
  }
}
```

> **answer 字段格式**（由 `type` 决定，**前端必须按 type 解释**）：
>
> | type | answer 类型 | 示例 |
> |------|------------|------|
> | SINGLE | string | `"C"` |
> | MULTI | string[] | `["A", "B"]` |
> | JUDGE | boolean | `true` |
> | ESSAY | string 或 string[] | `"参考答案..."` |
> | FILL | string[] | `["答案1", "答案2"]` |

##### POST `/api/v1/questions` — 创建

**请求体**（单选题示例）：
```json
{
  "type": "SINGLE",
  "stem": "Java 中用于保证线程间可见性的关键字是？",
  "analysis": "volatile 强制线程从主内存读写变量的最新值",
  "difficulty": 2,
  "defaultScore": 5.00,
  "answer": "C",
  "options": [
    { "optKey": "A", "optValue": "synchronized", "isCorrect": false, "sort": 1 },
    { "optKey": "B", "optValue": "final",       "isCorrect": false, "sort": 2 },
    { "optKey": "C", "optValue": "volatile",    "isCorrect": true,  "sort": 3 },
    { "optKey": "D", "optValue": "static",      "isCorrect": false, "sort": 4 }
  ],
  "tagIds": [1, 4]
}
```

| 错误码 | 含义 |
|--------|------|
| 20003 | 答案格式不对（如 SINGLE 答案非 string，JUDGE 答案非 boolean） |
| 20003 | SINGLE/MULTI 至少 2 选项 / 至少 1 正确答案 / SINGLE 仅 1 个正确答案 |

##### PUT `/api/v1/questions/{id}` — 更新

**约束**：
- **type 不可改**（改动会破坏已发布数据的关联）
- options / tagIds 用替换式（传空数组 = 清空）

##### DELETE `/api/v1/questions/{id}` — 软删

| 错误码 | 含义 |
|--------|------|
| 20004 | 已发布的题目不能删除，请先下架 |

##### POST `/api/v1/questions/batch-delete` — 批量软删

**请求体**：`{ "ids": [6, 7, 8] }`

**响应**：成功 + 失败 ID 在错误信息里（如"成功 2 条，失败 1 条。失败 ID：[9999]"）

##### POST `/api/v1/questions/{id}/publish` — 发布（status 0→1）

| 错误码 | 含义 |
|--------|------|
| 20001 | 题目不存在 |
| 20004 | 题目已发布，无需重复操作 |
| 20003 | 题目无答案，不能发布 |

##### POST `/api/v1/questions/{id}/unpublish` — 取消发布（status 1→0）

| 错误码 | 含义 |
|--------|------|
| 20001 | 题目不存在 |
| 20004 | 题目未发布，无需取消 |
| 20004 | （W4）被试卷引用，无法取消 |

##### POST `/api/v1/questions/batch-publish` — 批量发布

**Query**：`?ids=1,2,3`

> 部分失败静默跳过（单条失败不影响其他），整体仍返回 code=0。

##### GET `/api/v1/questions/health` — 健康检查（无需 JWT）

---

#### 2.3.2 标签管理 `/api/v1/tags/**`（需 JWT）

##### GET `/api/v1/tags` — 列表

**Query**：`?category=题目`（可选，按 category 过滤，默认 `题目`）

**响应**：
```json
{
  "code": 0,
  "data": [
    { "id": 1, "name": "Java", "category": "题目", "useCount": 5 }
  ]
}
```

##### POST `/api/v1/tags?name=Redis&category=题目` — 创建

##### PUT `/api/v1/tags/{id}` — 更新

##### DELETE `/api/v1/tags/{id}` — 删除

| 错误码 | 含义 |
|--------|------|
| 20004 | 该标签被 N 个题目引用，无法删除 |

---

### 2.4 人员管理（3.3.2）

> **状态**：W2.3 已实现，详细字段契约见 [3.3.2-人员管理开发总结.md](3.3.2-人员管理开发总结.md)。

#### 人员 CRUD `/api/v1/persons/**`（需 JWT）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/persons` | 分页 + keyword + departmentId + status |
| GET | `/persons/{id}` | 详情（含账号关联 + 部门列表） |
| POST | `/persons` | 创建（可选 `createAccount=true` 同时创建账号） |
| PUT | `/persons/{id}` | 更新 |
| DELETE | `/persons/{id}` | 离职（软删） |
| GET | `/persons/{id}/departments` | 查询人员的部门列表 |
| POST | `/persons/{id}/departments` | 分配部门（替换式 + 主部门标记） |

**创建人员 body**（可选联动账号）：
```json
{
  "employeeNo": "E9004",
  "name": "测试人员",
  "gender": 1,
  "mobile": "13900009004",
  "email": "e9004@gac-lms.local",
  "status": 1,
  "createAccount": true,
  "username": "e9004",
  "password": "Test@1234",
  "roleIds": [3],
  "departmentIds": [3, 4],
  "primaryDepartmentId": 4
}
```

| 错误码 | 含义 |
|--------|------|
| 20001 | 人员不存在 |
| 20002 | 工号已存在 |
| 10001 | 主部门必须是部门列表之一 |

#### 部门 CRUD `/api/v1/departments/**`（需 JWT）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/departments/tree` | 部门树（全量递归） |
| GET | `/departments/all` | 所有部门（扁平，下拉用） |
| POST | `/departments` | 新建（Query 参数：name/code/parentId/sort/leaderId） |
| PUT | `/departments/{id}` | 更新 |
| DELETE | `/departments/{id}` | 删除（无子部门且无人员） |

| 错误码 | 含义 |
|--------|------|
| 20001 | 部门不存在 |
| 20002 | 同级部门名称已存在 |
| 20004 | 有子部门/有人员，无法删除 |

### 2.5 试卷管理（3.3.4）—— 待开发

> W2.3 计划开发。包含自动抽题组卷算法（最复杂的接口）。

### 2.6 考试管理（3.3.5）—— 待开发

> W2.3 计划开发。含状态机 + 定时任务 + 参考范围。

---

### 2.7 通用约定

| 项 | 约定 |
|----|------|
| **认证** | 除 `/api/v1/auth/**`、`/actuator/health`、Swagger UI、`/api/v1/*/health` 外，所有接口需 `Authorization: Bearer {token}` |
| **JWT 有效期** | 默认 12 小时（720 分钟），可通过 `JWT_EXPIRE_MINUTES` 环境变量调整 |
| **JWT Claims** | `sub`(userId) / `username` / `roles` / `perms` |
| **密码策略** | BCrypt 强度 10，长度 ≥ 8，含字母+数字 |
| **审计字段** | 所有 Entity 由 `MyMetaObjectHandler` 自动填充 `createBy/createTime/updateBy/updateTime` |
| **逻辑删除** | `@TableLogic` 字段 `deleted`（0=未删，1=已删），查询自动过滤 |
| **乐观锁** | 主表用 `@Version` 字段，中间表不用 |
| **分页响应** | `{ total, pageNum, pageSize, records }` |
| **错误码** | 0=成功 / 1xxxx=通用 / 2xxxx=业务 / 3xxxx=第三方 |

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
