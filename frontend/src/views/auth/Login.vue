<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="brand">
          <el-icon :size="48" color="white"><DataBoard /></el-icon>
          <h1>FitTracker Pro</h1>
          <p>专业个人健身训练与饮食记录平台</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <el-icon><Trophy /></el-icon>
            <span>科学训练记录</span>
          </div>
          <div class="feature-item">
            <el-icon><Food /></el-icon>
            <span>智能饮食追踪</span>
          </div>
          <div class="feature-item">
            <el-icon><DataLine /></el-icon>
            <span>数据可视化分析</span>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="login-form-container">
          <h2>欢迎回来</h2>
          <p class="subtitle">登录您的账户继续训练</p>

          <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                size="large"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleLogin"
                class="login-btn"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span>还没有账户？</span>
            <router-link to="/register">立即注册</router-link>
          </div>

          <div class="demo-hint">
            <el-tag type="info">演示账号: admin / admin123</el-tag>
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
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userStore.login(form.username, form.password)
    if (res.code === 200) {
      ElMessage.success('登录成功')
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
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0F766E 0%, #14B8A6 50%, #0D9488 100%);
  padding: 20px;
}

.login-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.login-left {
  flex: 1;
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
    margin-bottom: 40px;

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

  .features {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .feature-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 10px;
      backdrop-filter: blur(10px);

      .el-icon {
        font-size: 20px;
      }

      span {
        font-size: 15px;
      }
    }
  }
}

.login-right {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;

  @media (max-width: 768px) {
    padding: 40px 24px;
  }
}

.login-form-container {
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

.login-btn {
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

.demo-hint {
  text-align: center;
  margin-top: 20px;
}
</style>
