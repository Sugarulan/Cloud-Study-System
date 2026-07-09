# WBS 任务分解（团队完整视图）

> 本文件覆盖整个项目的 WBS 编号与任务，按 **王茗瑾 / 方雨菲** 两人职责拆分。
> 原始 WBS 总表参见 `project.md` 与项目开发规划.pdf。

---

## 一、责任矩阵（11 个业务模块 + 公共能力）

### 王茗瑾负责（前期基础域）

| 维度 | WBS 编号 | 工作内容 | 排期 |
|------|----------|----------|------|
| **后端模块** | 3.3.1 | 账号管理（账号 CRUD / RBAC / 密码策略 / 重置） | W2 |
| | 3.3.2 | 人员信息管理（CRUD / 部门分组 / Excel 导入导出） | W2 |
| | 3.3.3 | 题目管理（CRUD / 6 类题型 / 分类标签 / 批量导入导出） | W2 |
| | 3.3.4 | 试卷管理（CRUD / 抽题组卷 / 发布） | W2 |
| | 3.3.5 | 考试管理（CRUD / 周期 / 参考范围 / 发布） | W2 |
| **后端公共** | 3.3.4 | 系统集成（邮件 / Webhook / 第三方登录） | W2 |
| **基础设施** | 3.1.1-3.1.6 | Spring Boot 多模块工程 / MyBatis-Plus / Redis / Security / Knife4j | W1 ✅ |
| **数据层** | 3.2.1-3.2.6 | 数据库设计 / DDL / 字典 / 初始数据 | W2 |
| **部署运维** | 6.1-6.10 | Docker / CI/CD / Nginx / 监控 / 备份 / 灰度 | W5-W6 |
| **管理端** | 4.7-4.13 | 账号 / 人员 / 题目 / 试卷 / 考试 / 评卷 / 成绩管理页面 | W3 |

### 方雨菲负责（后期扩展域）

| 维度 | WBS 编号 | 工作内容 | 排期 |
|------|----------|----------|------|
| **后端模块** | 3.3.6 | 评卷模块（自动 / AI / 人工 / 复核 / 发布） | W2 |
| | 3.3.7 | 成绩管理（筛选 / 统计 / 导出） | W2 |
| | 3.3.8 | 在线作答（渲染 / 暂存 / 续答 / 计时 / 交卷） | W2 |
| | 3.3.9 | 个人测评（我的考试 / 错题 + AI 解析） | W2 + W4 |
| | 3.3.10 | 知识管理（目录 / 标签 / 状态机 / 版本） | W2 + W4 |
| **后端公共** | 3.3.4 | 文件上传 / 下载、统一消息中心（站内信） | W2 |
| **AI 与集成** | 5.1-5.9 | AI Provider 接口 + RestTemplate 直连 + 熔断 / 降级 | W1 骨架 ✅ + W4 |
| **前端基础设施** | 4.1-4.6 | Vue3 脚手架 / UI / 路由 / Pinia / Axios / 公共组件 | W1 ✅ |
| **学员端** | 4.14-4.19 | 在线作答 / 个人测评 / 知识管理页面 + 联调 + 性能优化 | W3 + W5 |
| **测试** | 7.1-7.11 | 单测 / 集成 / 联调 / 压测 / 安全 / 兼容 / UAT / 回归 / 缺陷 | W2 + W5 |
| **培训交付** | 8.1-8.5 | 学员手册 / 运维手册 / API 文档 / 培训视频 | W6 |

---

## 二、W1 任务清单（已完成 ✅）

### 后端基础设施（共同）

