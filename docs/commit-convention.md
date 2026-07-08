# Conventional Commits 提交规范

> 本项目强制使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范，
> 配合 WBS 编号作为 scope，便于自动生成 CHANGELOG 和模块归属统计。

---

## 1. 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 1.1 type（必填）

| type | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 仅文档变更 |
| `style` | 代码格式（不影响逻辑） |
| `refactor` | 重构（既不是新功能也不是 Bug 修复） |
| `perf` | 性能优化 |
| `test` | 测试相关 |
| `chore` | 构建 / 工具 / 依赖变更 |
| `ci` | CI 配置变更 |
| `build` | 构建系统变更 |

### 1.2 scope（必填）

使用 **WBS 编号** 或 **模块名**：

| scope 示例 | 对应 |
|-----------|------|
| `3.3.6` 或 `evaluation` | 评卷模块 |
| `3.3.7` 或 `grade` | 成绩管理 |
| `3.3.8` 或 `exam-taking` | 在线作答 |
| `3.3.9` 或 `self-test` | 个人测评 |
| `3.3.10` 或 `knowledge` | 知识管理 |
| `5.1` 或 `ai` | AI 适配层 |
| `4.16` 或 `student-exam` | 学员端在线作答页面 |

### 1.3 subject（必填）

- 简要描述变更内容
- **不超过 50 字符**
- 不使用句末标点
- 使用中文或英文均可（推荐英文）

### 1.4 body（可选）

- 详细说明变更动机、与之前行为的对比
- 每行不超过 72 字符

### 1.5 footer（可选）

- 关联 Issue：`Closes #102` / `Refs #205`
- 重大变更：`BREAKING CHANGE: <说明>`

---

## 2. 示例

### 2.1 新功能

```bash
git commit -m "feat(3.3.6): 实现客观题自动评阅接口"
```

```bash
git commit -m "feat(3.3.8): 接入 Redis 答题暂存

使用 Hash 结构存储用户答题快照，TTL 2 小时，
key 格式: exam:taking:{examId}:{userId}

Refs: #45"
```

### 2.2 Bug 修复

```bash
git commit -m "fix(3.3.8): 修复考试自动交卷定时任务重复执行问题"
```

### 2.3 文档

```bash
git commit -m "docs(2.6): 补充数据库设计 ER 图说明"
```

### 2.4 测试

```bash
git commit -m "test(3.3.6): 评卷模块补充 AI 评阅异常场景用例"
```

### 2.5 重构

```bash
git commit -m "refactor(3.3.7): 抽取成绩统计公共计算逻辑"
```

### 2.6 重大变更

```bash
git commit -m "feat(3.3.8)!: 答题暂存数据结构升级为带版本号

BREAKING CHANGE: 旧版答题数据需要通过 migration 脚本迁移，
旧 key exam:taking:* 迁移至 exam:taking:v2:*"
```

---

## 3. 工具配置

### 3.1 Commitizen（推荐）

```bash
npm install -g commitizen cz-conventional-changelog
```

在 `package.json` 中：

```json
{
  "config": {
    "commitizen": {
      "path": "cz-conventional-changelog"
    }
  }
}
```

使用 `git cz` 代替 `git commit`。

### 3.2 commitlint 校验

```bash
npm install --save-dev @commitlint/cli @commitlint/config-conventional
```

`commitlint.config.js`：

```js
module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'scope-enum': [2, 'always', [
      '3.3.6', '3.3.7', '3.3.8', '3.3.9', '3.3.10',
      'evaluation', 'grade', 'exam-taking', 'self-test', 'knowledge',
      'ai', 'infra', 'frontend', 'docs'
    ]]
  }
};
```

### 3.3 Husky Git Hooks

```bash
npx husky add .husky/commit-msg 'npx --no-install commitlint --edit "$1"'
```

---

## 4. W1 起执行的初始提交示例

```bash
git commit -m "chore(repo): 初始化 Monorepo 仓库结构与文档"
git commit -m "feat(infra): Docker Compose 一键启动 MySQL/Redis/MinIO"
git commit -m "feat(backend): Spring Boot 3 多模块脚手架"
git commit -m "feat(ai): Spring AI Provider 接口与 Mock 实现"
git commit -m "feat(frontend): Vue3 + Vite + TS 脚手架与 Element Plus 集成"
git commit -m "feat(3.3.6): 评卷模块 Maven 模块空壳"
git commit -m "feat(3.3.7): 成绩管理 Maven 模块空壳"
git commit -m "feat(3.3.8): 在线作答 Maven 模块空壳"
git commit -m "feat(3.3.9): 个人测评 Maven 模块空壳"
git commit -m "feat(3.3.10): 知识管理 Maven 模块空壳"
git commit -m "feat(student-exam): 学员端在线作答页面骨架"
git commit -m "feat(student-self-test): 学员端个人测评页面骨架"
git commit -m "feat(student-knowledge): 学员端知识管理页面骨架"
git commit -m "docs(api): 11 模块接口契约初稿"
git commit -m "docs(sql): 数据库表结构初稿（5 个业务模块）"
```
