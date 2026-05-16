<template>
  <div class="stats-page animate-fade-in">
    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" class="stats-tabs">
      <el-tab-pane label="训练统计" name="training">
        <div class="stats-content">
          <!-- 训练概览 -->
          <div class="stats-grid">
            <div class="card stat-card">
              <span class="stat-value">{{ trainingStats.totalTrainings || 0 }}</span>
              <span class="stat-label">总训练次数</span>
            </div>
            <div class="card stat-card">
              <span class="stat-value">{{ formatDuration(trainingStats.totalDuration) }}</span>
              <span class="stat-label">总训练时长</span>
            </div>
            <div class="card stat-card">
              <span class="stat-value">{{ formatVolume(trainingStats.totalVolume) }}</span>
              <span class="stat-label">总训练容量(kg)</span>
            </div>
            <div class="card stat-card">
              <span class="stat-value">{{ trainingStats.thisWeekTrainings || 0 }}</span>
              <span class="stat-label">本周训练</span>
            </div>
          </div>

          <!-- 训练类型分布 -->
          <div class="card chart-card">
            <h3 class="section-title">训练类型分布</h3>
            <div ref="typeChart" class="chart-container"></div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="营养统计" name="nutrition">
        <div class="stats-content">
          <!-- 营养概览 -->
          <div class="nutrition-overview">
            <div class="card">
              <h3 class="section-title">今日营养摄入</h3>
              <div class="nutrition-bars">
                <div class="nutrition-item">
                  <span class="label">卡路里</span>
                  <el-progress :percentage="calorieProgress" :stroke-width="12" color="#F97316" />
                  <span class="value">{{ Math.round(Number(nutritionStats.todayCalories) || 0) }} / {{ nutritionStats.calorieTarget || 2000 }} kcal</span>
                </div>
                <div class="nutrition-item">
                  <span class="label">蛋白质</span>
                  <el-progress :percentage="proteinProgress" :stroke-width="12" color="#3B82F6" />
                  <span class="value">{{ Math.round(Number(nutritionStats.todayProtein) || 0) }} / {{ nutritionStats.proteinTarget || 150 }} g</span>
                </div>
                <div class="nutrition-item">
                  <span class="label">碳水</span>
                  <el-progress :percentage="carbsProgress" :stroke-width="12" color="#22C55E" />
                  <span class="value">{{ Math.round(Number(nutritionStats.todayCarbs) || 0) }} / {{ nutritionStats.carbTarget || 250 }} g</span>
                </div>
                <div class="nutrition-item">
                  <span class="label">脂肪</span>
                  <el-progress :percentage="fatProgress" :stroke-width="12" color="#EAB308" />
                  <span class="value">{{ Math.round(Number(nutritionStats.todayFat) || 0) }} / {{ nutritionStats.fatTarget || 65 }} g</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 本周营养趋势 -->
          <div class="card chart-card">
            <h3 class="section-title">本周营养趋势</h3>
            <div ref="nutritionChart" class="chart-container"></div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="睡眠统计" name="sleep">
        <div class="stats-content">
          <!-- 睡眠概览 -->
          <div class="stats-grid">
            <div class="card stat-card">
              <span class="stat-value">{{ sleepRecords.length }}</span>
              <span class="stat-label">记录天数</span>
            </div>
            <div class="card stat-card">
              <span class="stat-value">{{ formatSleepDuration(avgSleepDuration) }}</span>
              <span class="stat-label">平均睡眠</span>
            </div>
            <div class="card stat-card">
              <span class="stat-value">{{ avgSleepQuality.toFixed(1) }}</span>
              <span class="stat-label">平均质量(1-5)</span>
            </div>
            <div class="card stat-card">
              <span class="stat-value">{{ formatSleepDuration(maxSleepDuration) }}</span>
              <span class="stat-label">最长睡眠</span>
            </div>
          </div>

          <!-- 睡眠时长趋势 -->
          <div class="card chart-card">
            <h3 class="section-title">睡眠时长趋势</h3>
            <div ref="sleepDurationChart" class="chart-container"></div>
          </div>

          <!-- 睡眠质量趋势 -->
          <div class="card chart-card">
            <h3 class="section-title">睡眠质量趋势</h3>
            <div ref="sleepQualityChart" class="chart-container"></div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="身体数据" name="body">
        <div class="stats-content">
          <!-- 身体数据概览 -->
          <div class="card" v-if="latestBody">
            <h3 class="section-title">最新身体数据</h3>
            <div class="body-stats-grid">
              <div class="body-stat" v-if="latestBody.weight">
                <span class="value">{{ latestBody.weight }}kg</span>
                <span class="label">体重</span>
              </div>
              <div class="body-stat" v-if="latestBody.bmi">
                <span class="value">{{ latestBody.bmi }}</span>
                <span class="label">BMI</span>
              </div>
              <div class="body-stat" v-if="latestBody.bodyFatRate">
                <span class="value">{{ latestBody.bodyFatRate }}%</span>
                <span class="label">体脂率</span>
              </div>
              <div class="body-stat" v-if="latestBody.muscleMass">
                <span class="value">{{ latestBody.muscleMass }}kg</span>
                <span class="label">肌肉量</span>
              </div>
            </div>
          </div>

          <!-- 体重趋势图 -->
          <div class="card chart-card">
            <h3 class="section-title">体重趋势</h3>
            <div ref="weightChart" class="chart-container"></div>
          </div>

          <!-- 添加身体数据 -->
          <div class="card">
            <h3 class="section-title">记录身体数据</h3>
            <el-form :model="bodyForm" label-width="80px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="体重(kg)">
                    <el-input-number v-model="bodyForm.weight" :min="0" :max="300" :step="0.1" :precision="1" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="体脂率(%)">
                    <el-input-number v-model="bodyForm.bodyFatRate" :min="0" :max="60" :step="0.1" :precision="1" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="胸围(cm)">
                    <el-input-number v-model="bodyForm.chest" :min="0" :max="200" :step="0.5" :precision="1" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="腰围(cm)">
                    <el-input-number v-model="bodyForm.waist" :min="0" :max="200" :step="0.5" :precision="1" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item>
                <el-button type="primary" @click="saveBodyData" :loading="savingBody">保存</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { trainingApi, dietApi, bodyApi, sleepApi } from '@/api'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'

