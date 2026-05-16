<template>
  <div class="training-create animate-fade-in">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <h2>创建训练</h2>
      <el-button type="primary" @click="saveTraining" :loading="saving">
        <el-icon><Check /></el-icon>保存
      </el-button>
    </div>

    <!-- 基本信息 -->
    <div class="card">
      <h3 class="section-title">基本信息</h3>
      <el-form :model="form" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="训练名称">
              <el-input v-model="form.name" placeholder="例：胸部训练日" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="训练类型">
              <el-select v-model="form.trainingType" placeholder="选择类型">
                <el-option label="力量训练" value="力量训练" />
                <el-option label="有氧训练" value="有氧训练" />
                <el-option label="HIIT" value="HIIT" />
                <el-option label="拉伸" value="拉伸" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="训练日期">
              <el-date-picker v-model="form.trainingDate" type="date" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="训练时长">
              <el-input-number v-model="form.duration" :min="0" :max="999" :step="5" />
              <span class="unit">分钟</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="感受评分">
          <el-rate v-model="form.feelingScore" :max="5" />
        </el-form-item>
        <el-form-item label="训练笔记">
          <el-input v-model="form.note" type="textarea" :rows="2" placeholder="记录今天的训练感受..." />
        </el-form-item>
      </el-form>
    </div>

    <!-- 训练动作 -->
    <div class="card">
      <div class="section-header">
        <h3 class="section-title">训练动作</h3>
        <el-button type="primary" size="small" @click="showExerciseDialog = true">
          <el-icon><Plus /></el-icon>添加动作
        </el-button>
      </div>

      <div v-if="form.exercises.length" class="exercises-list">
        <div v-for="(exercise, exIndex) in form.exercises" :key="exIndex" class="exercise-item">
          <div class="exercise-header">
            <div class="exercise-info">
              <span class="exercise-name">{{ exercise.exerciseName }}</span>
              <el-button text type="danger" size="small" @click="removeExercise(exIndex)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>

          <!-- 组数表格 -->
          <div class="sets-table">
            <div class="sets-header">
              <span class="set-num">组</span>
              <span class="set-weight">重量(kg)</span>
              <span class="set-reps">次数</span>
              <span class="set-action">操作</span>
            </div>
            <div v-for="(set, setIndex) in exercise.sets" :key="setIndex" class="set-row">
              <span class="set-num">{{ setIndex + 1 }}</span>
              <el-input-number v-model="set.weight" :min="0" :step="2.5" size="small" controls-position="right" />
              <el-input-number v-model="set.reps" :min="0" :step="1" size="small" controls-position="right" />
              <el-button text type="danger" size="small" @click="removeSet(exIndex, setIndex)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button text type="primary" size="small" @click="addSet(exIndex)" class="add-set-btn">
              <el-icon><Plus /></el-icon>添加组
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="点击上方按钮添加训练动作" :image-size="60" />
    </div>

    <!-- 选择动作弹窗 -->
    <el-dialog v-model="showExerciseDialog" title="选择动作" width="600px">
      <div class="exercise-search">
        <el-input v-model="exerciseSearch" placeholder="搜索动作..." :prefix-icon="Search" />
      </div>
      <div class="exercise-categories">
        <el-tag
          v-for="cat in categories"
          :key="cat"
          :type="selectedCategory === cat ? '' : 'info'"
          @click="selectedCategory = selectedCategory === cat ? '' : cat"
          class="category-tag"
        >
          {{ cat }}
        </el-tag>
      </div>
      <div class="exercise-grid">
        <div
          v-for="exercise in filteredExercises"
          :key="exercise.id"
          class="exercise-option"
          @click="selectExercise(exercise)"
        >
          <span class="exercise-name">{{ exercise.name }}</span>
          <span class="exercise-muscle">{{ exercise.muscleGroup }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { trainingApi } from '@/api'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const saving = ref(false)
const showExerciseDialog = ref(false)
const exerciseSearch = ref('')
const selectedCategory = ref('')
const exercises = ref([])

const form = reactive({
  name: '',
  trainingType: '力量训练',
  trainingDate: dayjs().format('YYYY-MM-DD'),
  duration: 60,
  feelingScore: 3,
  note: '',
  exercises: []
})

const categories = computed(() => {
  const cats = [...new Set(exercises.value.map(e => e.category))]
  return cats.sort()
})

const filteredExercises = computed(() => {
  let list = exercises.value
  if (selectedCategory.value) {
    list = list.filter(e => e.category === selectedCategory.value)
  }
  if (exerciseSearch.value) {
    const search = exerciseSearch.value.toLowerCase()
    list = list.filter(e => e.name.toLowerCase().includes(search))
  }
  return list
})

function selectExercise(exercise) {
  form.exercises.push({
    exerciseLibraryId: exercise.id,
    exerciseName: exercise.name,
    sortOrder: form.exercises.length,
    sets: [
      { setNumber: 1, weight: 0, reps: 10, setType: 'normal', isCompleted: 1 },
      { setNumber: 2, weight: 0, reps: 10, setType: 'normal', isCompleted: 1 },
      { setNumber: 3, weight: 0, reps: 10, setType: 'normal', isCompleted: 1 }
    ]
  })
  showExerciseDialog.value = false
}

function removeExercise(index) {
  form.exercises.splice(index, 1)
}

function addSet(exIndex) {
  const sets = form.exercises[exIndex].sets
  sets.push({
    setNumber: sets.length + 1,
    weight: sets.length > 0 ? sets[sets.length - 1].weight : 0,
    reps: sets.length > 0 ? sets[sets.length - 1].reps : 10,
    setType: 'normal',
    isCompleted: 1
  })
}

function removeSet(exIndex, setIndex) {
  form.exercises[exIndex].sets.splice(setIndex, 1)
  // 重新编号
  form.exercises[exIndex].sets.forEach((set, i) => {
    set.setNumber = i + 1
  })
}

async function saveTraining() {
  if (!form.name) {
    ElMessage.warning('请输入训练名称')
    return
  }
  if (form.exercises.length === 0) {
    ElMessage.warning('请至少添加一个训练动作')
    return
  }

  saving.value = true
  try {
    const res = await trainingApi.create(form)
    if (res.code === 200) {
      ElMessage.success('训练记录已保存')
      router.push('/training')
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    saving.value = false
  }
}

async function loadExercises() {
  try {
    const res = await trainingApi.getExercises()
    if (res.code === 200) {
      exercises.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to load exercises', e)
  }
}

onMounted(() => {
  loadExercises()
})
</script>

<style lang="scss" scoped>
.training-create {
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

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .section-title {
    margin-bottom: 0;
  }
}

.card {
  margin-bottom: 20px;
}

.unit {
  margin-left: 8px;
  color: var(--text-muted);
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

  .exercise-info {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .exercise-name {
      font-weight: 600;
      font-size: 15px;
      color: var(--text-primary);
    }
  }
}

.sets-table {
  .sets-header {
    display: grid;
    grid-template-columns: 40px 1fr 1fr 50px;
    gap: 8px;
    padding: 8px 0;
    font-size: 13px;
    color: var(--text-muted);
    font-weight: 500;
  }

  .set-row {
    display: grid;
    grid-template-columns: 40px 1fr 1fr 50px;
    gap: 8px;
    align-items: center;
    padding: 4px 0;

    .set-num {
      text-align: center;
      font-weight: 600;
      color: var(--text-secondary);
    }
  }

  .add-set-btn {
    margin-top: 8px;
  }
}

.exercise-search {
  margin-bottom: 16px;
}

.exercise-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;

  .category-tag {
    cursor: pointer;
    transition: var(--transition);
  }
}

.exercise-grid {
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.exercise-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    background: var(--primary);
    color: white;

    .exercise-muscle {
      color: rgba(255, 255, 255, 0.8);
    }
  }

  .exercise-name {
    font-weight: 500;
  }

  .exercise-muscle {
    font-size: 13px;
    color: var(--text-muted);
  }
}
</style>
