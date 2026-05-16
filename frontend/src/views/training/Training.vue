<template>
  <div class="training-page animate-fade-in">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="filter-group">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="default"
          @change="loadTrainings"
        />
        <el-select v-model="trainingType" placeholder="训练类型" clearable size="default" @change="loadTrainings">
          <el-option label="力量训练" value="力量训练" />
          <el-option label="有氧训练" value="有氧训练" />
          <el-option label="HIIT" value="HIIT" />
          <el-option label="拉伸" value="拉伸" />
        </el-select>
      </div>
      <el-button type="primary" @click="$router.push('/training/create')">
        <el-icon><Plus /></el-icon>新建训练
      </el-button>
    </div>

    <!-- 训练日历 -->
    <div class="card calendar-section">
      <div class="calendar-header">
        <el-button text @click="prevMonth"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="calendar-title">{{ currentMonth }}</span>
        <el-button text @click="nextMonth"><el-icon><ArrowRight /></el-icon></el-button>
      </div>
      <div class="calendar-grid">
        <div class="calendar-weekday" v-for="day in weekdays" :key="day">{{ day }}</div>
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          class="calendar-day"
          :class="{
            'has-training': day.hasTraining,
            'is-today': day.isToday,
            'other-month': !day.currentMonth
          }"
        >
          <span class="day-number">{{ day.date }}</span>
          <div class="training-dot" v-if="day.hasTraining"></div>
        </div>
      </div>
    </div>

    <!-- 训练统计 -->
    <div class="stats-row">
      <div class="card stat-item">
        <span class="stat-value">{{ stats.totalTrainings || 0 }}</span>
        <span class="stat-label">总训练次数</span>
      </div>
      <div class="card stat-item">
        <span class="stat-value">{{ formatDuration(stats.totalDuration) }}</span>
        <span class="stat-label">总训练时长</span>
      </div>
      <div class="card stat-item">
        <span class="stat-value">{{ stats.thisWeekTrainings || 0 }}</span>
        <span class="stat-label">本周训练</span>
      </div>
      <div class="card stat-item">
        <span class="stat-value">{{ stats.thisMonthTrainings || 0 }}</span>
        <span class="stat-label">本月训练</span>
      </div>
    </div>

    <!-- 训练列表 -->
    <div class="card training-list">
      <h3 class="section-title">训练记录</h3>
      <div v-if="trainings.length" class="list-container">
        <div
          v-for="item in trainings"
          :key="item.id"
          class="training-item"
          @click="$router.push(`/training/${item.id}`)"
        >
          <div class="training-left">
            <div class="training-icon" :class="getTypeClass(item.trainingType)">
              <el-icon :size="20"><component :is="getTypeIcon(item.trainingType)" /></el-icon>
            </div>
            <div class="training-info">
              <span class="training-name">{{ item.name || '训练' }}</span>
              <span class="training-date">{{ formatDate(item.trainingDate) }}</span>
            </div>
          </div>
          <div class="training-right">
            <div class="training-stats">
              <span class="duration">{{ item.duration || 0 }}分钟</span>
              <span class="sets">{{ item.totalSets || 0 }}组</span>
            </div>
            <div class="training-score">
              <el-rate v-model="item.feelingScore" disabled :max="5" />
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无训练记录" />

      <div class="pagination-container" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadTrainings"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { trainingApi } from '@/api'
import dayjs from 'dayjs'

const trainings = ref([])
const stats = ref({})
const calendarData = ref([])
const dateRange = ref(null)
const trainingType = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const calendarMonth = ref(dayjs())

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const currentMonth = computed(() => {
  return calendarMonth.value.format('YYYY年MM月')
})

const calendarDays = computed(() => {
  const start = calendarMonth.value.startOf('month').startOf('week')
  const end = calendarMonth.value.endOf('month').endOf('week')
  const days = []
  let current = start

  while (current.isBefore(end) || current.isSame(end, 'day')) {
    const dateStr = current.format('YYYY-MM-DD')
    days.push({
      date: current.date(),
      fullDate: dateStr,
      isToday: current.isSame(dayjs(), 'day'),
      currentMonth: current.month() === calendarMonth.value.month(),
      hasTraining: calendarData.value.some(d => d.date === dateStr)
    })
    current = current.add(1, 'day')
  }

  return days
})

