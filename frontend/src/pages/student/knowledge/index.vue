<template>
  <div class="knowledge">
    <el-row :gutter="16">
      <!-- 左侧：目录树 -->
      <el-col :span="6">
        <el-card shadow="never" class="tree-card">
          <template #header>
            <div class="card-header">
              <span>📚 知识库目录</span>
              <el-tag size="small">W2</el-tag>
            </div>
          </template>
          <el-tree
            :data="treeData"
            node-key="id"
            :props="{ label: 'name' }"
            default-expand-all
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <el-icon><Folder /></el-icon>
                <span>{{ node.label }}</span>
                <el-tag v-if="data.count" size="small" type="info">{{ data.count }}</el-tag>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧：文档列表 + AI 抽题 -->
      <el-col :span="18">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>📖 文档列表（4.19）</span>
              <div>
                <el-button type="primary" :icon="MagicStick" disabled>AI 抽题（W4）</el-button>
              </div>
            </div>
          </template>

          <el-input
            v-model="keyword"
            placeholder="搜索文档..."
            clearable
            style="margin-bottom: 16px"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>

          <el-table :data="filteredDocs" stripe>
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="tags" label="标签" width="160">
              <template #default="{ row }">
                <el-tag v-for="t in row.tags" :key="t" size="small" effect="plain" style="margin-right: 4px">
                  {{ t }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default>
                <el-button size="small" disabled>查看</el-button>
                <el-button size="small" type="primary" disabled>历史版本</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-divider />

          <el-alert type="info" :closable="false">
            <p><strong>W2 待实现：</strong></p>
            <ul>
              <li><code>GET /api/v1/knowledge/tree</code> 目录树</li>
              <li><code>GET /api/v1/knowledge/docs</code> 文档列表</li>
              <li><code>GET /api/v1/knowledge/docs/{id}/versions</code> 版本列表</li>
              <li><code>GET /api/v1/knowledge/docs/{id}/diff</code> 版本对比</li>
            </ul>
            <p><strong>W4 待实现：</strong></p>
            <ul>
              <li><code>POST /api/v1/knowledge/ai/extract</code> AI 文档 → 题目</li>
              <li>富文本编辑器（用于新建 / 编辑文档）</li>
            </ul>
          </el-alert>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { MagicStick, Search, Folder } from '@element-plus/icons-vue'

const keyword = ref('')

// W1 占位数据
const treeData = [
  {
    id: 1,
    name: '产品手册',
    count: 12,
    children: [
      { id: 11, name: '广汽埃安' },
      { id: 12, name: '广汽传祺' }
    ]
  },
  { id: 2, name: '设计文档', count: 8 },
  { id: 3, name: '运维手册', count: 5 },
  { id: 4, name: '培训课件', count: 20 }
]

const docs = [
  { title: '埃安 LX Plus 用户手册', category: '产品手册', tags: ['产品', '埃安'], status: 'PUBLISHED' },
  { title: '云学习系统架构设计', category: '设计文档', tags: ['架构', '设计'], status: 'PUBLISHED' },
  { title: '广汽传祺 GS8 操作指南', category: '产品手册', tags: ['产品', '传祺'], status: 'PENDING' }
]

const filteredDocs = computed(() =>
  docs.filter((d) => !keyword.value || d.title.includes(keyword.value))
)

function statusLabel(s: string) {
  return { DRAFT: '草稿', PENDING: '审核中', PUBLISHED: '已发布', ARCHIVED: '已归档' }[s] || s
}
function statusType(s: string) {
  return ({ DRAFT: 'info', PENDING: 'warning', PUBLISHED: 'success', ARCHIVED: '' } as any)[s] || ''
}
</script>

<style scoped lang="scss">
.knowledge {
  .tree-card {
    min-height: 600px;
  }
  .tree-node {
    display: flex;
    align-items: center;
    gap: 6px;
    width: 100%;
  }
  code {
    background: #f0f0f0;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
  }
}
</style>
