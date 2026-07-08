# GitFlow 简化版分支策略

> 本项目采用 GitFlow 简化版：`main` / `develop` / `feature/*` / `fix/*` / `hotfix/*` / `release/*`。
> 同时作为 Conventional Commits 的执行规范。

---

## 1. 分支类型与命名

| 分支 | 命名规则 | 来源 | 合并目标 | 生命周期 |
|------|----------|------|----------|----------|
| `main` | `main` | — | — | 永久 |
| `develop` | `develop` | `main` | — | 永久 |
| `feature` | `feature/<模块号>-<短描述>` | `develop` | `develop` | 模块完成后删除 |
| `fix` | `fix/<bug-id>-<描述>` | `develop` | `develop` | 修复后删除 |
| `hotfix` | `hotfix/v<版本>-<描述>` | `main` | `main` + `develop` | 紧急修复后删除 |
| `release` | `release/v<版本号>` | `develop` | `main` + `develop` | 上线后删除 |

### 示例

```
main
develop
feature/3.3.6-evaluation
feature/3.3.10-knowledge
fix/BUG-102-login-token-expired
hotfix/v1.0.1-exam-deadline-bug
release/v1.0.0
```

---

## 2. 分支保护规则

| 分支 | 保护策略 |
|------|----------|
| `main` | 禁止直接推送；必须通过 PR；至少 1 名架构师审批；CI 全绿方可合并 |
| `develop` | 禁止直接推送；必须通过 PR；至少 1 名模块负责人审批；CI 全绿方可合并 |
| `release/*` | 禁止直接推送；必须经集成测试通过后方可合并 |
| `feature/*` | 建议存活周期不超过 5 个工作日；每日 rebase develop 一次 |

---

## 3. 模块与分支对应（W1 起）

| 模块 | WBS | 负责人 | 分支 |
|------|-----|--------|------|
| 账号管理 | 3.3.1 | 王茗瑾 | `feature/3.3.1-account` |
| 人员信息��理 | 3.3.2 | 王茗瑾 | `feature/3.3.2-person` |
| 题目管理 | 3.3.3 | 王茗瑾 | `feature/3.3.3-question` |
| 试卷管理 | 3.3.4 | 王茗瑾 | `feature/3.3.4-paper` |
| 考试管理 | 3.3.5 | 王茗瑾 | `feature/3.3.5-exam` |
| 评卷模块 | 3.3.6 | **方雨菲** | `feature/3.3.6-evaluation` |
| 成绩管理 | 3.3.7 | **方雨菲** | `feature/3.3.7-grade` |
| 在线作答 | 3.3.8 | **方雨菲** | `feature/3.3.8-exam-taking` |
| 个人测评 | 3.3.9 | **方雨菲** | `feature/3.3.9-self-test` |
| 知识管理 | 3.3.10 | **方雨菲** | `feature/3.3.10-knowledge` |

---

## 4. 工作流

### 4.1 开始新功能

```bash
# 从 develop 拉取最新代码
git checkout develop
git pull origin develop

# 创建功能分支
git checkout -b feature/3.3.6-evaluation

# 开发完成后推送并创建 PR
git push origin feature/3.3.6-evaluation
# 在 Gitea / GitHub 上创建 PR → develop
```

### 4.2 修复 Bug

```bash
git checkout develop
git pull origin develop
git checkout -b fix/BUG-102-login-token-expired
# ...
git push origin fix/BUG-102-login-token-expired
# PR → develop
```

### 4.3 紧急修复（生产事故）

```bash
git checkout main
git pull origin main
git checkout -b hotfix/v1.0.1-critical-bug
# ...
git push origin hotfix/v1.0.1-critical-bug
# PR → main（修复后必须同步回 develop）
```

### 4.4 发布

```bash
git checkout develop
git pull origin develop
git checkout -b release/v1.0.0
# 只修 Bug，不再加新功能
git push origin release/v1.0.0
# PR → main（合并后打 tag）
```

---

## 5. W1 起执行的初始分支

```
main                          ← 受保护
└── develop                   ← 主开发分支，受保护
    ├── feature/w1-scaffold   ← W1 基座搭建（你 + 王茗瑾并行）
    ├── feature/3.3.6-evaluation    ← 评卷模块
    ├── feature/3.3.7-grade         ← 成绩管理
    ├── feature/3.3.8-exam-taking   ← 在线作答
    ├── feature/3.3.9-self-test     ← 个人测评
    └── feature/3.3.10-knowledge    ← 知识管理
```
