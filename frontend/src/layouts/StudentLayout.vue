<template>
  <el-container class="student-layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo">
        <span v-if="!collapsed">GAC-LMS</span>
        <span v-else>G</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        router
        background-color="#001529"
        text-color="#ffffffcc"
        active-text-color="#1890ff"
      >
        <el-menu-item index="/student">
          <el-icon><HomeFilled /></el-icon>
          <template #title>学员首页</template>
        </el-menu-item>
        <el-menu-item index="/student/exam-taking">
          <el-icon><EditPen /></el-icon>
          <template #title>在线作答</template>
        </el-menu-item>
        <el-menu-item index="/student/self-test">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>个人测评</template>
        </el-menu-item>
        <el-menu-item index="/student/knowledge">
          <el-icon><Reading /></el-icon>
          <template #title>知识管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <el-icon class="collapse-btn" @click="collapsed = !collapsed">
          <Expand v-if="collapsed" />
          <Fold v-else />
        </el-icon>
        <div class="title">{{ currentTitle }}</div>
        <div class="user-area">
          <el-tag type="info" effect="plain">W1 学员端 · 方雨菲</el-tag>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const collapsed = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta?.title as string) || '学员端')
</script>

<style scoped lang="scss">
.student-layout {
  height: 100vh;
}

.aside {
  background: #001529;
  transition: width 0.2s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #ffffff14;
}

.header {
  background: #fff;
  border-bottom: 1px solid var(--gac-border);
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 16px;

  .collapse-btn {
    font-size: 20px;
    cursor: pointer;
  }

  .title {
    flex: 1;
    font-size: 16px;
    font-weight: 500;
  }
}

.el-menu {
  border-right: none;
}
</style>
