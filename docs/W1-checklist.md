# W1 验收清单（团队完整）

> 本周交付物的自检清单，对应 [WBS.md](./WBS.md) 中的 W1 任务。
> 王茗瑾与方雨菲共同完成的全部 W1 产出。

---

## ✅ 已完成

### 一、文档（100%）
- [x] [README.md](../README.md) —— 项目总览 + 14 模块结构 + 团队分工 + 快速启动
- [x] [docs/WBS.md](./WBS.md) —— WBS 任务分解（团队完整）
- [x] [docs/ROADMAP.md](./ROADMAP.md) —— 6 周路线图
- [x] [docs/branching.md](./branching.md) —— GitFlow 分支策略
- [x] [docs/commit-convention.md](./commit-convention.md) —— Conventional Commits 规范
- [x] [docs/architecture.md](./architecture.md) —— 系统架构（14 模块图）
- [x] [docs/api-contract.md](./api-contract.md) —— 接口契约初稿（11 模块）
- [x] [docs/sql/full-schema.sql](./sql/full-schema.sql) —— 数据库 Schema 文档
- [x] [docs/W1-checklist.md](./W1-checklist.md) —— 本文档

### 二、后端骨架（14 个模块，100%）

| 模块 | Maven 模块 | 健康检查接口 | 负责人 |
|------|-----------|-------------|--------|
| 公共 | `gac-lms-common` | - | 共同 |
| 基础设施 | `gac-lms-infrastructure` | - | 共同 |
| AI 适配层 | `gac-lms-ai` | `POST /api/v1/ai/invoke` | 方雨菲 |
| 账号管理 3.3.1 | `gac-lms-module-account` | `GET /api/v1/accounts/health` | 王茗瑾 |
| 人员信息 3.3.2 | `gac-lms-module-person` | `GET /api/v1/persons/health` | 王茗瑾 |
| 题目管理 3.3.3 | `gac-lms-module-question` | `GET /api/v1/questions/health` | 王茗瑾 |
| 试卷管理 3.3.4 | `gac-lms-module-paper` | `GET /api/v1/papers/health` | 王茗瑾 |
| 考试管理 3.3.5 | `gac-lms-module-exam` | `GET /api/v1/exams/health` | 王茗瑾 |
| 评卷 3.3.6 | `gac-lms-module-evaluation` | `GET /api/v1/evaluation/health` | 方雨菲 |
| 成绩 3.3.7 | `gac-lms-module-grade` | `GET /api/v1/grades/health` | 方雨菲 |
| 在线作答 3.3.8 | `gac-lms-module-exam-taking` | `GET /api/v1/exam-taking/health` | 方雨菲 |
| 个人测评 3.3.9 | `gac-lms-module-self-test` | `GET /api/v1/self-test/health` | 方雨菲 |
| 知识管理 3.3.10 | `gac-lms-module-knowledge` | `GET /api/v1/knowledge/health` | 方雨菲 |
| 启动 | `gac-lms-app` | `GacLmsApplication` | 共同 |

### 三、AI 适配层（100%）
- [x] `LLMProvider` 统一接口
- [x] `MockLLMProvider` 兜底实现（默认启用）
- [x] `OpenAICompatibleLLMProvider`（RestTemplate 直连 OpenAI 兼容 API，W1 默认禁用，W4 启用）
- [x] `LLMService` 熔断门面（Resilience4j）
- [x] `AIController` 通用调用接口

### 四、前端骨架（100%）
- [x] Vue 3 + Vite + TS + Element Plus + Pinia + Vue Router
- [x] Axios 统一封装（拦截器、错误处理）
- [x] 学员端布局（折叠侧边栏 + Header）
- [x] 管理端布局（占位，由王茗瑾 W3 完善）
- [x] API 类型定义（`exam-taking.ts` / `self-test.ts` / `knowledge.ts`）

### 五、学员端 3 个页面骨架（100%）
- [x] **4.16 在线作答**：考试列表 + 作答室布局（倒计时/答题卡占位）
- [x] **4.17 个人测评**：待考 / 已考 / 错题本 三 Tab
- [x] **4.19 知识管理**：左侧目录树 + 右侧文档列表 + AI 抽题占位

### 六、数据库（100%）
- [x] 方雨菲部分（schema.sql）：`grade_record` / `grade_detail` / `exam_taking` / `wrong_question` / `knowledge_doc` / `knowledge_category` / `knowledge_doc_version` / `sys_message`
- [x] 测试数据：分类 / 文档 / 消息
- [x] 完整 ER 文档：[docs/sql/full-schema.sql](./sql/full-schema.sql)
- [ ] 王茗瑾部分（账号 / 人员 / 题目 / 试卷 / 考试）的 DDL —— **W2 补充**

