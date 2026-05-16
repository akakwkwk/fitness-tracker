<template>
  <div class="achievement-page animate-fade-in">
    <!-- 积分概览 -->
    <div class="points-banner">
      <div class="points-info">
        <span class="points-value">{{ totalPoints }}</span>
        <span class="points-label">成就积分</span>
      </div>
      <div class="points-stats">
        <span>已解锁 {{ unlockedCount }} / {{ achievements.length }}</span>
      </div>
      <el-button type="primary" @click="checkAchievements" :loading="checking">
        <el-icon><Refresh /></el-icon>检查成就
      </el-button>
    </div>

    <!-- 分类标签 -->
    <div class="category-tabs">
      <el-tag
        v-for="cat in categories"
        :key="cat"
        :type="activeCategory === cat ? '' : 'info'"
        @click="activeCategory = activeCategory === cat ? '' : cat"
        class="category-tag"
      >
        {{ cat }}
      </el-tag>
    </div>

    <!-- 成就列表 -->
    <div class="achievement-grid">
      <div
        v-for="item in filteredAchievements"
        :key="item.id"
        class="achievement-card card"
        :class="{ locked: !item.unlocked, unlocked: item.unlocked }"
      >
        <div class="ach-icon" :class="'rarity-' + item.rarity">{{ item.icon }}</div>
        <div class="ach-info">
          <span class="ach-name">{{ item.name }}</span>
          <span class="ach-desc">{{ item.description }}</span>
          <div class="ach-meta">
            <span class="ach-points">+{{ item.points }} 积分</span>
            <el-tag size="small" :type="rarityTagType(item.rarity)">{{ rarityLabel(item.rarity) }}</el-tag>
          </div>
        </div>
        <div class="ach-status">
          <el-icon v-if="item.unlocked" :size="24" color="#22C55E"><CircleCheckFilled /></el-icon>
          <el-icon v-else :size="24" color="#94A3B8"><Lock /></el-icon>
        </div>
      </div>
    </div>

    <el-empty v-if="!filteredAchievements.length" description="暂无成就数据" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { achievementApi } from '@/api'
import { ElMessage } from 'element-plus'

const achievements = ref([])
const totalPoints = ref(0)
const checking = ref(false)
const activeCategory = ref('')

const categories = computed(() => {
  const set = new Set(achievements.value.map(a => a.category))
  return [...set]
})

const unlockedCount = computed(() => achievements.value.filter(a => a.unlocked).length)

const filteredAchievements = computed(() => {
  if (!activeCategory.value) return achievements.value
  return achievements.value.filter(a => a.category === activeCategory.value)
})

function rarityLabel(r) {
  return { 1: '普通', 2: '稀有', 3: '史诗', 4: '传说' }[r] || '普通'
}

function rarityTagType(r) {
  return { 1: 'info', 2: '', 3: 'warning', 4: 'danger' }[r] || 'info'
}

async function loadData() {
  try {
    const [achRes, ptsRes] = await Promise.all([
      achievementApi.getList(),
      achievementApi.getPoints()
    ])
    if (achRes.code === 200) achievements.value = achRes.data || []
    if (ptsRes.code === 200) totalPoints.value = ptsRes.data || 0
  } catch (e) {
    console.error(e)
  }
}

async function checkAchievements() {
  checking.value = true
  try {
    const res = await achievementApi.check()
    if (res.code === 200) {
      const unlocked = res.data || []
      if (unlocked.length) {
        ElMessage.success(`恭喜解锁 ${unlocked.length} 个成就！`)
      } else {
        ElMessage.info('暂无新成就解锁，继续努力！')
      }
      loadData()
    }
  } catch (e) {
    // handled
  } finally {
    checking.value = false
  }
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.achievement-page { max-width: 1000px; margin: 0 auto; }

.points-banner {
  display: flex; align-items: center; gap: 24px;
  background: linear-gradient(135deg, var(--primary), #2DD4BF);
  border-radius: var(--radius); padding: 28px 32px; margin-bottom: 24px; color: #fff;
  .points-info { display: flex; flex-direction: column;
    .points-value { font-size: 36px; font-weight: 800; }
    .points-label { font-size: 14px; opacity: 0.85; }
  }
  .points-stats { flex: 1; font-size: 14px; opacity: 0.9; }
  .el-button { --el-button-bg-color: rgba(255,255,255,0.2); --el-button-border-color: transparent; --el-button-hover-bg-color: rgba(255,255,255,0.35); --el-button-hover-border-color: transparent; --el-button-text-color: #fff; }
}

.category-tabs {
  display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 20px;
  .category-tag { cursor: pointer; }
}

.achievement-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px;
  @media (max-width: 768px) { grid-template-columns: 1fr; }
}

.achievement-card {
  display: flex; align-items: center; gap: 16px; padding: 16px 20px;
  transition: var(--transition);
  &.locked { opacity: 0.55; }
  &.unlocked { border-left: 4px solid #22C55E; }
}

.ach-icon {
  font-size: 36px; width: 52px; height: 52px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center; border-radius: 12px;
  &.rarity-1 { background: rgba(148,163,184,0.15); }
  &.rarity-2 { background: rgba(59,130,246,0.15); }
  &.rarity-3 { background: rgba(245,158,11,0.15); }
  &.rarity-4 { background: rgba(239,68,68,0.15); }
}

.ach-info {
  flex: 1; display: flex; flex-direction: column; gap: 4px;
  .ach-name { font-weight: 600; font-size: 15px; }
  .ach-desc { font-size: 13px; color: var(--text-secondary); }
  .ach-meta { display: flex; align-items: center; gap: 10px; margin-top: 4px;
    .ach-points { font-size: 12px; color: var(--primary); font-weight: 600; }
  }
}

.ach-status { flex-shrink: 0; }
</style>
