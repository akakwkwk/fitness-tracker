import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'training',
        name: 'Training',
        component: () => import('@/views/training/Training.vue'),
        meta: { title: '训练记录' }
      },
      {
        path: 'training/create',
        name: 'TrainingCreate',
        component: () => import('@/views/training/TrainingCreate.vue'),
        meta: { title: '创建训练' }
      },
      {
        path: 'training/:id',
        name: 'TrainingDetail',
        component: () => import('@/views/training/TrainingDetail.vue'),
        meta: { title: '训练详情' }
      },
      {
        path: 'diet',
        name: 'Diet',
        component: () => import('@/views/diet/Diet.vue'),
        meta: { title: '饮食记录' }
      },
      {
        path: 'sleep',
        name: 'Sleep',
        component: () => import('@/views/sleep/Sleep.vue'),
        meta: { title: '睡眠记录' }
      },
      {
        path: 'achievement',
        name: 'Achievement',
        component: () => import('@/views/achievement/Achievement.vue'),
        meta: { title: '成就系统' }
      },
      {
        path: 'exercises',
        name: 'Exercises',
        component: () => import('@/views/exercise/Exercises.vue'),
        meta: { title: '动作库' }
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('@/views/stats/Stats.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/Settings.vue'),
        meta: { title: '个人设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.guest && userStore.isLoggedIn) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