### 七、部署与工程化（100%）
- [x] Docker Compose（MySQL 8 + Redis 7 + MinIO + Backend + Frontend）
- [x] Backend Dockerfile（多阶段构建）
- [x] Frontend Dockerfile（Nginx）
- [x] Nginx 反向代理配置
- [x] `.gitignore` + `.editorconfig`
- [x] H2 默认 profile（零外部依赖启动）

---

## 🧪 验收步骤

### 步骤 1：编译打包（5 分钟）

```bash
cd backend
mvn clean install -DskipTests
```

预期：`BUILD SUCCESS`，所有 14 个模块编译通过。

### 步骤 2：启动后端（30 秒）

```bash
java -jar backend/gac-lms-app/target/gac-lms-app-1.0.0-SNAPSHOT.jar
```

预期输出：
```
====================================================
  GAC-LMS 启动成功（W1 基座）
  Swagger UI: http://localhost:8080/doc.html
  Knife4j   : http://localhost:8080/swagger-ui.html
  AI 健康   : http://localhost:8080/api/v1/ai/invoke (POST)
====================================================
```

> W1 默认使用 **H2 内存库**，无需启动任何外部中间件。

访问 [http://localhost:8080/doc.html](http://localhost:8080/doc.html) 应能看到 14 个模块的 Swagger 接口。

### 步骤 3：测试 11 个健康检查 + AI 调用

```bash
# 王茗瑾的 5 个模块
curl http://localhost:8080/api/v1/accounts/health
curl http://localhost:8080/api/v1/persons/health
curl http://localhost:8080/api/v1/questions/health
curl http://localhost:8080/api/v1/papers/health
curl http://localhost:8080/api/v1/exams/health

# 方雨菲的 5 个模块
curl http://localhost:8080/api/v1/evaluation/health
curl http://localhost:8080/api/v1/grades/health
curl http://localhost:8080/api/v1/exam-taking/health
curl http://localhost:8080/api/v1/self-test/health
curl http://localhost:8080/api/v1/knowledge/health

# AI 调用（Mock Provider 兜底）
curl -X POST http://localhost:8080/api/v1/ai/invoke \
  -H "Content-Type: application/json" \
  -d '{"prompt":"评阅以下主观题...","temperature":0.3}'
```

预期：每个 health 接口返回 `"xxx-module-ok"`，AI 接口返回 Mock 占位文本。

### 步骤 4：启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 [http://localhost:5173](http://localhost:5173)，验证：
- 学员首页 4 个统计卡片正常显示
- 顶部菜单可切换"在线作答 / 个人测评 / 知识管理"
- 三个页面都有占位内容 + W2/W4 待办清单

### 步骤 5：Docker Compose 全栈（可选）

```bash
docker compose -f deploy/docker-compose.yml up -d mysql redis minio
# 然后用 --spring.profiles.active=dev 启动后端，启用真实 MySQL
```

---

## ⚠️ W1 阶段已知限制

| 限制 | 原因 | 何时解决 |
|------|------|----------|
| Security 全放行 | W1 骨架阶段 | W2 接入 JWT |
| 默认 H2 内存库 | 零外部依赖快速启动 | dev profile 切换 MySQL |
| AI 调用只走 Mock | 企业内网 API 未配置 | W4 切换 OpenAI 兼容协议 |
| 前端登录态硬编码 | 无登录模块 | W2 接入王茗瑾的账号模块 |
| 学员端 3 个页面无真实数据 | 业务接口未开发 | W2-W3 |
| 王茗瑾模块 DDL 未并入 schema.sql | W1 分头并行 | W2 合并 |

---

## 📊 W1 统计

| 指标 | 数量 |
|------|------|
| 文档文件 | 9 |
| 后端 Maven 模块 | 14 |
| 后端 Java 类 | ~25 |
| 前端页面 | 6（含 Home / NotFound） |
| 前端 API 封装 | 4 |
| 数据库表 | 8（方雨菲部分，王茗瑾部分 W2 补充） |
| Docker 服务 | 5 |

---

## 🤝 W1 末与 W2 初衔接

W1 已完结。下一步（**W2 第一周启动会**）需要确认：
1. [ ] 王茗瑾交付的 5 个基础域模块的可联调性
2. [ ] 接口契约 [docs/api-contract.md](./api-contract.md) 字段冻结
3. [ ] 双方各自分支 `develop` 拉出，开始功能开发
4. [ ] MySQL / Redis 容器启动，从 H2 切换到 MySQL 验证
