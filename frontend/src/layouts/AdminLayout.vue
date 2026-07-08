<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">GAC-LMS · 管理端</div>
      <el-menu router :default-active="$route.path">
        <el-menu-item index="/admin">
          <el-icon><Setting /></el-icon>
          <template #title>管理首页</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main>
      <el-alert
        title="管理端由王茗瑾负责"
        type="info"
        :closable="false"
        description="W1 阶段仅占位，管理端页面在 W2 末接入"
        show-icon
      />
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
}
.aside {
  background: #001529;
  color: #fff;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #ffffff14;
}
</style>
