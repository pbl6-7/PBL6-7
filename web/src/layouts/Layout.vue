<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/home')">
          <el-icon><School /></el-icon>
          <span>校园活动平台</span>
        </div>
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          class="header-menu"
          :ellipsis="false"
        >
          <el-menu-item index="home" @click="router.push('/home')">首页</el-menu-item>
          <el-menu-item index="my-activities" @click="router.push('/my-activities')">我的活动</el-menu-item>
          <el-menu-item index="my-registrations" @click="router.push('/my-registrations')">我的报名</el-menu-item>
          <el-menu-item index="my-collections" @click="router.push('/my-collections')">我的收藏</el-menu-item>
        </el-menu>
        <div class="header-actions">
          <el-button type="primary" @click="router.push('/publish')">
            <el-icon><Plus /></el-icon>
            发布活动
          </el-button>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { School, Plus, User, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const path = route.path.split('/')[1] || 'home'
  return path
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

userStore.initUser()
</script>

<style scoped>
.layout-container {
  height: 100%;
  background-color: #f5f7fa;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  margin-right: 40px;
}

.logo .el-icon {
  font-size: 28px;
}

.header-menu {
  flex: 1;
  border: none;
  background: transparent;
}

.header-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.85);
  font-size: 15px;
}

.header-menu :deep(.el-menu-item:hover) {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

.header-menu :deep(.el-menu-item.is-active) {
  color: white;
  border-bottom: 2px solid white;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-actions .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
}

.header-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.username {
  font-size: 14px;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}
</style>