- [x] **3.1.1** Spring Boot 3 多模块工程脚手架（10 + 4 = 14 个 Maven 模块）
- [x] **3.1.2** MyBatis-Plus + 数据库集成（含 H2 默认 / MySQL dev profile）
- [x] **3.1.3** Redis 配置 + Jackson 序列化
- [x] **3.1.4** Spring Security + JWT 骨架（W1 占位，W2 启用鉴权）
- [x] **3.1.5** Knife4j 接口文档（[http://localhost:8080/doc.html](http://localhost:8080/doc.html)）
- [x] **3.1.6** Docker / Docker Compose / Nginx

### 王茗瑾（W1 末交付）

- [x] **3.3.1** `gac-lms-module-account` 模块 + `/health` 接口
- [x] **3.3.2** `gac-lms-module-person` 模块 + `/health` 接口
- [x] **3.3.3** `gac-lms-module-question` 模块 + `/health` 接口
- [x] **3.3.4** `gac-lms-module-paper` 模块 + `/health` 接口
- [x] **3.3.5** `gac-lms-module-exam` 模块 + `/health` 接口

### 方雨菲（W1 末交付）

- [x] **5.1** AI Provider 接口 + Mock Provider + OpenAI 兼容 Provider（RestTemplate 直连）
- [x] **5.6** Resilience4j 熔断（AI 调用失败 → 自动降级到 Mock）
- [x] **3.3.6** `gac-lms-module-evaluation` 模块 + `/health` 接口
- [x] **3.3.7** `gac-lms-module-grade` 模块 + `/health` 接口
- [x] **3.3.8** `gac-lms-module-exam-taking` 模块 + `/health` 接口
- [x] **3.3.9** `gac-lms-module-self-test` 模块 + `/health` 接口
- [x] **3.3.10** `gac-lms-module-knowledge` 模块 + `/health` 接口
- [x] **3.3.4** `sys_message` 站内消息表（schema.sql）
- [x] **3.3.4** `MinIO` 文件上传接口预留（infrastructure 模块）

### 前端基础设施（方雨菲）

- [x] **4.1** Vue 3 + Vite + TypeScript 脚手架
- [x] **4.2** Element Plus 集成与主题
- [x] **4.3** 路由 / 权限 / Pinia
- [x] **4.4** Axios 封装 + 拦截器 + 接口类型生成
- [x] **4.5** 公共组件（Table / Form / Upload / Tree）—— **W3 完善**
- [x] **4.6** 公共布局（学员端 / 管理端占位）

### 学员端页面骨架（方雨菲）

- [x] **4.16** `pages/student/exam-taking/index.vue` —— 在线作答
- [x] **4.17** `pages/student/self-test/index.vue` —— 个人测评
- [x] **4.19** `pages/student/knowledge/index.vue` —— 知识管理

### 部署与工程化

- [x] `deploy/docker-compose.yml` —— MySQL + Redis + MinIO
- [x] `deploy/docker/Dockerfile.backend`（多阶段构建）
- [x] `deploy/docker/Dockerfile.frontend`（Nginx）
- [x] `deploy/nginx/nginx.conf`
- [x] `.gitignore` + `.editorconfig`
- [x] `docs/branching.md` + `docs/commit-convention.md`

### 数据库

- [x] 方雨菲负责的 5 个模块的 DDL：`grade_record` / `grade_detail` / `exam_taking` / `wrong_question` / `knowledge_doc` / `knowledge_category` / `knowledge_doc_version` / `sys_message`
- [x] 测试数据：分类 / 文档 / 消息

### 接口契约

- [x] `docs/api-contract.md` —— 11 模块的接口骨架初稿（W2 冻结）

---

## 三、两人衔接点（W2 末 / W3 / W6）

| 节点 | 方雨菲 → 王茗瑾 | 王茗瑾 → 方雨菲 |
|------|------------------|------------------|
| W1 末 | 学员端 UI 设计规范、接口契约初稿评审 | 5 个基础域模块骨架 commit |
| W2 末 | 在线作答 / 个人测评 / 知识管理 接口文档 | 试卷 / 考试 / 题目 / 账号 / 人员 模块可联调 |
| W3 整周 | 学员端主链路演示 | 管理端可联调 |
| W5 | 性能压测报告 | CI / CD 流水线就绪 |
| W6 | 学员手册 / 培训视频 | 联合部署 / 灰度发布 |
