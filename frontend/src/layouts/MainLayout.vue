<template>
  <div class="layout" :class="{ 'dark-mode': isDark }">
    <!-- 移动端顶部导航 -->
    <header class="mobile-header hide-desktop">
      <div class="header-left">
        <el-icon @click="toggleSidebar" class="menu-icon"><Operation /></el-icon>
        <span class="logo-text">FitTracker</span>
      </div>
      <div class="header-right">
        <el-icon @click="toggleDark" class="theme-icon">
          <Moon v-if="!isDark" />
          <Sunny v-else />
        </el-icon>
      </div>
    </header>

    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ open: sidebarOpen }">
      <div class="sidebar-header">
        <div class="logo">
          <el-icon :size="28" color="var(--primary)"><DataBoard /></el-icon>
          <span class="logo-text">FitTracker Pro</span>
        </div>
        <el-icon class="close-btn hide-desktop" @click="sidebarOpen = false"><Close /></el-icon>
      </div>

      <nav class="nav-menu">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          @click="sidebarOpen = false"
        >
          <el-icon :size="20"><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info" v-if="userStore.userInfo">
          <el-avatar :size="36" :src="userStore.userInfo.avatar">
            {{ userStore.userInfo.nickname?.[0] || userStore.userInfo.username?.[0] }}
          </el-avatar>
          <div class="user-details">
            <span class="user-name">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
            <span class="user-role">{{ userRole }}</span>
          </div>
        </div>
        <el-button type="danger" text @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-button>
      </div>
    </aside>

    <!-- 遮罩层 -->
    <div class="sidebar-overlay" v-if="sidebarOpen" @click="sidebarOpen = false"></div>

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="content-header hide-mobile">
        <h1 class="page-title">{{ currentTitle }}</h1>
        <div class="header-actions">
          <el-icon @click="toggleDark" class="theme-toggle" :size="20">
            <Moon v-if="!isDark" />
            <Sunny v-else />
          </el-icon>
        </div>
      </div>
      <div class="content-body">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { trainingApi } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const sidebarOpen = ref(false)
const isDark = ref(false)
const trainingCount = ref(0)

const userRole = computed(() => {
  const n = trainingCount.value
  if (n >= 200) return '健身传奇'
  if (n >= 100) return '健身狂人'
  if (n >= 50) return '健身达人'
  if (n >= 20) return '健身爱好者'
  if (n >= 5) return '初见成效'
  if (n >= 1) return '健身新手'
  return '准备出发'
})

const menuItems = [
  { path: '/dashboard', label: '仪表盘', icon: 'Odometer' },
  { path: '/training', label: '训练记录', icon: 'Trophy' },
  { path: '/exercises', label: '动作库', icon: 'List' },
  { path: '/diet', label: '饮食记录', icon: 'Food' },
  { path: '/sleep', label: '睡眠记录', icon: 'Moon' },
  { path: '/achievement', label: '成就系统', icon: 'Medal' },
  { path: '/stats', label: '数据统计', icon: 'DataLine' },
  { path: '/settings', label: '个人设置', icon: 'Setting' },
]

const currentTitle = computed(() => {
  const item = menuItems.find(m => route.path.startsWith(m.path))
  return item?.label || 'FitTracker Pro'
})

function isActive(path) {
  return route.path.startsWith(path)
}

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function toggleDark() {
  isDark.value = !isDark.value
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : '')
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}

async function loadTrainingCount() {
  try {
    const res = await trainingApi.getStats()
    if (res.code === 200) trainingCount.value = res.data?.totalTrainings || 0
  } catch (e) { /* ignore */ }
}

// 监听路由变化关闭侧边栏
watch(() => route.path, () => {
  sidebarOpen.value = false
})

onMounted(() => loadTrainingCount())
</script>

<style lang="scss" scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-primary);
}

.mobile-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 100;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .menu-icon, .theme-icon {
    font-size: 22px;
    cursor: pointer;
    color: var(--text-primary);
  }

  .logo-text {
    font-size: 18px;
    font-weight: 700;
    color: var(--primary);
  }
}

.sidebar {
  width: 260px;
  background: var(--bg-card);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 200;
  transition: transform 0.3s ease;

  @media (max-width: 768px) {
    transform: translateX(-100%);

    &.open {
      transform: translateX(0);
    }
  }
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 150;
}

.sidebar-header {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border);

  .logo {
    display: flex;
    align-items: center;
    gap: 10px;

    .logo-text {
      font-size: 20px;
      font-weight: 700;
      color: var(--primary);
    }
  }

  .close-btn {
    cursor: pointer;
    font-size: 20px;
    color: var(--text-secondary);
  }
}

.nav-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;

  .nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    border-radius: var(--radius-sm);
    color: var(--text-secondary);
    transition: var(--transition);
    margin-bottom: 4px;
    font-weight: 500;

    &:hover {
      background: var(--bg-secondary);
      color: var(--text-primary);
    }

    &.active {
      background: linear-gradient(135deg, var(--primary), var(--primary-light));
      color: white;
      box-shadow: 0 4px 12px rgba(15, 118, 110, 0.3);
    }
  }
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--border);

  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .user-details {
      display: flex;
      flex-direction: column;

      .user-name {
        font-weight: 600;
        font-size: 14px;
        color: var(--text-primary);
      }

      .user-role {
        font-size: 12px;
        color: var(--text-muted);
      }
    }
  }
}

.main-content {
  flex: 1;
  margin-left: 260px;
  min-height: 100vh;

  @media (max-width: 768px) {
    margin-left: 0;
    padding-top: 56px;
  }
}

.content-header {
  padding: 24px 32px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;

  .page-title {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
  }

  .theme-toggle {
    cursor: pointer;
    color: var(--text-secondary);
    transition: var(--transition);

    &:hover {
      color: var(--primary);
    }
  }
}

.content-body {
  padding: 24px 32px;

  @media (max-width: 768px) {
    padding: 16px;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
