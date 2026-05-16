<template>
  <div class="settings-page animate-fade-in">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="个人信息" name="profile">
        <div class="card">
          <h3 class="section-title">基本信息</h3>
          <el-form :model="userForm" label-width="100px" :rules="userRules" ref="userFormRef">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="userForm.nickname" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="userForm.email" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="userForm.phone" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="性别">
                  <el-radio-group v-model="userForm.gender">
                    <el-radio :value="1">男</el-radio>
                    <el-radio :value="2">女</el-radio>
                    <el-radio :value="0">保密</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="生日">
                  <el-date-picker v-model="userForm.birthday" type="date" value-format="YYYY-MM-DD" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="身高(cm)">
                  <el-input-number v-model="userForm.height" :min="0" :max="250" :step="0.5" :precision="1" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="体重(kg)">
                  <el-input-number v-model="userForm.weight" :min="0" :max="300" :step="0.5" :precision="1" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="活动水平">
                  <el-select v-model="userForm.activityLevel">
                    <el-option label="久坐" :value="1" />
                    <el-option label="轻度活动" :value="2" />
                    <el-option label="中度活动" :value="3" />
                    <el-option label="高强度" :value="4" />
                    <el-option label="极高" :value="5" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="saveUserInfo" :loading="savingUser">保存信息</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="健身目标" name="goals">
        <div class="card">
          <h3 class="section-title">健身目标</h3>
          <el-form :model="goalForm" label-width="100px">
            <el-form-item label="目标类型">
              <el-radio-group v-model="goalForm.fitnessGoal">
                <el-radio-button :value="1">增肌</el-radio-button>
                <el-radio-button :value="2">减脂</el-radio-button>
                <el-radio-button :value="3">维持</el-radio-button>
                <el-radio-button :value="4">力量提升</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <h3 class="section-title" style="margin-top: 24px;">每日营养目标</h3>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="卡路里(kcal)">
                  <el-input-number v-model="goalForm.dailyCalorieTarget" :min="500" :max="10000" :step="100" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="蛋白质(g)">
                  <el-input-number v-model="goalForm.dailyProteinTarget" :min="0" :max="500" :step="10" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="碳水(g)">
                  <el-input-number v-model="goalForm.dailyCarbTarget" :min="0" :max="1000" :step="10" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="脂肪(g)">
                  <el-input-number v-model="goalForm.dailyFatTarget" :min="0" :max="300" :step="5" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="饮水目标(ml)">
                  <el-input-number v-model="goalForm.dailyWaterTarget" :min="500" :max="5000" :step="100" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="每周训练(次)">
                  <el-input-number v-model="goalForm.weeklyTrainingTarget" :min="1" :max="14" :step="1" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="saveGoals" :loading="savingGoals">保存目标</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="账户安全" name="security">
        <div class="card">
          <h3 class="section-title">修改密码</h3>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="savingPassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('profile')
const userFormRef = ref(null)
const passwordFormRef = ref(null)
const savingUser = ref(false)
const savingGoals = ref(false)
const savingPassword = ref(false)

const userForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  birthday: '',
  height: null,
  weight: null,
  activityLevel: 2
})

const goalForm = reactive({
  fitnessGoal: 2,
  dailyCalorieTarget: 2000,
  dailyProteinTarget: 150,
  dailyCarbTarget: 250,
  dailyFatTarget: 65,
  dailyWaterTarget: 2000,
  weeklyTrainingTarget: 5
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const userRules = {
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

function initForms() {
  const user = userStore.userInfo
  if (!user) return

  Object.assign(userForm, {
    nickname: user.nickname || '',
    email: user.email || '',
    phone: user.phone || '',
    gender: user.gender || 0,
    birthday: user.birthday || '',
    height: user.height,
    weight: user.weight,
    activityLevel: user.activityLevel || 2
  })

  Object.assign(goalForm, {
    fitnessGoal: user.fitnessGoal || 2,
    dailyCalorieTarget: user.dailyCalorieTarget || 2000,
    dailyProteinTarget: user.dailyProteinTarget || 150,
    dailyCarbTarget: user.dailyCarbTarget || 250,
    dailyFatTarget: user.dailyFatTarget || 65,
    dailyWaterTarget: user.dailyWaterTarget || 2000,
    weeklyTrainingTarget: user.weeklyTrainingTarget || 5
  })
}

async function saveUserInfo() {
  savingUser.value = true
  try {
    const res = await userApi.updateUser(userForm)
    if (res.code === 200) {
      ElMessage.success('个人信息已更新')
      userStore.fetchUserInfo()
    }
  } catch (e) {
    // error handled
  } finally {
    savingUser.value = false
  }
}

async function saveGoals() {
  savingGoals.value = true
  try {
    const res = await userApi.updateUser(goalForm)
    if (res.code === 200) {
      ElMessage.success('健身目标已更新')
      userStore.fetchUserInfo()
    }
  } catch (e) {
    // error handled
  } finally {
    savingGoals.value = false
  }
}

async function changePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  savingPassword.value = true
  try {
    const res = await userApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码已修改')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  } catch (e) {
    // error handled
  } finally {
    savingPassword.value = false
  }
}

onMounted(async () => {
  await userStore.fetchUserInfo()
  initForms()
})
</script>

<style lang="scss" scoped>
.settings-page {
  max-width: 900px;
  margin: 0 auto;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.card {
  margin-bottom: 20px;
}
</style>
