<template>
  <div class="exercises-page animate-fade-in">
    <!-- 搜索和筛选 -->
    <div class="filter-bar">
      <div class="filter-top">
        <el-input v-model="keyword" placeholder="搜索动作..." :prefix-icon="Search" clearable style="max-width:300px" />
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>添加动作
        </el-button>
      </div>
      <div class="category-tags">
        <el-tag
          v-for="cat in categories"
          :key="cat"
          :type="activeCategory === cat ? '' : 'info'"
          @click="activeCategory = activeCategory === cat ? '' : cat"
          class="cat-tag"
        >{{ cat }}</el-tag>
      </div>
    </div>

    <!-- 动作列表 -->
    <div class="exercise-grid">
      <div v-for="ex in filteredExercises" :key="ex.id" class="exercise-card card" @click="showDetail(ex)">
        <div class="ex-header">
          <span class="ex-name">{{ ex.name }}</span>
          <el-tag size="small" :type="difficultyType(ex.difficulty)">{{ difficultyLabel(ex.difficulty) }}</el-tag>
        </div>
        <div class="ex-meta">
          <span class="ex-category">{{ ex.category }}</span>
          <span class="ex-equipment" v-if="ex.equipment">{{ ex.equipment }}</span>
        </div>
        <div class="ex-muscles">
          <span class="muscle primary">{{ ex.muscleGroup }}</span>
          <span class="muscle secondary" v-if="ex.secondaryMuscle">{{ ex.secondaryMuscle }}</span>
        </div>
      </div>
    </div>

    <el-empty v-if="!filteredExercises.length" description="暂无动作数据" />

    <!-- 动作详情弹窗 -->
    <el-dialog v-model="showDialog" :title="currentEx?.name" width="520px">
      <div class="detail-content" v-if="currentEx">
        <div class="detail-row">
          <span class="detail-label">分类</span>
          <span>{{ currentEx.category }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">主要肌群</span>
          <span>{{ currentEx.muscleGroup }}</span>
        </div>
        <div class="detail-row" v-if="currentEx.secondaryMuscle">
          <span class="detail-label">辅助肌群</span>
          <span>{{ currentEx.secondaryMuscle }}</span>
        </div>
        <div class="detail-row" v-if="currentEx.equipment">
          <span class="detail-label">器械</span>
          <span>{{ currentEx.equipment }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">难度</span>
          <span>{{ difficultyLabel(currentEx.difficulty) }}</span>
        </div>
        <div class="detail-row" v-if="currentEx.description">
          <span class="detail-label">描述</span>
          <span>{{ currentEx.description }}</span>
        </div>
        <div class="detail-section" v-if="currentEx.instructions">
          <span class="detail-label">动作要领</span>
          <pre class="instructions">{{ currentEx.instructions }}</pre>
        </div>
      </div>
    </el-dialog>

    <!-- 添加动作弹窗 -->
    <el-dialog v-model="showAddDialog" title="添加动作" width="520px" :close-on-click-modal="false">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="动作名称" required>
          <el-input v-model="addForm.name" placeholder="例：杠铃弯举" />
        </el-form-item>
        <el-form-item label="英文名">
          <el-input v-model="addForm.nameEn" placeholder="例：Barbell Curl" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="addForm.category" filterable allow-create placeholder="选择或输入分类">
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="主要肌群" required>
          <el-input v-model="addForm.muscleGroup" placeholder="例：肱二头肌" />
        </el-form-item>
        <el-form-item label="辅助肌群">
          <el-input v-model="addForm.secondaryMuscle" placeholder="例：前臂" />
        </el-form-item>
        <el-form-item label="器械">
          <el-input v-model="addForm.equipment" placeholder="例：杠铃" />
        </el-form-item>
        <el-form-item label="难度">
          <el-radio-group v-model="addForm.difficulty">
            <el-radio :value="1">初级</el-radio>
            <el-radio :value="2">中级</el-radio>
            <el-radio :value="3">高级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="addForm.description" type="textarea" :rows="2" placeholder="简要描述..." />
        </el-form-item>
        <el-form-item label="动作要领">
          <el-input v-model="addForm.instructions" type="textarea" :rows="3" placeholder="训练步骤，换行分隔..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addExercise" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { trainingApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const exercises = ref([])
const keyword = ref('')
const activeCategory = ref('')
const showDialog = ref(false)
const currentEx = ref(null)
const showAddDialog = ref(false)
const saving = ref(false)

const addForm = reactive({
  name: '',
  nameEn: '',
  category: '',
  muscleGroup: '',
  secondaryMuscle: '',
  equipment: '',
  difficulty: 1,
  description: '',
  instructions: ''
})

const categories = computed(() => {
  const set = new Set(exercises.value.map(e => e.category))
  return [...set]
})

const filteredExercises = computed(() => {
  let list = exercises.value
  if (activeCategory.value) list = list.filter(e => e.category === activeCategory.value)
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter(e =>
      e.name.toLowerCase().includes(kw) ||
      (e.nameEn && e.nameEn.toLowerCase().includes(kw)) ||
      (e.muscleGroup && e.muscleGroup.includes(kw))
    )
  }
  return list
})

function difficultyLabel(d) {
  return { 1: '初级', 2: '中级', 3: '高级' }[d] || '初级'
}

function difficultyType(d) {
  return { 1: 'success', 2: '', 3: 'danger' }[d] || 'info'
}

function showDetail(ex) {
  currentEx.value = ex
  showDialog.value = true
}

function openAddDialog() {
  addForm.name = ''
  addForm.nameEn = ''
  addForm.category = ''
  addForm.muscleGroup = ''
  addForm.secondaryMuscle = ''
  addForm.equipment = ''
  addForm.difficulty = 1
  addForm.description = ''
  addForm.instructions = ''
  showAddDialog.value = true
}

async function addExercise() {
  if (!addForm.name.trim()) {
    ElMessage.warning('请输入动作名称')
    return
  }
  if (!addForm.category) {
    ElMessage.warning('请选择分类')
    return
  }
  if (!addForm.muscleGroup.trim()) {
    ElMessage.warning('请输入主要肌群')
    return
  }
  saving.value = true
  try {
    const res = await trainingApi.addExercise({
      name: addForm.name.trim(),
      nameEn: addForm.nameEn.trim() || null,
      category: addForm.category,
      muscleGroup: addForm.muscleGroup.trim(),
      secondaryMuscle: addForm.secondaryMuscle.trim() || null,
      equipment: addForm.equipment.trim() || null,
      difficulty: addForm.difficulty,
      description: addForm.description.trim() || null,
      instructions: addForm.instructions.trim() || null
    })
    if (res.code === 200) {
      ElMessage.success('动作已添加')
      showAddDialog.value = false
      loadExercises()
    }
  } catch (e) {
    // handled
  } finally {
    saving.value = false
  }
}

async function loadExercises() {
  try {
    const res = await trainingApi.getExercises()
    if (res.code === 200) exercises.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => loadExercises())
</script>

<style lang="scss" scoped>
.exercises-page { max-width: 1100px; margin: 0 auto; }

.filter-bar {
  margin-bottom: 20px;
  .filter-top { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
  .category-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px;
    .cat-tag { cursor: pointer; }
  }
}

.exercise-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;
  @media (max-width: 900px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 600px) { grid-template-columns: 1fr; }
}

.exercise-card {
  padding: 16px; cursor: pointer; transition: var(--transition);
  &:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
}

.ex-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;
  .ex-name { font-weight: 600; font-size: 15px; }
}

.ex-meta {
  display: flex; gap: 12px; font-size: 12px; color: var(--text-muted); margin-bottom: 10px;
}

.ex-muscles {
  display: flex; flex-wrap: wrap; gap: 6px;
  .muscle { font-size: 12px; padding: 2px 8px; border-radius: 4px;
    &.primary { background: rgba(15,118,110,0.1); color: var(--primary); }
    &.secondary { background: var(--bg-secondary); color: var(--text-secondary); }
  }
}

.detail-content { display: flex; flex-direction: column; gap: 14px; }

.detail-row {
  display: flex; gap: 12px;
  .detail-label { min-width: 70px; color: var(--text-muted); font-size: 13px; flex-shrink: 0; }
  span:last-child { font-size: 14px; }
}

.detail-section {
  .detail-label { display: block; color: var(--text-muted); font-size: 13px; margin-bottom: 8px; }
  .instructions {
    background: var(--bg-secondary); padding: 12px 16px; border-radius: var(--radius-sm);
    font-family: inherit; font-size: 14px; white-space: pre-wrap; margin: 0; line-height: 1.8;
  }
}
</style>
