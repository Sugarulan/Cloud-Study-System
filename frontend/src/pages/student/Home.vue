<template>
  <div class="home">
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in stats" :key="item.label">
        <el-card shadow="hover">
          <div class="stat">
            <div class="stat-icon" :style="{ background: item.color }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome" shadow="never">
      <template #header>
        <div class="card-header">
          <span>欢迎使用 GAC-LMS</span>
          <el-tag type="success">W1 阶段</el-tag>
        </div>
      </template>
      <p>这是学员端首页（W1 占位）。W3 将接入真实待考列表与公告。</p>
      <ul>
        <li>📝 在线作答：参加考试、暂存、断点续答</li>
        <li>📊 个人测评：成绩、错题本、AI 解析</li>
        <li>📚 知识管理：知识库浏览、AI 抽题</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { EditPen, DataAnalysis, Reading, Bell } from '@element-plus/icons-vue'

const stats = [
  { label: '待考', value: 0, color: '#1890ff', icon: EditPen },
  { label: '已通过', value: 0, color: '#52c41a', icon: DataAnalysis },
  { label: '待学知识', value: 0, color: '#faad14', icon: Reading },
  { label: '未读消息', value: 0, color: '#ff4d4f', icon: Bell }
]
</script>

<style scoped lang="scss">
.home {
  .stat {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 8px;
      color: #fff;
      font-size: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .stat-value {
      font-size: 22px;
      font-weight: 600;
    }

    .stat-label {
      color: var(--gac-text-secondary);
      font-size: 13px;
    }
  }

  .welcome {
    margin-top: 16px;
  }
}
</style>
