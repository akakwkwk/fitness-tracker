<template>
  <div class="diet-page animate-fade-in">
    <!-- 日期选择 -->
    <div class="date-selector">
      <el-button text @click="prevDay"><el-icon><ArrowLeft /></el-icon></el-button>
      <span class="current-date">{{ formatDate(selectedDate) }}</span>
      <el-button text @click="nextDay"><el-icon><ArrowRight /></el-icon></el-button>
      <el-button size="small" @click="selectedDate = dayjs().format('YYYY-MM-DD')">今天</el-button>
    </div>

    <!-- 营养汇总 -->
    <div class="nutrition-summary">
      <div class="calorie-ring">
        <div class="ring-content">
          <span class="ring-value">{{ Math.round(Number(summary.totalCalories) || 0) }}</span>
          <span class="ring-label">卡路里</span>
        </div>
      </div>
      <div class="macro-bars">
        <div class="macro-item protein">
          <span class="macro-label">蛋白质</span>
          <el-progress :percentage="proteinProgress" :stroke-width="8" color="#3B82F6" />
          <span class="macro-value">{{ Math.round(Number(summary.totalProtein) || 0) }}g / {{ userStore.userInfo?.dailyProteinTarget || 150 }}g</span>
        </div>
        <div class="macro-item carbs">
          <span class="macro-label">碳水</span>
          <el-progress :percentage="carbsProgress" :stroke-width="8" color="#F97316" />
          <span class="macro-value">{{ Math.round(Number(summary.totalCarbs) || 0) }}g / {{ userStore.userInfo?.dailyCarbTarget || 250 }}g</span>
        </div>
        <div class="macro-item fat">
          <span class="macro-label">脂肪</span>
          <el-progress :percentage="fatProgress" :stroke-width="8" color="#EAB308" />
          <span class="macro-value">{{ Math.round(Number(summary.totalFat) || 0) }}g / {{ userStore.userInfo?.dailyFatTarget || 65 }}g</span>
        </div>
      </div>
    </div>

    <!-- 餐次列表 -->
    <div v-for="mealType in mealTypes" :key="mealType.value" class="card meal-section">
      <div class="meal-header">
        <div class="meal-info">
          <span class="meal-icon">{{ mealType.icon }}</span>
          <span class="meal-name">{{ mealType.label }}</span>
          <span class="meal-calories">{{ getMealCalories(mealType.value) }} kcal</span>
        </div>
        <el-button text type="primary" size="small" @click="openFoodDialog(mealType.value)">
          <el-icon><Plus /></el-icon>添加
        </el-button>
      </div>

      <div v-if="getMealRecords(mealType.value).length" class="meal-items">
        <div v-for="record in getMealRecords(mealType.value)" :key="record.id" class="meal-item">
          <div class="item-info">
            <span class="item-name">{{ record.foodName }}</span>
            <span class="item-amount">{{ record.amount }}g</span>
          </div>
          <div class="item-nutrition">
            <span>{{ Math.round(Number(record.calories)) }} kcal</span>
          </div>
          <el-button text type="danger" size="small" @click="deleteDietRecord(record.id)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
      <div v-else class="meal-empty">
        <span>暂无记录</span>
      </div>
    </div>

    <!-- 添加食物弹窗 -->
    <el-dialog v-model="showFoodDialog" title="添加食物" width="560px" :close-on-click-modal="false">
      <!-- 模式切换 -->
      <div class="dialog-tabs">
        <div class="tab-item" :class="{ active: dialogMode === 'search' }" @click="dialogMode = 'search'">
          <el-icon><Search /></el-icon>搜索食物
        </div>
        <div class="tab-item" :class="{ active: dialogMode === 'custom' }" @click="dialogMode = 'custom'">
          <el-icon><EditPen /></el-icon>自定义食物
        </div>
      </div>

      <!-- 模式1: 搜索本地食物 -->
      <div v-if="dialogMode === 'search'">
        <div class="food-search">
          <el-input v-model="foodSearch" placeholder="搜索食物..." :prefix-icon="Search" @input="searchFood" clearable />
        </div>
        <div class="food-categories">
          <el-tag
            v-for="cat in foodCategories"
            :key="cat"
            :type="selectedFoodCategory === cat ? '' : 'info'"
            @click="selectedFoodCategory = selectedFoodCategory === cat ? '' : cat; searchFood()"
            class="category-tag"
          >
            {{ cat }}
          </el-tag>
        </div>
        <div class="food-list">
          <div
            v-for="food in foods"
            :key="food.id"
            class="food-item"
            :class="{ selected: selectedFood?.id === food.id }"
            @click="selectFood(food)"
          >
            <div class="food-info">
              <span class="food-name">{{ food.name }}</span>
              <span class="food-detail">{{ food.calories }}kcal / {{ food.servingSize }}{{ food.servingUnit }}</span>
            </div>
            <div class="food-macros">
              <span class="macro-p">P:{{ food.protein }}g</span>
              <span class="macro-c">C:{{ food.carbs }}g</span>
              <span class="macro-f">F:{{ food.fat }}g</span>
            </div>
          </div>
          <el-empty v-if="!foods.length" description="暂无食物数据" :image-size="60" />
        </div>
      </div>

      <!-- 模式2: 自定义食物 -->
      <div v-if="dialogMode === 'custom'" class="custom-food-form">
        <el-form :model="customForm" label-width="80px" size="default">
          <el-form-item label="食物名称" required>
            <el-input v-model="customForm.name" placeholder="例：自制鸡胸肉沙拉" />
          </el-form-item>
          <el-form-item label="食物分类">
            <el-select v-model="customForm.category" placeholder="选择分类">
              <el-option v-for="cat in foodCategories" :key="cat" :label="cat" :value="cat" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="份量">
            <el-input-number v-model="customForm.servingSize" :min="1" :step="10" />
            <span class="unit">g</span>
          </el-form-item>
          <el-divider content-position="left">每份营养成分</el-divider>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="蛋白质" required>
                <el-input-number v-model="customForm.protein" :min="0" :step="1" :precision="1" controls-position="right" style="width:100%" />
                <span class="unit">g</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="碳水" required>
                <el-input-number v-model="customForm.carbs" :min="0" :step="1" :precision="1" controls-position="right" style="width:100%" />
                <span class="unit">g</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="脂肪" required>
                <el-input-number v-model="customForm.fat" :min="0" :step="1" :precision="1" controls-position="right" style="width:100%" />
                <span class="unit">g</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="卡路里">
                <el-input :model-value="customCalories" disabled controls-position="right" style="width:100%" />
                <span class="unit">kcal (自动计算)</span>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 已选食物预览 -->
      <div v-if="selectedFood && dialogMode !== 'custom'" class="selected-food">
        <div class="selected-header">
          <span class="selected-name">{{ selectedFood.name }}</span>
          <el-button text type="danger" size="small" @click="selectedFood = null">取消选择</el-button>
        </div>
        <el-form :model="foodForm" label-width="80px" size="small">
          <el-form-item label="食用量">
            <el-input-number v-model="foodForm.amount" :min="1" :step="10" />
            <span class="unit">g</span>
          </el-form-item>
        </el-form>
        <div class="food-nutrition-preview">
          <span>卡路里: {{ calcCalories }} kcal</span>
          <span>蛋白质: {{ calcProtein }}g</span>
          <span>碳水: {{ calcCarbs }}g</span>
          <span>脂肪: {{ calcFat }}g</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="showFoodDialog = false">取消</el-button>
        <el-button v-if="dialogMode === 'custom'" type="success" @click="saveCustomFood" :loading="savingCustom">
          <el-icon><Check /></el-icon>保存食物
        </el-button>
        <el-button type="primary" @click="addDietRecord" :disabled="!selectedFood" v-if="dialogMode !== 'custom'">
          确定添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { dietApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const userStore = useUserStore()
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const dietRecords = ref([])
const summary = ref({})
const showFoodDialog = ref(false)
const currentMealType = ref(1)
const foodSearch = ref('')
const selectedFoodCategory = ref('')
const foods = ref([])
const selectedFood = ref(null)
const foodForm = reactive({ amount: 100 })
const dialogMode = ref('search')

// 自定义食物
const savingCustom = ref(false)
const customForm = reactive({
  name: '',
  category: '其他',
  servingSize: 100,
  calories: 0,
  protein: 0,
  carbs: 0,
  fat: 0
})

const mealTypes = [
  { value: 1, label: '早餐', icon: '🌅' },
  { value: 2, label: '午餐', icon: '☀️' },
  { value: 3, label: '晚餐', icon: '🌙' },
  { value: 4, label: '加餐', icon: '🍎' }
]

const foodCategories = ['主食', '肉类', '蛋奶', '蔬菜', '水果', '坚果', '调味品', '补剂']

// 蛋白质×4 + 碳水×4 + 脂肪×9
const customCalories = computed(() => {
  return Math.round((customForm.protein || 0) * 4 + (customForm.carbs || 0) * 4 + (customForm.fat || 0) * 9)
})

const calcCalories = computed(() => {
  if (!selectedFood.value) return 0
  const ratio = foodForm.amount / (selectedFood.value.servingSize || 100)
  return Math.round(selectedFood.value.calories * ratio)
})
const calcProtein = computed(() => {
  if (!selectedFood.value) return 0
  const ratio = foodForm.amount / (selectedFood.value.servingSize || 100)
  return (selectedFood.value.protein * ratio).toFixed(1)
})
const calcCarbs = computed(() => {
  if (!selectedFood.value) return 0
  const ratio = foodForm.amount / (selectedFood.value.servingSize || 100)
  return (selectedFood.value.carbs * ratio).toFixed(1)
})
const calcFat = computed(() => {
  if (!selectedFood.value) return 0
  const ratio = foodForm.amount / (selectedFood.value.servingSize || 100)
  return (selectedFood.value.fat * ratio).toFixed(1)
})

const proteinProgress = computed(() => {
  const intake = Number(summary.value.totalProtein) || 0
  const target = userStore.userInfo?.dailyProteinTarget || 150
  return Math.min(Math.round((intake / target) * 100), 100)
})
const carbsProgress = computed(() => {
  const intake = Number(summary.value.totalCarbs) || 0
  const target = userStore.userInfo?.dailyCarbTarget || 250
  return Math.min(Math.round((intake / target) * 100), 100)
})
const fatProgress = computed(() => {
  const intake = Number(summary.value.totalFat) || 0
  const target = userStore.userInfo?.dailyFatTarget || 65
  return Math.min(Math.round((intake / target) * 100), 100)
})

function formatDate(date) {
  const d = dayjs(date)
  const today = dayjs().format('YYYY-MM-DD')
  const yesterday = dayjs().subtract(1, 'day').format('YYYY-MM-DD')
  if (date === today) return '今天'
  if (date === yesterday) return '昨天'
  return d.format('MM月DD日')
}
function prevDay() { selectedDate.value = dayjs(selectedDate.value).subtract(1, 'day').format('YYYY-MM-DD') }
function nextDay() { selectedDate.value = dayjs(selectedDate.value).add(1, 'day').format('YYYY-MM-DD') }
function getMealRecords(mealType) { return dietRecords.value.filter(r => r.mealType === mealType) }
function getMealCalories(mealType) { return Math.round(getMealRecords(mealType).reduce((sum, r) => sum + Number(r.calories || 0), 0)) }

function openFoodDialog(mealType) {
  currentMealType.value = mealType
  selectedFood.value = null
  foodForm.amount = 100
  dialogMode.value = 'search'
  resetCustomForm()
  showFoodDialog.value = true
  searchFood()
}

function resetCustomForm() {
  customForm.name = ''
  customForm.category = '其他'
  customForm.servingSize = 100
  customForm.calories = 0
  customForm.protein = 0
  customForm.carbs = 0
  customForm.fat = 0
}

async function searchFood() {
  try {
    const params = { page: 1, size: 50 }
    if (foodSearch.value) params.keyword = foodSearch.value
    if (selectedFoodCategory.value) params.category = selectedFoodCategory.value
    const res = await dietApi.searchFood(params)
    if (res.code === 200) {
      foods.value = res.data.records || []
    }
  } catch (e) {
    console.error('Failed to search food', e)
  }
}

function selectFood(food) {
  selectedFood.value = food
  foodForm.amount = Number(food.servingSize) || 100
}

// 保存自定义食物到数据库
async function saveCustomFood() {
  if (!customForm.name.trim()) {
    ElMessage.warning('请输入食物名称')
    return
  }
  if (!customForm.protein && !customForm.carbs && !customForm.fat) {
    ElMessage.warning('请至少填写一项营养成分')
    return
  }
  savingCustom.value = true
  try {
    const res = await dietApi.addCustomFood({
      name: customForm.name.trim(),
      category: customForm.category || '其他',
      servingSize: customForm.servingSize,
      servingUnit: 'g',
      calories: customCalories.value,
      protein: customForm.protein,
      carbs: customForm.carbs,
      fat: customForm.fat
    })
    if (res.code === 200) {
      ElMessage.success('自定义食物已保存')
      // 自动选中并切换到搜索模式
      selectedFood.value = res.data
      foodForm.amount = customForm.servingSize
      dialogMode.value = 'search'
      searchFood()
    }
  } catch (e) {
    // handled
  } finally {
    savingCustom.value = false
  }
}

async function addDietRecord() {
  if (!selectedFood.value) return

  let foodId = selectedFood.value.id
  if (!foodId) {
    // 食物没有ID时先保存到数据库
    try {
      const saveRes = await dietApi.addCustomFood({
        name: selectedFood.value.name,
        category: selectedFood.value.category || '其他',
        servingSize: selectedFood.value.servingSize || 100,
        servingUnit: 'g',
        calories: selectedFood.value.calories,
        protein: selectedFood.value.protein,
        carbs: selectedFood.value.carbs,
        fat: selectedFood.value.fat
      })
      if (saveRes.code === 200) {
        foodId = saveRes.data.id
      } else {
        ElMessage.error('保存食物失败')
        return
      }
    } catch (e) {
      ElMessage.error('保存食物失败')
      return
    }
  }

  try {
    const res = await dietApi.add({
      recordDate: selectedDate.value,
      mealType: currentMealType.value,
      foodId: foodId,
      amount: foodForm.amount
    })
    if (res.code === 200) {
      ElMessage.success('已添加')
      showFoodDialog.value = false
      loadDietData()
    }
  } catch (e) {
    // handled
  }
}

async function deleteDietRecord(id) {
  try {
    await ElMessageBox.confirm('确定删除这条记录？', '确认')
    const res = await dietApi.delete(id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      loadDietData()
    }
  } catch (e) { /* cancelled */ }
}

async function loadDietData() {
  try {
    const [recordsRes, summaryRes] = await Promise.all([
      dietApi.getDaily(selectedDate.value),
      dietApi.getDailySummary(selectedDate.value)
    ])
    if (recordsRes.code === 200) dietRecords.value = recordsRes.data || []
    if (summaryRes.code === 200) summary.value = summaryRes.data || {}
  } catch (e) {
    console.error('Failed to load diet data', e)
  }
}

watch(selectedDate, () => loadDietData())
onMounted(() => loadDietData())
</script>

<style lang="scss" scoped>
.diet-page { max-width: 900px; margin: 0 auto; }

.date-selector {
  display: flex; align-items: center; justify-content: center; gap: 16px; margin-bottom: 24px;
  .current-date { font-size: 18px; font-weight: 600; min-width: 100px; text-align: center; }
}

.nutrition-summary {
  display: flex; gap: 32px; align-items: center; background: var(--bg-card);
  border-radius: var(--radius); padding: 24px; box-shadow: var(--shadow); margin-bottom: 24px;
  @media (max-width: 768px) { flex-direction: column; }
}

.calorie-ring {
  width: 120px; height: 120px; border-radius: 50%; flex-shrink: 0;
  background: conic-gradient(var(--primary) 0%, var(--primary) var(--progress, 0%), var(--bg-secondary) var(--progress, 0%));
  display: flex; align-items: center; justify-content: center;
  .ring-content {
    width: 100px; height: 100px; border-radius: 50%; background: var(--bg-card);
    display: flex; flex-direction: column; align-items: center; justify-content: center;
    .ring-value { font-size: 24px; font-weight: 700; color: var(--primary); }
    .ring-label { font-size: 12px; color: var(--text-muted); }
  }
}

.macro-bars { flex: 1; display: flex; flex-direction: column; gap: 16px; }
.macro-item {
  .macro-label { display: block; font-size: 13px; color: var(--text-secondary); margin-bottom: 4px; }
  .macro-value { display: block; font-size: 12px; color: var(--text-muted); margin-top: 4px; }
}

.meal-section { margin-bottom: 16px; }
.meal-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;
  .meal-info { display: flex; align-items: center; gap: 8px;
    .meal-icon { font-size: 20px; }
    .meal-name { font-weight: 600; font-size: 15px; }
    .meal-calories { font-size: 13px; color: var(--text-muted); }
  }
}
.meal-items { display: flex; flex-direction: column; gap: 8px; }
.meal-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 12px; background: var(--bg-secondary); border-radius: var(--radius-sm);
  .item-info { display: flex; flex-direction: column;
    .item-name { font-weight: 500; font-size: 14px; }
    .item-amount { font-size: 12px; color: var(--text-muted); }
  }
  .item-nutrition { font-size: 13px; color: var(--text-secondary); }
}
.meal-empty { text-align: center; padding: 16px; color: var(--text-muted); font-size: 13px; }

