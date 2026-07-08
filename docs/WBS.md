# WBS 任务分解（方雨菲视角）

> 本文件仅列**方雨菲负责**的 WBS 编号与任务，王茗瑾负责的部分详见其个人 WBS。
> 完整 WBS 总表参见 `project.md` / 项目开发规划.pdf。

---

## 一、责任矩阵

| 维度 | WBS 编号 | 工作内容 | 排期 |
|------|----------|----------|------|
| **后端模块** | 3.3.6 | 评卷模块（自动 / AI / 人工 / 复核 / 发布） | W2 |
| | 3.3.7 | 成绩管理（筛选 / 统计 / 导出） | W2 |
| | 3.3.8 | 在线作答（渲染 / 暂存 / 续答 / 计时 / 交卷） | W2 |
| | 3.3.9 | 个人测评（我的考试 / 错题 + AI 解析） | W2 |
| | 3.3.10 | 知识管理（目录 / 标签 / 状态机 / 版本） | W2 |
| **后端公共** | 3.3.4 | 文件服务、统一消息中心 | W2 |
| **AI 与集成** | 5.1-5.9 | Spring AI 接入、Prompt、熔断、降级 | W1(骨架) + W4 |
| **前端基础设施** | 4.1-4.6 | Vue3 脚手架 / UI / 路由 / Pinia / Axios / 公共组件 | W1 |
| **学员端** | 4.14-4.19 | 在线作答 / 个人测评 / 知识管理页面 + 联调 + 性能优化 | W3 + W5 |
| **测试** | 7.1-7.11 | 单测 / 集成 / 联调 / 压测 / 安全 / 兼容 / UAT / 回归 / 缺陷 | W2 + W5 |
| **培训交付** | 8.1-8.5 | 学员手册 / 运维手册 / API 文档 / 培训视频 | W6 |

---

## 二、W1 任务清单（本周内完成）

### 后端基础设施（你负责的部分）

- [x] **5.1** Spring AI 框架接入 + Provider 接口定义（Mock + OpenAI 兼容）
- [x] **3.3.4** 文件上传 / 下载服务接口预留（MinIO 客户端封装）
- [x] **3.3.4** 统一消息中心接口预留（站内信表结构）

### 后端业务模块空壳

- [x] **3.3.6** `gac-lms-module-evaluation` Maven 模块 + 包结构
- [x] **3.3.7** `gac-lms-module-grade` Maven 模块 + 包结构
- [x] **3.3.8** `gac-lms-module-exam-taking` Maven 模块 + 包结构
- [x] **3.3.9** `gac-lms-module-self-test` Maven 模块 + 包结构
- [x] **3.3.10** `gac-lms-module-knowledge` Maven 模块 + 包结构

### 前端基础设施

- [x] **4.1** Vue 3 + Vite + TypeScript 脚手架
- [x] **4.2** Element Plus 集成与主题
- [x] **4.3** 路由 / 权限 / Pinia
- [x] **4.4** Axios 封装 + 拦截器 + 接口类型生成
- [x] **4.5** 公共组件（Table / Form / Upload / Tree）
- [x] **4.6** 公共布局（学员端 / 管理端占位）

### 学员端页面骨架

- [x] **4.16** `pages/student/exam-taking/index.vue` —— 在线作答
- [x] **4.17** `pages/student/self-test/index.vue` —— 个人测评
- [x] **4.19** `pages/student/knowledge/index.vue` —— 知识管理

### 部署与工程化

- [x] `deploy/docker-compose.yml` —— MySQL + Redis + MinIO
- [x] `deploy/docker/Dockerfile.backend`
- [x] `deploy/nginx/nginx.conf`
- [x] `.gitignore` + `.editorconfig`
- [x] `docs/branching.md` + `docs/commit-convention.md`

### 数据库

- [x] `docs/sql/schema.sql` —— 你负责的 5 个模块的核心表结构占位
- [x] `docs/sql/data.sql` —— 字典数据 / 测试账号

### 接口契约（与王茗瑾对齐）

- [x] `docs/api-contract.md` —— 11 个模块的接口骨架（你负责 5 个）

---

## 三、与王茗瑾的衔接点

| 节点 | 你依赖他交付 | 你交付给他 |
|------|-------------|-----------|
| W1 末 | 试卷 / 考试 / 题目接口契约评审 | 学员端 UI 设计规范 |
| W2 末 | 试卷 / 考试模块可联调 | 在线作答 / 个人测评接口文档 |
| W3 整周 | 管理端可联调 | 学员端主链路演示 |
| W5 | CI / CD 流水线就绪 | 性能压测报告 |
| W6 | 联合部署 / 灰度发布 | 学员手册 / 培训视频 |
