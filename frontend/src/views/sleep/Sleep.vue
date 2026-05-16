<template>
  <div class="sleep-page animate-fade-in">
    <!-- 今日睡眠概览 -->
    <div class="sleep-overview">
      <div class="overview-card main-card">
        <div class="sleep-icon">🌙</div>
        <div class="overview-info">
          <span class="overview-label">昨晚睡眠</span>
          <span class="overview-value" v-if="latestRecord">{{ formatDuration(latestRecord.duration) }}</span>
          <span class="overview-value empty" v-else>暂无记录</span>
        </div>
        <div class="quality-stars" v-if="latestRecord">
          <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= latestRecord.quality }">★</span>
        </div>
      </div>
      <div class="overview-card">
        <span class="card-label">本周平均</span>
        <span class="card-value">{{ formatDuration(weekAvgDuration) }}</span>
      </div>
      <div class="overview-card">
        <span class="card-label">本周平均质量</span>
        <span class="card-value">{{ weekAvgQuality.toFixed(1) }} / 5</span>
      </div>
      <div class="overview-card">
        <span class="card-label">记录天数</span>
        <span class="card-value">{{ records.length }} 天</span>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <h2>睡眠记录</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>记录睡眠
      </el-button>
    </div>

    <!-- 睡眠记录列表 -->
    <div class="records-list">
      <div v-for="record in records" :key="record.id" class="record-card card">
        <div class="record-main">
          <div class="record-date">
            <span class="date-text">{{ formatDate(record.recordDate) }}</span>
            <span class="date-weekday">{{ getWeekday(record.recordDate) }}</span>
          </div>
          <div class="record-time">
            <span class="time-range">
              {{ formatTime(record.sleepTime) }} - {{ formatTime(record.wakeTime) }}
            </span>
            <span class="duration">{{ formatDuration(record.duration) }}</span>
          </div>
          <div class="record-quality">
            <span v-for="i in 5" :key="i" class="star small" :class="{ active: i <= record.quality }">★</span>
          </div>
        </div>
        <div class="record-note" v-if="record.note">
          <span>{{ record.note }}</span>
        </div>
      </div>
      <el-empty v-if="!records.length" description="暂无睡眠记录" />
    </div>

    <!-- 添加睡眠记录弹窗 -->
    <el-dialog v-model="showAddDialog" title="记录睡眠" width="480px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px">
        <el-form-item label="日期" required>
          <el-date-picker v-model="form.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="入睡时间" required>
          <el-date-picker v-model="form.sleepTime" type="datetime" placeholder="选择入睡时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="起床时间" required>
          <el-date-picker v-model="form.wakeTime" type="datetime" placeholder="选择起床时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="睡眠质量">
          <div class="quality-select">
            <span
              v-for="i in 5"
              :key="i"
              class="star selectable"
              :class="{ active: i <= form.quality }"
              @click="form.quality = i"
            >★</span>
            <span class="quality-text">{{ qualityLabels[form.quality - 1] }}</span>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" :rows="2" placeholder="可选备注..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addRecord" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { sleepApi } from '@/api'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const records = ref([])
const showAddDialog = ref(false)
const saving = ref(false)

const qualityLabels = ['很差', '较差', '一般', '良好', '优秀']

const form = reactive({
  recordDate: dayjs().format('YYYY-MM-DD'),
  sleepTime: '',
  wakeTime: '',
  quality: 3,
  note: ''
})

const latestRecord = computed(() => records.value[0] || null)

const weekAvgDuration = computed(() => {
  if (!records.value.length) return 0
  const total = records.value.reduce((sum, r) => sum + (r.duration || 0), 0)
  return Math.round(total / records.value.length)
})

const weekAvgQuality = computed(() => {
  if (!records.value.length) return 0
  const total = records.value.reduce((sum, r) => sum + (r.quality || 0), 0)
  return total / records.value.length
})

function formatDuration(minutes) {
  if (!minutes) return '0h'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m > 0 ? `${h}h ${m}min` : `${h}h`
}

function formatDate(date) {
  return dayjs(date).format('MM月DD日')
}

function formatTime(datetime) {
  return dayjs(datetime).format('HH:mm')
}

function getWeekday(date) {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[dayjs(date).day()]
}

async function loadRecords() {
  try {
    const res = await sleepApi.getList()
    if (res.code === 200) {
      records.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to load sleep records', e)
  }
}

async function addRecord() {
  if (!form.recordDate || !form.sleepTime || !form.wakeTime) {
    ElMessage.warning('请填写日期和时间')
    return
  }
  saving.value = true
  try {
    const res = await sleepApi.add({
      recordDate: form.recordDate,
      sleepTime: form.sleepTime,
      wakeTime: form.wakeTime,
      quality: form.quality,
      note: form.note || null
    })
    if (res.code === 200) {
      ElMessage.success('记录已保存')
      showAddDialog.value = false
      resetForm()
      loadRecords()
    }
  } catch (e) {
    // handled
  } finally {
    saving.value = false
  }
}

function resetForm() {
  form.recordDate = dayjs().format('YYYY-MM-DD')
  form.sleepTime = ''
  form.wakeTime = ''
  form.quality = 3
  form.note = ''
}

onMounted(() => loadRecords())
</script>

<style lang="scss" scoped>
.sleep-page { max-width: 900px; margin: 0 auto; }

.sleep-overview {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
  @media (max-width: 768px) { grid-template-columns: 1fr 1fr; }
}

.overview-card {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 20px;
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  gap: 8px;

  &.main-card {
    flex-direction: row;
    align-items: center;
    gap: 16px;
    @media (max-width: 768px) { grid-column: 1 / -1; }
  }

  .sleep-icon { font-size: 40px; }
  .overview-info {
    display: flex;
    flex-direction: column;
    .overview-label { font-size: 13px; color: var(--text-muted); }
    .overview-value { font-size: 28px; font-weight: 700; color: var(--primary);
      &.empty { font-size: 16px; color: var(--text-muted); font-weight: 400; }
    }
  }
  .quality-stars { display: flex; gap: 2px; }

  .card-label { font-size: 13px; color: var(--text-muted); }
  .card-value { font-size: 20px; font-weight: 700; color: var(--text-primary); }
}

.star {
  color: var(--border);
  font-size: 18px;
  &.active { color: #F59E0B; }
  &.small { font-size: 14px; }
  &.selectable { cursor: pointer; font-size: 24px; transition: transform 0.15s;
    &:hover { transform: scale(1.2); }
  }
}

.quality-select {
  display: flex;
  align-items: center;
  gap: 8px;
  .quality-text { font-size: 13px; color: var(--text-muted); margin-left: 8px; }
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  h2 { font-size: 18px; font-weight: 600; }
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-card {
  padding: 16px 20px;
}

.record-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.record-date {
  display: flex;
  flex-direction: column;
  min-width: 70px;
  .date-text { font-weight: 600; font-size: 15px; }
  .date-weekday { font-size: 12px; color: var(--text-muted); }
}

.record-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  .time-range { font-size: 15px; font-weight: 500; color: var(--text-primary); }
  .duration { font-size: 13px; color: var(--primary); font-weight: 600; }
}

.record-quality {
  display: flex;
  gap: 2px;
}

.record-note {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border);
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