/* 弹窗内样式 */
.dialog-tabs {
  display: flex; gap: 0; margin-bottom: 20px; border: 1px solid var(--border); border-radius: var(--radius-sm); overflow: hidden;
  .tab-item {
    flex: 1; text-align: center; padding: 10px 0; cursor: pointer; font-size: 14px; font-weight: 500;
    color: var(--text-secondary); background: var(--bg-secondary); transition: var(--transition);
    display: flex; align-items: center; justify-content: center; gap: 6px;
    &:hover { background: var(--border); }
    &.active { background: var(--primary); color: #fff; }
    + .tab-item { border-left: 1px solid var(--border); }
  }
}

.food-search { margin-bottom: 12px; }
.food-categories { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px;
  .category-tag { cursor: pointer; }
}
.food-list { max-height: 320px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px; }
.food-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px; background: var(--bg-secondary); border-radius: var(--radius-sm);
  cursor: pointer; transition: var(--transition); border: 2px solid transparent;
  &:hover { background: var(--border); }
  &.selected { border-color: var(--primary); background: rgba(15,118,110,0.05); }
  .food-info { display: flex; flex-direction: column;
    .food-name { font-weight: 500; }
    .food-detail { font-size: 12px; color: var(--text-muted); }
  }
  .food-macros { display: flex; gap: 10px; font-size: 12px; font-weight: 500;
    .macro-p { color: #3B82F6; }
    .macro-c { color: #F97316; }
    .macro-f { color: #EAB308; }
  }
}

.custom-food-form {
  .unit { margin-left: 8px; color: var(--text-muted); font-size: 13px; }
  :deep(.el-divider__text) { font-size: 13px; color: var(--text-muted); }
}

.selected-food {
  margin-top: 16px; padding: 16px; background: var(--bg-secondary); border-radius: var(--radius-sm);
  .selected-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;
    .selected-name { font-weight: 600; font-size: 15px; }
  }
  .unit { margin-left: 8px; color: var(--text-muted); }
  .food-nutrition-preview {
    display: flex; gap: 16px; flex-wrap: wrap; margin-top: 12px;
    font-size: 13px; color: var(--primary); font-weight: 500;
  }
}
</style>