const activeTab = ref('training')
const trainingStats = ref({})
const nutritionStats = ref({})
const bodyData = ref([])
const latestBody = ref(null)
const savingBody = ref(false)

const sleepRecords = ref([])
const typeChart = ref(null)
const nutritionChart = ref(null)
const weightChart = ref(null)
const sleepDurationChart = ref(null)
const sleepQualityChart = ref(null)

const bodyForm = reactive({
  recordDate: dayjs().format('YYYY-MM-DD'),
  weight: null,
  bodyFatRate: null,
  chest: null,
  waist: null
})

const calorieProgress = computed(() => {
  const intake = Number(nutritionStats.value.todayCalories) || 0
  const target = nutritionStats.value.calorieTarget || 2000
  return Math.min(Math.round((intake / target) * 100), 100)
})

const proteinProgress = computed(() => {
  const intake = Number(nutritionStats.value.todayProtein) || 0
  const target = nutritionStats.value.proteinTarget || 150
  return Math.min(Math.round((intake / target) * 100), 100)
})

const carbsProgress = computed(() => {
  const intake = Number(nutritionStats.value.todayCarbs) || 0
  const target = nutritionStats.value.carbTarget || 250
  return Math.min(Math.round((intake / target) * 100), 100)
})

const fatProgress = computed(() => {
  const intake = Number(nutritionStats.value.todayFat) || 0
  const target = nutritionStats.value.fatTarget || 65
  return Math.min(Math.round((intake / target) * 100), 100)
})

const avgSleepDuration = computed(() => {
  if (!sleepRecords.value.length) return 0
  return Math.round(sleepRecords.value.reduce((sum, r) => sum + (r.duration || 0), 0) / sleepRecords.value.length)
})

const avgSleepQuality = computed(() => {
  if (!sleepRecords.value.length) return 0
  return sleepRecords.value.reduce((sum, r) => sum + (r.quality || 0), 0) / sleepRecords.value.length
})

const maxSleepDuration = computed(() => {
  if (!sleepRecords.value.length) return 0
  return Math.max(...sleepRecords.value.map(r => r.duration || 0))
})

