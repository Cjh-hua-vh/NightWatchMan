<template>
  <div class="register-page">
    <ParticleBackground />
    <div class="register-card glow-hover">
      <div class="register-header">
        <img src="/logo.png" alt="Logo" class="logo-img" />
        <h1 class="title">加入卡塞尔之门</h1>
        <p class="subtitle">卡塞尔学院 · 学员注册</p>
      </div>

      <el-form ref="formRef" :model="registerForm" :rules="rules" label-width="80px" class="register-form">
        <el-form-item label="头像" prop="avatar">
          <el-upload :show-file-list="false" :before-upload="beforeAvatarUpload" :http-request="handleAvatarUpload" accept="image/*">
            <div class="avatar-upload-wrap">
              <img v-if="registerForm.avatar" :src="registerForm.avatar" class="avatar-preview" />
              <div v-else class="avatar-placeholder">
                <el-icon :size="28"><Plus /></el-icon>
                <span>选择头像</span>
              </div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="3-20位字符" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="6-20位字符" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="registerForm.nickname" placeholder="你的昵称（选填，最多10字）" maxlength="10" show-word-limit />
        </el-form-item>
        <el-form-item label="血统等级" prop="bloodlineGrade">
          <div class="bloodline-display">
            <span class="bl-grade">{{ registerForm.bloodlineGrade }}</span>
            <span class="bl-label">级混血种</span>
            <span class="bl-lock">🔒 血统不可修改</span>
          </div>
        </el-form-item>
        <el-form-item label="派系" prop="faction">
          <el-select v-model="registerForm.faction" placeholder="选择派系（选填）" style="width: 100%" clearable>
            <el-option label="狮心会" value="狮心会" />
            <el-option label="学生会" value="学生会" />
            <el-option label="执行部" value="执行部" />
            <el-option label="教授" value="教授" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="register-btn" :loading="loading" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Plus } from '@element-plus/icons-vue'
import { register } from '../api/auth'
import { uploadAvatar } from '../api/file'
import ParticleBackground from '../components/ParticleBackground.vue'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  nickname: '',
  avatar: '',
  bloodlineGrade: 'D',
  yanling: '',
  faction: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

onMounted(() => {
  const grade = route.query.grade
  const yanling = route.query.yanling
  if (grade) registerForm.bloodlineGrade = grade
  if (yanling) registerForm.yanling = yanling
})

function beforeAvatarUpload(file) {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false }
  if (file.size / 1024 / 1024 > 2) { ElMessage.error('图片不能超过2MB'); return false }
  return true
}

async function handleAvatarUpload(options) {
  try {
    const res = await uploadAvatar(options.file)
    registerForm.avatar = res.data
  } catch { ElMessage.error('头像上传失败') }
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register(registerForm)
    ElMessage.success('注册成功，请等待管理员审核')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  position: relative;
  width: 100vw;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  overflow: auto;
  padding: var(--spacing-xl) 0;
}

.register-card {
  position: relative;
  z-index: 10;
  width: 460px;
  max-width: 95vw;
  background: rgba(26, 31, 58, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid var(--accent-primary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-glow-strong), 0 20px 60px rgba(0, 0, 0, 0.5);
}

.register-header { text-align: center; margin-bottom: var(--spacing-xl); }
.logo-img { width: 52px; height: 52px; border-radius: 50%; object-fit: cover; margin-bottom: var(--spacing-sm); }
.title { font-size: 26px; font-weight: 700; color: var(--accent-primary); text-shadow: 0 0 20px var(--accent-glow); letter-spacing: 3px; margin-bottom: var(--spacing-xs); }
.subtitle { font-size: 13px; color: var(--text-secondary); }

.register-form { margin-top: var(--spacing-lg); }
.register-form :deep(.el-form-item) { margin-bottom: var(--spacing-md); }
.register-form :deep(.el-form-item__label) { color: var(--text-secondary); }

/* 头像上传居中 */
.avatar-upload-wrap { display: flex; justify-content: center; width: 100%; }
.avatar-preview {
  width: 72px; height: 72px; border-radius: 50%; object-fit: cover;
  border: 2px solid var(--accent-primary);
  box-shadow: 0 0 10px var(--accent-glow);
}
.avatar-placeholder {
  width: 72px; height: 72px; border-radius: 50%;
  background: rgba(0, 212, 255, 0.08);
  border: 2px dashed rgba(0, 212, 255, 0.3);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  color: var(--text-muted); font-size: 11px; gap: 2px;
  transition: all var(--transition-fast);
}
.avatar-placeholder:hover {
  border-color: var(--accent-primary);
  background: rgba(0, 212, 255, 0.15);
}

.bloodline-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.25);
  border-radius: var(--radius-md);
  padding: var(--spacing-sm) var(--spacing-md);
}
.bl-grade {
  font-size: 24px;
  font-weight: 900;
  color: var(--accent-primary);
  text-shadow: 0 0 10px var(--accent-glow);
}
.bl-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 600;
}
.bl-lock {
  font-size: 11px;
  color: var(--text-muted);
  background: rgba(0, 0, 0, 0.3);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}

.register-btn { width: 100%; }

.register-footer { text-align: center; margin-top: var(--spacing-md); font-size: 14px; color: var(--text-secondary); }
.register-footer a { color: var(--accent-primary); margin-left: var(--spacing-xs); transition: all var(--transition-fast); }
.register-footer a:hover { text-shadow: 0 0 8px var(--accent-glow); }

@media (max-width: 768px) {
  .register-card { width: 100%; margin: 0 var(--spacing-md); padding: var(--spacing-lg); }
  .register-form :deep(.el-form-item) { flex-direction: column; align-items: flex-start; }
  .register-form :deep(.el-form-item__label) { width: 100% !important; text-align: left; margin-bottom: 4px; }
  .register-form :deep(.el-form-item__content) { margin-left: 0 !important; width: 100%; }
  .register-form :deep(.el-input), .register-form :deep(.el-select) { width: 100% !important; }
  .register-form :deep(.el-select__wrapper) { width: 100% !important; }
}

@media (max-width: 480px) {
  .register-card { padding: var(--spacing-md); }
}
</style>