function formatDuration(minutes) {
  if (!minutes) return '0小时'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours === 0) return `${mins}分钟`
  if (mins === 0) return `${hours}小时`
  return `${hours}小时${mins}分钟`
}

function formatDate(date) {
  return dayjs(date).format('MM月DD日')
}

function getTypeClass(type) {
  const map = {
    '力量训练': 'strength',
    '有氧训练': 'cardio',
    'HIIT': 'hiit',
    '拉伸': 'stretch'
  }
  return map[type] || 'default'
}

function getTypeIcon(type) {
  const map = {
    '力量训练': 'Trophy',
    '有氧训练': 'Bicycle',
    'HIIT': 'Lightning',
    '拉伸': 'Rank'
  }
  return map[type] || 'Trophy'
}

function prevMonth() {
  calendarMonth.value = calendarMonth.value.subtract(1, 'month')
  loadCalendar()
}

function nextMonth() {
  calendarMonth.value = calendarMonth.value.add(1, 'month')
  loadCalendar()
}

async function loadTrainings() {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (dateRange.value) {
      params.startDate = dayjs(dateRange.value[0]).format('YYYY-MM-DD')
      params.endDate = dayjs(dateRange.value[1]).format('YYYY-MM-DD')
    }
    if (trainingType.value) {
      params.trainingType = trainingType.value
    }

    const res = await trainingApi.getList(params)
    if (res.code === 200) {
      trainings.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('Failed to load trainings', e)
  }
}

async function loadStats() {
  try {
    const res = await trainingApi.getStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (e) {
    console.error('Failed to load stats', e)
  }
}

async function loadCalendar() {
  try {
    const year = calendarMonth.value.year()
    const month = calendarMonth.value.month() + 1
    const res = await trainingApi.getCalendar(year, month)
    if (res.code === 200) {
      calendarData.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to load calendar', e)
  }
}

onMounted(() => {
  loadTrainings()
  loadStats()
  loadCalendar()
})
</script>

<style lang="scss" scoped>
.training-page {
  max-width: 1200px;
  margin: 0 auto;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;

  .filter-group {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
}

.calendar-section {
  margin-bottom: 24px;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;

  .calendar-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    min-width: 100px;
    text-align: center;
  }
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-weekday {
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  padding: 8px;
}

.calendar-day {
  text-align: center;
  padding: 8px 4px;
  border-radius: var(--radius-sm);
  position: relative;
  transition: var(--transition);

  .day-number {
    font-size: 14px;
    color: var(--text-primary);
  }

  &.other-month {
    .day-number {
      color: var(--text-muted);
    }
  }

  &.is-today {
    background: var(--primary);
    .day-number {
      color: white;
      font-weight: 600;
    }
  }

  &.has-training {
    .day-number {
      font-weight: 600;
    }
  }

  .training-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--accent);
    margin: 4px auto 0;
  }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-item {
  text-align: center;
  padding: 20px;

  .stat-value {
    display: block;
    font-size: 24px;
    font-weight: 700;
    color: var(--primary);
  }

  .stat-label {
    display: block;
    font-size: 13px;
    color: var(--text-muted);
    margin-top: 4px;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.training-list {
  margin-bottom: 24px;
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.training-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    background: var(--border);
    transform: translateX(4px);
  }
}

.training-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.training-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;

  &.strength { background: rgba(15, 118, 110, 0.1); color: var(--primary); }
  &.cardio { background: rgba(59, 130, 246, 0.1); color: var(--info); }
  &.hiit { background: rgba(249, 115, 22, 0.1); color: var(--accent); }
  &.stretch { background: rgba(139, 92, 246, 0.1); color: #8B5CF6; }
  &.default { background: rgba(100, 116, 139, 0.1); color: var(--text-secondary); }
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

.training-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.training-stats {
  display: flex;
  gap: 12px;

  .duration, .sets {
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