function formatDuration(minutes) {
  if (!minutes) return '0小时'
  const hours = Math.floor(minutes / 60)
  return hours > 0 ? `${hours}小时${minutes % 60}分钟` : `${minutes}分钟`
}

function formatVolume(volume) {
  if (!volume) return '0'
  return Math.round(Number(volume))
}

function formatSleepDuration(minutes) {
  if (!minutes) return '0h'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h}h${m}m` : `${h}h`
}

function initTypeChart() {
  if (!typeChart.value || !trainingStats.value.trainingTypeDistribution?.length) return
  const chart = echarts.init(typeChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: trainingStats.value.trainingTypeDistribution.map(item => ({
        name: item.name,
        value: item.value
      })),
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}: {c}次' }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function initNutritionChart() {
  if (!nutritionChart.value || !nutritionStats.value.weeklyNutritionTrend?.length) return
  const chart = echarts.init(nutritionChart.value)
  const data = nutritionStats.value.weeklyNutritionTrend

  const splits = 5
  const macroMax = Math.max(...data.map(d => (Number(d.protein) || 0) + (Number(d.carbs) || 0) + (Number(d.fat) || 0)), 1)
  const calMax = Math.max(...data.map(d => Number(d.calories) || 0), 1)

  function ceilNice(v) {
    const mag = Math.pow(10, Math.floor(Math.log10(v)))
    return Math.ceil(v / mag) * mag
  }

  const macroTop = ceilNice(macroMax)
  const calTop = ceilNice(calMax)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['蛋白质', '碳水', '脂肪', '卡路里'] },
    grid: { top: 50, right: 60, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: data.map(d => dayjs(d.date).format('MM/DD'))
    },
    yAxis: [
      { type: 'value', name: '克(g)', position: 'left', min: 0, max: macroTop, interval: macroTop / splits, nameTextStyle: { padding: [0, 40, 0, 0] } },
      { type: 'value', name: '千卡', position: 'right', min: 0, max: calTop, interval: calTop / splits, nameTextStyle: { padding: [0, 0, 0, 40] } }
    ],
    series: [
      { name: '蛋白质', type: 'bar', stack: 'macro', data: data.map(d => Math.round(Number(d.protein) || 0)), itemStyle: { color: '#3B82F6' }, barMaxWidth: 28 },
      { name: '碳水', type: 'bar', stack: 'macro', data: data.map(d => Math.round(Number(d.carbs) || 0)), itemStyle: { color: '#22C55E' }, barMaxWidth: 28 },
      { name: '脂肪', type: 'bar', stack: 'macro', data: data.map(d => Math.round(Number(d.fat) || 0)), itemStyle: { color: '#EAB308', borderRadius: [4, 4, 0, 0] }, barMaxWidth: 28 },
      { name: '卡路里', type: 'line', yAxisIndex: 1, smooth: true, data: data.map(d => Math.round(Number(d.calories) || 0)), itemStyle: { color: '#F97316' }, lineStyle: { width: 2.5 }, symbol: 'circle', symbolSize: 6 }
    ]
  })
  window.addEventListener('resize', () => chart.resize())
}

function initWeightChart() {
  if (!weightChart.value || !bodyData.value.length) return
  const chart = echarts.init(weightChart.value)
  const sorted = [...bodyData.value].sort((a, b) => a.recordDate.localeCompare(b.recordDate))

  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 35, right: 20, bottom: 30, left: 60 },
    xAxis: {
      type: 'category',
      data: sorted.map(d => dayjs(d.recordDate).format('MM/DD'))
    },
    yAxis: { type: 'value', name: '体重(kg)', nameTextStyle: { padding: [0, 40, 0, 0] } },
    series: [{
      type: 'line',
      smooth: true,
      data: sorted.map(d => Number(d.weight)),
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

function initSleepDurationChart() {
  if (!sleepDurationChart.value || !sleepRecords.value.length) return
  const chart = echarts.init(sleepDurationChart.value)
  const sorted = [...sleepRecords.value].sort((a, b) => a.recordDate.localeCompare(b.recordDate))

  chart.setOption({
    tooltip: { trigger: 'axis', formatter: p => `${p[0].axisValue}<br/>${p[0].seriesName}: ${formatSleepDuration(p[0].value)}` },
    grid: { top: 35, right: 20, bottom: 30, left: 60 },
    xAxis: { type: 'category', data: sorted.map(d => dayjs(d.recordDate).format('MM/DD')) },
    yAxis: { type: 'value', name: '分钟', min: 0, max: 960, nameTextStyle: { padding: [0, 40, 0, 0] } },
    series: [{
      name: '睡眠时长',
      type: 'bar',
      data: sorted.map(d => d.duration || 0),
      itemStyle: { color: '#6366F1', borderRadius: [4, 4, 0, 0] },
      barMaxWidth: 32
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function initSleepQualityChart() {
  if (!sleepQualityChart.value || !sleepRecords.value.length) return
  const chart = echarts.init(sleepQualityChart.value)
  const sorted = [...sleepRecords.value].sort((a, b) => a.recordDate.localeCompare(b.recordDate))

  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 35, right: 20, bottom: 30, left: 60 },
    xAxis: { type: 'category', data: sorted.map(d => dayjs(d.recordDate).format('MM/DD')) },
    yAxis: { type: 'value', name: '质量', min: 0, max: 5, nameTextStyle: { padding: [0, 40, 0, 0] } },
    series: [{
      name: '睡眠质量',
      type: 'line',
      smooth: true,
      data: sorted.map(d => d.quality || 0),
      itemStyle: { color: '#F59E0B' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(245, 158, 11, 0.3)' },
          { offset: 1, color: 'rgba(245, 158, 11, 0.05)' }
        ])
      }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

async function loadSleepStats() {
  try {
    const res = await sleepApi.getList()
    if (res.code === 200) {
      sleepRecords.value = res.data || []
      await nextTick()
      initSleepDurationChart()
      initSleepQualityChart()
    }
  } catch (e) {
    console.error(e)
  }
}

async function loadTrainingStats() {
  try {
    const res = await trainingApi.getStats()
    if (res.code === 200) {
      trainingStats.value = res.data
      await nextTick()
      initTypeChart()
    }
  } catch (e) {
    console.error(e)
  }
}

async function loadNutritionStats() {
  try {
    const res = await dietApi.getStats()
    if (res.code === 200) {
      nutritionStats.value = res.data
      await nextTick()
      initNutritionChart()
    }
  } catch (e) {
    console.error(e)
  }
}

async function loadBodyData() {
  try {
    const [listRes, latestRes] = await Promise.all([
      bodyApi.getList({}),
      bodyApi.getLatest()
    ])
    if (listRes.code === 200) {
      bodyData.value = listRes.data || []
      await nextTick()
      initWeightChart()
    }
    if (latestRes.code === 200) {
      latestBody.value = latestRes.data
    }
  } catch (e) {
    console.error(e)
  }
}

async function saveBodyData() {
  savingBody.value = true
  try {
    const res = await bodyApi.add(bodyForm)
    if (res.code === 200) {
      ElMessage.success('身体数据已保存')
      loadBodyData()
    }
  } catch (e) {
    // error handled
  } finally {
    savingBody.value = false
  }
}

watch(activeTab, (tab) => {
  if (tab === 'training') loadTrainingStats()
  if (tab === 'nutrition') loadNutritionStats()
  if (tab === 'sleep') loadSleepStats()
  if (tab === 'body') loadBodyData()
})

onMounted(() => {
  loadTrainingStats()
})
</script>

<style lang="scss" scoped>
.stats-page {
  max-width: 1200px;
  margin: 0 auto;
}

.stats-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  text-align: center;
  padding: 24px;

  .stat-value {
    display: block;
    font-size: 28px;
    font-weight: 700;
    color: var(--primary);
  }

  .stat-label {
    display: block;
    font-size: 13px;
    color: var(--text-muted);
    margin-top: 8px;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.nutrition-bars {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.nutrition-item {
  .label {
    display: block;
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 8px;
  }

  .value {
    display: block;
    font-size: 13px;
    color: var(--text-muted);
    margin-top: 8px;
  }
}

.body-stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.body-stat {
  text-align: center;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);

  .value {
    display: block;
    font-size: 24px;
    font-weight: 700;
    color: var(--primary);
  }

  .label {
    display: block;
    font-size: 13px;
    color: var(--text-muted);
    margin-top: 4px;
  }
}
</style>
