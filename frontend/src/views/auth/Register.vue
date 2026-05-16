<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-left">
        <div class="brand">
          <el-icon :size="48" color="white"><DataBoard /></el-icon>
          <h1>FitTracker Pro</h1>
          <p>开始您的健身之旅</p>
        </div>
      </div>
      <div class="register-right">
        <div class="register-form-container">
          <h2>创建账户</h2>
          <p class="subtitle">注册成为会员，开始记录训练</p>

          <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleRegister">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名 (3-50个字符)" :prefix-icon="User" size="large" />
            </el-form-item>

            <el-form-item prop="nickname">
              <el-input v-model="form.nickname" placeholder="昵称 (可选)" :prefix-icon="UserFilled" size="large" />
            </el-form-item>

            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码 (至少6位)" :prefix-icon="Lock" size="large" show-password />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" size="large" show-password />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" @click="handleRegister" class="register-btn">
                注册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span>已有账户？</span>
            <router-link to="/login">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度6-50个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userStore.register(form.username, form.password, form.nickname)
    if (res.code === 200) {
      ElMessage.success('注册成功')
      router.push('/dashboard')
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0F766E 0%, #14B8A6 50%, #0D9488 100%);
  padding: 20px;
}

.register-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.register-left {
  flex: 0 0 40%;
  background: linear-gradient(135deg, #0F766E, #0D9488);
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: white;

  @media (max-width: 768px) {
    display: none;
  }

  .brand {
    h1 {
      font-size: 32px;
      font-weight: 700;
      margin-top: 16px;
    }

    p {
      font-size: 16px;
      opacity: 0.9;
      margin-top: 8px;
    }
  }
}

.register-right {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-form-container {
  width: 100%;
  max-width: 360px;

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: #0F172A;
    margin-bottom: 8px;
  }

  .subtitle {
    color: #64748B;
    margin-bottom: 32px;
  }
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  color: #64748B;

  a {
    color: #0F766E;
    font-weight: 600;
    margin-left: 4px;
  }
}
</style>
