# W1 验收清单（方雨菲）

> 本周交付物的自检清单，对应 [WBS.md](./WBS.md) 中的 W1 任务。

---

## ✅ 已完成

### 一、文档（100%）
- [x] [README.md](../README.md) —— 项目总览 + 仓库结构 + 团队分工 + 快速启动
- [x] [docs/WBS.md](./WBS.md) —— WBS 任务分解
- [x] [docs/ROADMAP.md](./ROADMAP.md) —— 6 周路线图
- [x] [docs/branching.md](./branching.md) —— GitFlow 分支策略
- [x] [docs/commit-convention.md](./commit-convention.md) —— Conventional Commits 规范
- [x] [docs/architecture.md](./architecture.md) —— 系统架构��
- [x] [docs/api-contract.md](./api-contract.md) —— 接口契约初稿（11 模块）
- [x] [docs/sql/full-schema.sql](./sql/full-schema.sql) —— 数据库 Schema 文档

### 二、后端骨架（100%）

| 模块 | Maven 模块 | 健康检查接口 |
|------|-----------|-------------|
| 公共 | `gac-lms-common` | - |
| 基础设施 | `gac-lms-infrastructure` | - |
| AI 适配层（方雨菲） | `gac-lms-ai` | `POST /api/v1/ai/invoke` |
| 评卷 3.3.6（方雨菲） | `gac-lms-module-evaluation` | `GET /api/v1/evaluation/health` |
| 成绩 3.3.7（方雨菲） | `gac-lms-module-grade` | `GET /api/v1/grades/health` |
| 在线作答 3.3.8（方雨菲） | `gac-lms-module-exam-taking` | `GET /api/v1/exam-taking/health` |
| 个人测评 3.3.9（方雨菲） | `gac-lms-module-self-test` | `GET /api/v1/self-test/health` |
| 知识管理 3.3.10（方雨菲） | `gac-lms-module-knowledge` | `GET /api/v1/knowledge/health` |
| 启动 | `gac-lms-app` | `GacLmsApplication` |

### 三、AI 适配层（100%）
- [x] `LLMProvider` 统一接口
- [x] `MockLLMProvider` 兜底实现
- [x] `OpenAICompatibleLLMProvider`（Spring AI）
- [x] `LLMService` 熔断门面（Resilience4j）
- [x] `AIController` 通用调用接口

### 四、前端骨架（100%）
- [x] Vue 3 + Vite + TS + Element Plus + Pinia + Vue Router
- [x] Axios 统一封装（拦截器、错误处理）
- [x] 学员端布局（折叠侧边栏 + Header）
- [x] 管理端布局（占位）
- [x] API 类型定义（`exam-taking.ts` / `self-test.ts` / `knowledge.ts`）

### 五、学员端 3 个页面骨架（100%）
- [x] **4.16 在线作答**：考试列表 + 作答室布局（倒计时/答题卡占位）
- [x] **4.17 个人测评**：待考 / 已考 / 错题本 三 Tab
- [x] **4.19 知识管理**：左侧目录树 + 右侧文档列表 + AI 抽题占位

### 六、数据库（100%）
- [x] 评卷 / 成绩 / 在线作答 / 错题本 / 知识库 / 站内消息 6 套表 DDL
- [x] 测试数据（分类 / 文档 / 消息）
- [x] ER 图文字版说明

### 七、部署与工程化（100%）
- [x] Docker Compose（MySQL 8 + Redis 7 + MinIO + Backend + Frontend）
- [x] Backend Dockerfile（多阶段构建）
- [x] Frontend Dockerfile（Nginx��
- [x] Nginx 反向代理配置
- [x] `.gitignore` + `.editorconfig`

---

## 🧪 验收步骤

### 步骤 1：启动基础设施（5 分钟）

```bash
# 在仓库根目录
docker compose -f deploy/docker-compose.yml up -d mysql redis minio
```

预期：3 个容器 healthy。

### 步骤 2：启动后端

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run -pl gac-lms-app
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

访问 [http://localhost:8080/doc.html](http://localhost:8080/doc.html) 应能看到所有 Swagger 接口。

### 步骤 3：测试 6 个健康检查 + AI 调用

```bash
# 健康检查（每个返回 "xxx-module-ok"）
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
docker compose -f deploy/docker-compose.yml up -d
```

预期：5 个容器全 healthy，前端 [http://localhost:5173](http://localhost:5173)，后端 [http://localhost:8080/doc.html](http://localhost:8080/doc.html)。

---

## ⚠️ W1 阶段已知限制

| 限制 | 原因 | 何时解决 |
|------|------|----------|
| Security 全放行 | W1 骨架阶段 | W2 接入 JWT |
| 后端只用 H2 内存库 | 容器未启动时也能跑 | dev profile 切换 MySQL |
| AI 调用只走 Mock | 企业内网 API 未配置 | W4 切换 OpenAI 兼容协议 |
| 前端登录态硬编码 | 无登录模块 | W2 接入王茗瑾的账号模块 |
| 数据库 schema 与全量版有差距 | W1 仅含方雨菲负责的 5 个模块 | W2 与王茗瑾合并 |
| 学员端 3 个页面无真实数据 | 业务接口未开发 | W2-W3 |

---

## 📊 W1 统计

| 指标 | 数量 |
|------|------|
| 文档文件 | 8 |
| 后端模块 | 10 |
| 后端 Java 类 | 17 |
| 前端页面 | 6（含 Home/NotFound） |
| 前端 API 封装 | 4 |
| 数据库表 | 7 |
| Docker 服务 | 5 |

---

## 🤝 与王茗瑾的衔接（W1 末）

需要王茗瑾在本周末前确认：
1. [ ] 接口契约 [docs/api-contract.md](./api-contract.md) 中 5.x 系列接口（试卷 / 考试 / 题目 / 账号）字段命名
2. [ ] 数据库 schema 中 `account` / `person` / `question` / `paper` / `exam` 表是否需要新增字段
3. [ ] 是否同步 W1 末创建 `feature/w1-scaffold` PR
