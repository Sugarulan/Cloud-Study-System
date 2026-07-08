# 广汽云学习管理系统（GAC-LMS）

> GAC Learning Management System — 企业级在线学习与考试平台

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)]()
[![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen)]()
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)]()
[![License](https://img.shields.io/badge/License-Internal-orange)]()

---

## 项目简介

面向广汽集团内部培训与考核场景，提供账号 / 人员 / 题库 / 试卷 / 考试 / 评卷 / 成绩 / 知识库等
完整 LMS 能力，并集成企业内网 AI 大模型实现自动评卷与错题解析。

详细需求见 [project.md](./project.md)。

---

## 仓库结构（Monorepo）

```
Cloud-Study-System/
├── backend/                  # Spring Boot 3 多模块后端
│   ├── gac-lms-common/       # 公共组件：异常、响应包装、工具类
│   ├── gac-lms-infrastructure/# 基础设施：MySQL、Redis、MinIO、安全
│   ├── gac-lms-ai/           # Spring AI 适配层（评卷 / 错题解析 / 抽题）
│   ├── gac-lms-module-account/      # 3.3.1  账号管理      (王茗瑾)
│   ├── gac-lms-module-person/       # 3.3.2  人员信息管理  (王茗瑾)
│   ├── gac-lms-module-question/     # 3.3.3  题目管理      (王茗瑾)
│   ├── gac-lms-module-paper/        # 3.3.4  试卷管理      (王茗瑾)
│   ├── gac-lms-module-exam/         # 3.3.5  考试管理      (王茗瑾)
│   ├── gac-lms-module-evaluation/   # 3.3.6  评卷模块      (方雨菲)
│   ├── gac-lms-module-grade/        # 3.3.7  成绩管理      (方雨菲)
│   ├── gac-lms-module-exam-taking/  # 3.3.8  在线作答      (方雨菲)
│   ├── gac-lms-module-self-test/    # 3.3.9  个人测评      (方雨菲)
│   └── gac-lms-module-knowledge/    # 3.3.10 知识管理      (方雨菲)
│
├── frontend/                 # Vue 3 + Vite + TS 前端
│   ├── src/
│   │   ├── layouts/          # 公共布局
│   │   ├── pages/
│   │   │   ├── admin/        # 管理端（王茗瑾负责）
│   │   │   └── student/      # 学员端（方雨菲负责）
│   │   ├── api/              # 接口封装
│   │   ├── components/       # 公共组件
│   │   ├── stores/           # Pinia 状态
│   │   ├── router/           # 路由与权限
│   │   └── utils/
│   └── ...
│
├── deploy/                   # 部署与运维
│   ├── docker-compose.yml    # 一键启动 dev 环境
│   ├── docker/               # 各服务 Dockerfile
│   ├── nginx/                # 反向代理
│   └── scripts/              # 运维脚本
│
├── docs/                     # 项目文档
│   ├── architecture.md       # 系统架构
│   ├── api-contract.md       # 前后端接口契约
│   ├── WBS.md                # WBS 任务分解
│   ├── ROADMAP.md            # 6 周路线图
│   ├── branching.md          # GitFlow 分支策略
│   └── commit-convention.md  # Conventional Commits 规范
│
└── project.md                # 原始需求（不可修改）
```

---

## 技术栈

| 层 | 技术 |
|----|------|
| 后端框架 | Spring Boot 3.x + Java 17 |
| 持久层 | MyBatis-Plus + MySQL 8 |
| 缓存 | Redis 7 |
| 鉴权 | Spring Security + JWT |
| 对象存储 | MinIO |
| AI | Spring AI（OpenAI 兼容协议，含 Mock Provider） |
| 熔断 | Resilience4j |
| 文档 | SpringDoc OpenAPI（Swagger UI） |
| 前端框架 | Vue 3 + Vite + TypeScript |
| UI 库 | Element Plus |
| 状态管理 | Pinia |
| HTTP | Axios |
| 图表 | ECharts |
| 容器化 | Docker + Docker Compose |
| 代码质量 | Checkstyle + SpotBugs + JaCoCo |

---

## 团队分工

| 成员 | 阶段定位 | 后端模块 | 前端模块 | 文档章节 |
|------|----------|----------|----------|----------|
| **王茗瑾** | 前期部署与支撑 | 账号 / 人员 / 题目 / 试卷 / 考试 / 系统集成 | 管理端（账号/人员/题目/试卷/考试/评卷/成绩） | 基础设施、数据层、部署运维 |
| **方雨菲** | 后期集成与测试 | 评卷 / 成绩 / 在线作答 / 个人测评 / 知识管理 | 学员端（在线作答 / 个人测评 / 知识管理） | AI 集成、测试、培训交付 |

详细分工见 [docs/WBS.md](./docs/WBS.md)。

---

## 6 周里程碑

| 周 | 里程碑 |
|----|--------|
| W1 | 项目骨架、AI 适配层、接口契约冻结 |
| W2 | 11 个业务模块后端 80% 完成 |
| W3 | 学员端 3 个核心页面 + 主链路联调 |
| W4 | AI 评卷 + 错题解析 + 文档抽题 |
| W5 | 测试覆盖率 ≥80%，性能压测 500 并发 |
| W6 | UAT、部署、学员手册、培训视频 |

详见 [docs/ROADMAP.md](./docs/ROADMAP.md)。

---

## 快速启动（开发环境）

```bash
# 1. 启动基础设施
docker compose -f deploy/docker-compose.yml up -d mysql redis minio

# 2. 启动后端
cd backend
mvn spring-boot:run -pl gac-lms-app

# 3. 启动前端
cd frontend
npm install
npm run dev
```

访问：
- 前端：http://localhost:5173
- 后端 Swagger：http://localhost:8080/swagger-ui.html

---

## 开发规范

- 分支策略：[docs/branching.md](./docs/branching.md)
- 提交规范：[docs/commit-convention.md](./docs/commit-convention.md)
- 接口契约：[docs/api-contract.md](./docs/api-contract.md)
