<template>
  <div class="dashboard animate-fade-in">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h2>你好，{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}!</h2>
        <p>{{ greeting }}，今天也要坚持训练哦</p>
      </div>
      <div class="quick-actions">
        <el-button type="primary" @click="$router.push('/training/create')">
          <el-icon><Plus /></el-icon>开始训练
        </el-button>
        <el-button @click="showDietDialog = true">
          <el-icon><Food /></el-icon>记录饮食
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card training-card">
        <div class="stat-icon">
          <el-icon :size="24"><Trophy /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ dashboardData.todayTrainingCount || 0 }}</span>
          <span class="stat-label">今日训练</span>
        </div>
      </div>

      <div class="stat-card calorie-card">
        <div class="stat-icon">
          <el-icon :size="24"><Food /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ formatNumber(dashboardData.todayCaloriesIntake) }}</span>
          <span class="stat-label">摄入卡路里</span>
        </div>
      </div>

      <div class="stat-card streak-card">
        <div class="stat-icon">
          <el-icon :size="24"><Star /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ dashboardData.streakDays || 0 }}</span>
          <span class="stat-label">连续打卡</span>
        </div>
      </div>

      <div class="stat-card water-card">
        <div class="stat-icon">
          <el-icon :size="24"><ColdDrink /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ dashboardData.todayWaterIntake || 0 }}ml</span>
          <span class="stat-label">今日饮水</span>
        </div>
        <div class="water-actions">
          <el-button v-for="amt in waterAmounts" :key="amt" size="small" @click.stop="addWater(amt)">+{{ amt }}</el-button>
        </div>
      </div>
    </div>

    <!-- 本周进度 -->
    <div class="progress-section">
      <div class="card">
        <h3 class="section-title">本周训练进度</h3>
        <div class="progress-bar-container">
          <el-progress
            :percentage="weeklyProgress"
            :stroke-width="12"
            color="var(--primary)"
          />
          <span class="progress-text">
            {{ dashboardData.weeklyTrainingCount || 0 }} / {{ dashboardData.weeklyTrainingTarget || 5 }} 次
          </span>
        </div>
      </div>

      <div class="card">
        <h3 class="section-title">今日营养摄入</h3>
        <div class="nutrition-bars">
          <div class="nutrition-item">
            <span class="nutrition-label">卡路里</span>
            <el-progress
              :percentage="calorieProgress"
              :stroke-width="8"
              color="#F97316"
            />
            <span class="nutrition-value">{{ formatNumber(dashboardData.todayCaloriesIntake) }} / {{ userStore.userInfo?.dailyCalorieTarget || 2000 }} kcal</span>
          </div>
          <div class="nutrition-item">
            <span class="nutrition-label">蛋白质</span>
            <el-progress
              :percentage="proteinProgress"
              :stroke-width="8"
              color="#3B82F6"
            />
            <span class="nutrition-value">{{ formatNumber(dashboardData.todayProtein) }} / {{ userStore.userInfo?.dailyProteinTarget || 150 }} g</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近训练 -->
    <div class="card recent-training">
      <h3 class="section-title">最近训练</h3>
      <div v-if="dashboardData.recentTrainings?.length" class="training-list">
        <div
          v-for="item in dashboardData.recentTrainings"
          :key="item.id"
          class="training-item"
          @click="$router.push(`/training/${item.id}`)"
        >
          <div class="training-info">
            <span class="training-name">{{ item.name || '训练' }}</span>
            <span class="training-date">{{ formatDate(item.trainingDate) }}</span>
          </div>
          <div class="training-meta">
            <el-tag size="small">{{ item.trainingType || '综合' }}</el-tag>
            <span class="training-duration">{{ item.duration || 0 }}分钟</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无训练记录" :image-size="80" />
    </div>

    <!-- 营养趋势图 -->
    <div class="card chart-section">
      <h3 class="section-title">本周营养趋势</h3>
      <div ref="nutritionChart" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { dashboardApi, waterApi } from '@/api'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const userStore = useUserStore()
const dashboardData = ref({})
const nutritionChart = ref(null)
const showDietDialog = ref(false)
const waterAmounts = [200, 300, 500]

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const weeklyProgress = computed(() => {
  const count = dashboardData.value.weeklyTrainingCount || 0
  const target = dashboardData.value.weeklyTrainingTarget || 5
  return Math.min(Math.round((count / target) * 100), 100)
})

const calorieProgress = computed(() => {
  const intake = Number(dashboardData.value.todayCaloriesIntake) || 0
  const target = userStore.userInfo?.dailyCalorieTarget || 2000
  return Math.min(Math.round((intake / target) * 100), 100)
})

