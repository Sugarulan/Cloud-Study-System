<template>
  <div class="self-test">
    <el-tabs v-model="activeTab">
      <!-- 待考 -->
      <el-tab-pane label="待考" name="pending">
        <el-card shadow="never">
          <el-empty description="W1 占位：W2 接入 GET /api/v1/self-test/exams/pending" />
        </el-card>
      </el-tab-pane>

      <!-- 已考 -->
      <el-tab-pane label="已考" name="finished">
        <el-card shadow="never">
          <el-empty description="W1 占位：W2 接入 GET /api/v1/self-test/exams/finished" />
        </el-card>
      </el-tab-pane>

      <!-- 错题本 -->
      <el-tab-pane label="错题本" name="wrong">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>📒 错题本（4.17）</span>
              <el-tag type="warning">W4 接入 AI 解析</el-tag>
            </div>
          </template>

          <el-empty description="W1 占位：W2 接入 GET /api/v1/self-test/wrong-questions" />

          <el-divider />

          <el-alert type="info" :closable="false">
            <p><strong>W2 待实现：</strong></p>
            <ul>
              <li>错题列表（分页、按知识点筛选）</li>
              <li>错题详情：题干 / 选项 / 学员答案 / 正确答案</li>
              <li>标记掌握 / 重做</li>
            </ul>
            <p><strong>W4 待实现：</strong></p>
            <ul>
              <li>AI 错题解析（调用 <code>POST /api/v1/ai/explain</code>）</li>
              <li>知识点推荐</li>
              <li>同类题推荐</li>
            </ul>
          </el-alert>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const activeTab = ref('pending')
</script>

<style scoped lang="scss">
.self-test {
  code {
    background: #f0f0f0;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
  }
}
</style>
