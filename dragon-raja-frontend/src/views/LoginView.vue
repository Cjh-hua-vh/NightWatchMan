<template>
  <div class="login-page">
    <!-- 粒子背景 -->
    <ParticleBackground />

    <!-- 登录卡片 -->
    <div class="login-card glow-hover">
      <div class="login-header">
        <img src="/logo.png" alt="Logo" class="logo-img" />
        <h1 class="title">卡塞尔之门</h1>
        <p class="subtitle">卡塞尔学院 · 龙族社区</p>
      </div>

      <el-form ref="formRef" :model="loginForm" :rules="rules" label-width="0" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/blood-test">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'
import ParticleBackground from '../components/ParticleBackground.vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 登录处理
async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(loginForm)
      // 存储token和用户信息
      userStore.setToken(res.data.token, {
        id: res.data.userId,
        username: res.data.username,
        nickname: res.data.nickname,
        role: res.data.role,
        avatar: res.data.avatar
      })
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  overflow: hidden;
}

/* 登录卡片 */
.login-card {
  position: relative;
  z-index: 10;
  width: 420px;
  background: rgba(26, 31, 58, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid var(--accent-primary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl) var(--spacing-xl) var(--spacing-lg);
  box-shadow: var(--shadow-glow-strong), 0 20px 60px rgba(0, 0, 0, 0.5);
}

/* 卡片头部 */
.login-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.logo-img {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: var(--spacing-sm);
}

.title {
  font-size: 26px;
  font-weight: 700;
  color: var(--accent-primary);
  text-shadow: 0 0 20px var(--accent-glow);
  letter-spacing: 3px;
  margin-bottom: var(--spacing-xs);
}

.subtitle {
  color: var(--text-secondary);
  font-size: 13px;
  letter-spacing: 1px;
}

/* 登录表单 */
.login-form {
  margin-bottom: var(--spacing-md);
}

.login-btn {
  width: 100%;
  letter-spacing: 4px;
}

/* 底部链接 */
.login-footer {
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
}

.login-footer a {
  color: var(--accent-primary);
  font-weight: 500;
  margin-left: var(--spacing-xs);
}

.login-footer a:hover {
  text-shadow: 0 0 8px var(--accent-glow);
}

/* 演示提示 */
.demo-tip {
  margin-top: var(--spacing-md);
  padding: var(--spacing-sm);
  background-color: rgba(0, 212, 255, 0.1);
  border: 1px dashed var(--accent-secondary);
  border-radius: var(--radius-sm);
  text-align: center;
  color: var(--accent-secondary);
  font-size: 12px;
}

/* 输入框适配 */
.login-form :deep(.el-input__wrapper) {
  background-color: rgba(16, 22, 50, 0.8) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.3) inset !important;
}
.login-form :deep(.el-input__inner) {
  color: #e0e4f0 !important;
}
.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(224, 228, 240, 0.4) !important;
}
.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--accent-primary) inset !important;
}
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--accent-primary) inset, 0 0 12px var(--accent-glow) !important;
}

@media (max-width: 480px) { .login-page { padding: 0; } .login-card { border-radius: 0; border: none; min-height: 100vh; } }
</style>
