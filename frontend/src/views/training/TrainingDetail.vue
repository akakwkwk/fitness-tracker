<template>
  <div class="training-detail animate-fade-in">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <h2>训练详情</h2>
      <div class="header-actions">
        <el-button type="danger" text @click="handleDelete">
          <el-icon><Delete /></el-icon>删除
        </el-button>
      </div>
    </div>

    <div v-if="training" class="detail-content">
      <!-- 基本信息 -->
      <div class="card info-card">
        <div class="info-header">
          <h3>{{ training.name || '训练' }}</h3>
          <el-tag>{{ training.trainingType || '综合' }}</el-tag>
        </div>
        <div class="info-meta">
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(training.trainingDate) }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Timer /></el-icon>
            <span>{{ training.duration || 0 }}分钟</span>
          </div>
          <div class="meta-item">
            <el-icon><Trophy /></el-icon>
            <span>{{ training.totalSets || 0 }}组</span>
          </div>
        </div>
        <div class="info-score" v-if="training.feelingScore">
          <span class="score-label">感受评分：</span>
          <el-rate v-model="training.feelingScore" disabled :max="5" />
        </div>
        <div class="info-note" v-if="training.note">
          <p>{{ training.note }}</p>
        </div>
      </div>

      <!-- 训练动作 -->
      <div class="card">
        <h3 class="section-title">训练动作</h3>
        <div v-if="training.exercises?.length" class="exercises-list">
          <div v-for="exercise in training.exercises" :key="exercise.id" class="exercise-item">
            <div class="exercise-header">
              <span class="exercise-name">{{ exercise.exerciseName }}</span>
            </div>
            <div class="sets-table" v-if="exercise.sets?.length">
              <div class="sets-header">
                <span>组</span>
                <span>重量</span>
                <span>次数</span>
              </div>
              <div v-for="set in exercise.sets" :key="set.id" class="set-row">
                <span>{{ set.setNumber }}</span>
                <span>{{ set.weight }}kg</span>
                <span>{{ set.reps }}次</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 统计汇总 -->
      <div class="card stats-card">
        <h3 class="section-title">训练统计</h3>
        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-value">{{ training.totalSets || 0 }}</span>
            <span class="stat-label">总组数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatVolume(training.totalVolume) }}</span>
            <span class="stat-label">总容量(kg)</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ training.caloriesBurned || 0 }}</span>
            <span class="stat-label">消耗卡路里</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { trainingApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const training = ref(null)

function formatDate(date) {
  return dayjs(date).format('YYYY年MM月DD日')
}

function formatVolume(volume) {
  if (!volume) return '0'
  return Math.round(Number(volume))
}

async function loadTraining() {
  try {
    const res = await trainingApi.getDetail(route.params.id)
    if (res.code === 200) {
      training.value = res.data
    }
  } catch (e) {
    console.error('Failed to load training', e)
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这条训练记录吗？', '确认删除', {
      type: 'warning'
    })
    const res = await trainingApi.delete(route.params.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      router.push('/training')
    }
  } catch (e) {
    // cancelled
  }
}

onMounted(() => {
  loadTraining()
})
</script>

<style lang="scss" scoped>
.training-detail {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  h2 {
    font-size: 20px;
    font-weight: 600;
  }
}

.info-card {
  margin-bottom: 20px;
}

.info-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  h3 {
    font-size: 20px;
    font-weight: 600;
  }
}

.info-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  flex-wrap: wrap;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--text-secondary);
  }
}

.info-score {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  .score-label {
    color: var(--text-secondary);
  }
}

.info-note {
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.exercises-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.exercise-item {
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  padding: 16px;
}

.exercise-header {
  margin-bottom: 12px;

  .exercise-name {
    font-weight: 600;
    font-size: 15px;
  }
}

.sets-table {
  .sets-header {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    padding: 8px 0;
    font-size: 13px;
    color: var(--text-muted);
    font-weight: 500;
  }

  .set-row {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    padding: 6px 0;
    font-size: 14px;
    color: var(--text-primary);
  }
}

.stats-card {
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);

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

.loading-container {
  padding: 40px;
}
</style>
