<template>
  <div class="exam-taking-list">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>📝 在线作答（4.16）</span>
          <el-tag>W1 骨架 · W2 接入真实接口</el-tag>
        </div>
      </template>

      <el-alert
        title="W1 占位页面"
        type="info"
        :closable="false"
        description="W2 将对接王茗瑾的『考试管理』模块获取真实待考列表"
      />

      <el-table :data="mockList" stripe style="margin-top: 16px">
        <el-table-column prop="title" label="考试名称" />
        <el-table-column prop="duration" label="时长（分钟）" width="140" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="截止时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default>
            <el-button type="primary" size="small" disabled>开始考试（W2）</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="todo">
      <template #header><span>W2 待实现</span></template>
      <ul>
        <li>调用 <code>GET /api/v1/exam-taking/{examId}/paper</code> 渲染试卷</li>
        <li>调用 <code>POST /api/v1/exam-taking/{examId}/save</code> 单题暂存（Redis）</li>
        <li>调用 <code>POST /api/v1/exam-taking/{examId}/save-all</code> 批量暂存</li>
        <li>调用 <code>POST /api/v1/exam-taking/{examId}/submit</code> 交卷</li>
        <li>调用 <code>GET /api/v1/exam-taking/{examId}/snapshot</code> 断点续答</li>
        <li>前端计时器 + 切题 + 自动交卷倒计时</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup lang="ts">
const mockList = [
  {
    title: '（示例）Java 高级开发考试',
    duration: 60,
    startTime: '2026-07-10 14:00',
    endTime: '2026-07-10 15:00'
  },
  {
    title: '（示例）广汽产品知识竞赛',
    duration: 30,
    startTime: '2026-07-12 10:00',
    endTime: '2026-07-12 10:30'
  }
]
</script>

<style scoped lang="scss">
.exam-taking-list {
  .todo {
    margin-top: 16px;

    ul {
      margin: 0;
      padding-left: 20px;
      color: var(--gac-text-secondary);

      li {
        line-height: 28px;
      }
    }

    code {
      background: #f0f0f0;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 12px;
    }
  }
}
</style>