const proteinProgress = computed(() => {
  const intake = Number(dashboardData.value.todayProtein) || 0
  const target = userStore.userInfo?.dailyProteinTarget || 150
  return Math.min(Math.round((intake / target) * 100), 100)
})

function formatNumber(num) {
  if (!num) return '0'
  return Math.round(Number(num))
}

function formatDate(date) {
  return dayjs(date).format('MM月DD日')
}

function initChart() {
  if (!nutritionChart.value || !dashboardData.value.weeklyNutritionTrend?.length) return

  const chart = echarts.init(nutritionChart.value)
  const data = dashboardData.value.weeklyNutritionTrend

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E2E8F0',
      textStyle: { color: '#0F172A' }
    },
    grid: { top: 30, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: data.map(d => dayjs(d.date).format('MM/DD')),
      axisLine: { lineStyle: { color: '#E2E8F0' } },
      axisLabel: { color: '#64748B' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#F1F5F9' } },
      axisLabel: { color: '#64748B' }
    },
    series: [{
      name: '卡路里',
      type: 'line',
      smooth: true,
      data: data.map(d => Math.round(Number(d.calories) || 0)),
      itemStyle: { color: '#0F766E' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(15, 118, 110, 0.3)' },
          { offset: 1, color: 'rgba(15, 118, 110, 0.05)' }
        ])
      }
    }]
  })

  window.addEventListener('resize', () => chart.resize())
}

async function loadDashboard() {
  try {
    const res = await dashboardApi.getDashboard()
    if (res.code === 200) {
      dashboardData.value = res.data
      await nextTick()
      initChart()
    }
  } catch (e) {
    console.error('Failed to load dashboard', e)
  }
}

async function addWater(amount) {
  try {
    const res = await waterApi.add(amount)
    if (res.code === 200) {
      ElMessage.success(`+${amount}ml`)
      loadDashboard()
    }
  } catch (e) { /* handled */ }
}

onMounted(() => {
  loadDashboard()
})
</script>

<style lang="scss" scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;

  .welcome-text {
    h2 {
      font-size: 24px;
      font-weight: 700;
      color: var(--text-primary);
    }

    p {
      color: var(--text-secondary);
      margin-top: 4px;
    }
  }

  .quick-actions {
    display: flex;
    gap: 12px;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow);
  transition: var(--transition);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .stat-info {
    display: flex;
    flex-direction: column;

    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: var(--text-primary);
    }

    .stat-label {
      font-size: 13px;
      color: var(--text-muted);
    }
  }
}

.training-card .stat-icon { background: rgba(15, 118, 110, 0.1); color: var(--primary); }
.calorie-card .stat-icon { background: rgba(249, 115, 22, 0.1); color: var(--accent); }
.streak-card .stat-icon { background: rgba(234, 179, 8, 0.1); color: var(--warning); }
.water-card .stat-icon { background: rgba(59, 130, 246, 0.1); color: var(--info); }
.water-card { flex-wrap: wrap; }
.water-actions {
  display: flex; gap: 6px; margin-left: auto;
  .el-button { --el-button-bg-color: rgba(59,130,246,0.1); --el-button-text-color: #3B82F6; --el-button-border-color: transparent; --el-button-hover-bg-color: rgba(59,130,246,0.2); --el-button-hover-text-color: #3B82F6; --el-button-hover-border-color: transparent; }
}

.progress-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.progress-bar-container {
  .progress-text {
    display: block;
    text-align: center;
    margin-top: 8px;
    font-size: 14px;
    color: var(--text-secondary);
  }
}

.nutrition-bars {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .nutrition-item {
    .nutrition-label {
      display: block;
      font-size: 13px;
      color: var(--text-secondary);
      margin-bottom: 4px;
    }

    .nutrition-value {
      display: block;
      font-size: 12px;
      color: var(--text-muted);
      margin-top: 4px;
    }
  }
}

.recent-training {
  margin-bottom: 24px;
}

.training-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.training-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    background: var(--border);
  }

  .training-info {
    display: flex;
    flex-direction: column;

    .training-name {
      font-weight: 600;
      color: var(--text-primary);
    }

    .training-date {
      font-size: 13px;
      color: var(--text-muted);
    }
  }

  .training-meta {
    display: flex;
    align-items: center;
    gap: 12px;

    .training-duration {
      font-size: 13px;
      color: var(--text-secondary);
    }
  }
}

.chart-section {
  margin-bottom: 24px;
}

.chart-container {
  height: 300px;
}
</style>
