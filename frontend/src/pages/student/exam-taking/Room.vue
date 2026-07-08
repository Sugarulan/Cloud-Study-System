<template>
  <div class="exam-room">
    <el-alert
      title="考试作答页（W2 实现）"
      type="warning"
      :closable="false"
      description="W1 占位：包含倒计时、答题卡、切题、暂存提示的完整布局将在 W2 完成"
    />

    <el-card class="layout">
      <template #header>
        <div class="card-header">
          <span>考试作答中 - 试卷 ID 占位</span>
          <el-tag type="danger">剩余时间 00:00:00</el-tag>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :span="16">
          <el-empty description="题目内容将在 W2 接入" />
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="answer-sheet">
            <template #header>答题卡</template>
            <el-empty description="W2 实现" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>

      <div class="actions">
        <el-button>暂存</el-button>
        <el-button type="primary">下一题</el-button>
        <el-button type="danger">交卷</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts"></script>

<style scoped lang="scss">
.exam-room {
  .layout {
    margin-top: 16px;
  }
  .answer-sheet {
    margin-top: 0;
  }
  .actions {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
