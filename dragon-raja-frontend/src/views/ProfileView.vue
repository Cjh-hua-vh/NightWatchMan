<template>
  <div class="profile-view" v-loading="loading">
    <!-- 用户信息卡片 -->
    <div class="profile-card" v-if="profile">
      <div class="profile-header">
        <el-avatar :size="80" :src="profile.avatar">
          {{ profile.nickname?.charAt(0) || profile.username?.charAt(0) }}
        </el-avatar>
        <div class="profile-info">
          <div class="info-name">
            <span class="nickname-text">{{ profile.nickname || profile.username }}</span>
            <span
              class="grade-badge"
              :style="{ color: getGradeColor(profile.bloodlineGrade), borderColor: getGradeColor(profile.bloodlineGrade) }"
            >
              {{ profile.bloodlineGrade }}级
            </span>
            <span class="faction-text" v-if="profile.faction">{{ profile.faction }}</span>
            <el-tag v-if="profile.role === 'ADMIN'" type="warning" size="small" effect="dark">管理员</el-tag>
          </div>
          <div class="info-username">@{{ profile.username }}</div>
          <div class="info-yanling" v-if="profile.yanling">
            🔥 言灵：<span class="yanling-text">{{ profile.yanling }}</span>
          </div>
          <div class="info-signature" v-if="profile.signature">{{ profile.signature }}</div>
          <div class="info-time">注册时间：{{ formatDateTime(profile.createTime) }}</div>
        </div>
      </div>
    </div>

    <!-- 操作区 -->
    <div class="action-area" v-if="profile">
      <el-tabs v-model="activeTab">
        <!-- 编辑资料 -->
        <el-tab-pane label="编辑资料" name="profile">
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称（最多10字）" maxlength="10" show-word-limit />
            </el-form-item>

            <el-form-item label="头像" prop="avatar">
              <el-upload :show-file-list="false" :before-upload="beforeAvatarUpload" :http-request="handleAvatarUpload" accept="image/*">
                <img v-if="profileForm.avatar" :src="profileForm.avatar" class="avatar-upload-preview" />
                <div v-else class="avatar-upload-placeholder">
                  <el-icon :size="24"><Plus /></el-icon>
                </div>
              </el-upload>
            </el-form-item>

            <el-form-item label="个性签名" prop="signature">
              <el-input
                v-model="profileForm.signature"
                type="textarea"
                :rows="3"
                placeholder="请输入个性签名"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="血统等级" prop="bloodlineGrade">
              <el-tag :color="getGradeColor(profileForm.bloodlineGrade)" effect="dark" size="large">
                {{ profileForm.bloodlineGrade }}级
              </el-tag>
              <span style="margin-left:8px;font-size:12px;color:var(--text-muted)">血统等级不可修改</span>
            </el-form-item>

            <el-form-item label="派系" prop="faction">
              <el-select v-model="profileForm.faction" placeholder="选择派系" style="width: 100%" clearable>
                <el-option label="狮心会" value="狮心会" />
                <el-option label="学生会" value="学生会" />
                <el-option label="执行部" value="执行部" />
                <el-option label="教授" value="教授" />
              </el-select>
            </el-form-item>

            <el-form-item label="言灵" prop="yanling">
              <el-input v-model="profileForm.yanling" placeholder="请输入言灵名称" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="savingProfile" @click="handleSaveProfile">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（6-20位）" />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="savingPassword" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- 我的帖子 -->
        <el-tab-pane label="我的帖子" name="posts">
          <div v-loading="loadingPosts">
            <div v-if="myPosts.length === 0 && !loadingPosts" class="empty-posts">
              <el-empty description="还没有发过帖子" />
            </div>
            <PostCard v-for="post in myPosts" :key="post.id" :post="post" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getProfile, updateProfile, updatePassword } from '../api/user'
import { uploadAvatar } from '../api/file'
import { Plus } from '@element-plus/icons-vue'
import { getGradeColor, formatDateTime } from '../utils/format'
import { getUserPosts } from '../api/post'
import PostCard from '../components/PostCard.vue'

const userStore = useUserStore()

const loading = ref(false)
const savingProfile = ref(false)
const savingPassword = ref(false)
const activeTab = ref('profile')

const profile = ref(null)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const myPosts = ref([])
const loadingPosts = ref(false)
const postsCurrentPage = ref(1)
const postsPageSize = ref(10)
const postsTotal = ref(0)

const profileForm = reactive({
  nickname: '',
  avatar: '',
  signature: '',
  bloodlineGrade: 'D',
  faction: '',
  yanling: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const gradeOptions = [
  { value: 'S', label: 'S级 - 纯血龙裔', color: '#ffd700' },
  { value: 'A', label: 'A级 - 高阶混血', color: '#b388ff' },
  { value: 'B', label: 'B级 - 中阶混血', color: '#4da6ff' },
  { value: 'C', label: 'C级 - 初阶混血', color: '#66bb6a' },
  { value: 'D', label: 'D级 - 觉醒者', color: '#8892b0' }
]

const profileRules = {
  nickname: [{ max: 10, message: '昵称最长10字', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载用户资料
async function loadProfile() {
  loading.value = true
  try {
    const res = await getProfile()
    profile.value = res.data
    // 填充编辑表单
    profileForm.nickname = res.data.nickname || ''
    profileForm.avatar = res.data.avatar || ''
    profileForm.signature = res.data.signature || ''
    profileForm.bloodlineGrade = res.data.bloodlineGrade || 'D'
    profileForm.faction = res.data.faction || ''
    profileForm.yanling = res.data.yanling || ''
  } catch (error) {
    console.error('加载资料失败:', error)
  } finally {
    loading.value = false
  }
}

// 保存资料
async function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.error('只能上传图片'); return false }
  if (!isLt2M) { ElMessage.error('图片不能超过2MB'); return false }
  return true
}

async function handleAvatarUpload(options) {
  try {
    const res = await uploadAvatar(options.file)
    const avatarUrl = res.data
    profileForm.avatar = avatarUrl
    profile.value.avatar = avatarUrl
    userStore.updateUserInfo({ avatar: avatarUrl })
    await updateProfile({ avatar: avatarUrl })
    options.onSuccess(res)
    ElMessage.success('头像更新成功')
  } catch (err) {
    console.error('头像上传失败:', err)
    options.onError(err)
    ElMessage.error('上传失败，请检查网络或稍后重试')
  }
}

async function handleSaveProfile() {
  if (!profileFormRef.value) return
  try {
    const valid = await profileFormRef.value.validate().catch(() => false)
    if (!valid) return
  } catch { return }
  savingProfile.value = true
  try {
    await updateProfile({
      nickname: profileForm.nickname,
      avatar: profileForm.avatar,
      signature: profileForm.signature,
      bloodlineGrade: profileForm.bloodlineGrade,
      faction: profileForm.faction,
      yanling: profileForm.yanling
    })
    ElMessage.success('资料修改成功')
    // 更新本地用户信息
    userStore.updateUserInfo({
      nickname: profileForm.nickname,
      avatar: profileForm.avatar,
      yanling: profileForm.yanling
    })
    await loadProfile()
  } catch (error) {
    console.error('保存资料失败:', error)
  } finally {
    savingProfile.value = false
  }
}

// 修改密码
async function handleChangePassword() {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    savingPassword.value = true
    try {
      await updatePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } catch (error) {
      console.error('修改密码失败:', error)
    } finally {
      savingPassword.value = false
    }
  })
}

// 加载我的帖子
async function loadMyPosts() {
  loadingPosts.value = true
  try {
    const res = await getUserPosts(profile.value?.id, { current: postsCurrentPage.value, size: postsPageSize.value })
    myPosts.value = res.data?.records || []
    postsTotal.value = res.data?.total || 0
  } catch (e) {
    console.error('加载帖子失败', e)
  } finally {
    loadingPosts.value = false
  }
}

watch(activeTab, (tab) => {
  if (tab === 'posts') loadMyPosts()
})

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-view {
  max-width: 700px;
  margin: 0 auto;
}

/* 用户信息卡片 */
.profile-card {
  background: var(--glass-medium);
  backdrop-filter: var(--glass-medium-blur);
  -webkit-backdrop-filter: var(--glass-medium-blur);
  border: 1px solid var(--glass-medium-border);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.profile-header {
  display: flex;
  gap: var(--spacing-lg);
  align-items: flex-start;
}

.profile-info {
  flex: 1;
}

.info-name {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
  min-width: 0;
}

.nickname-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 1;
}

.grade-badge {
  padding: 2px 8px;
  border: 1px solid;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  white-space: nowrap;
}

.faction-text {
  flex-shrink: 0;
  white-space: nowrap;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 400;
}

.info-username {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: var(--spacing-sm);
}

.info-yanling {
  color: #ff9800;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: var(--spacing-sm);
}

.info-yanling .yanling-text {
  color: var(--accent-primary);
  text-shadow: 0 0 8px var(--accent-glow);
}

.info-signature {
  color: var(--text-primary);
  font-size: 14px;
  font-style: italic;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--glass-light);
  backdrop-filter: var(--glass-light-blur);
  -webkit-backdrop-filter: var(--glass-light-blur);
  border-left: 3px solid var(--accent-primary);
  border-radius: var(--radius-sm);
  margin-bottom: var(--spacing-sm);
}

.info-time {
  color: var(--text-muted);
  font-size: 12px;
}

/* 操作区 */
.action-area {
  background: var(--glass-medium);
  backdrop-filter: var(--glass-medium-blur);
  -webkit-backdrop-filter: var(--glass-medium-blur);
  border: 1px solid var(--glass-medium-border);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
}

.profile-form {
  max-width: 500px;
  padding-top: var(--spacing-md);
}

.avatar-upload-preview {
  width: 80px; height: 80px; border-radius: 50%; object-fit: cover;
  cursor: pointer; border: 2px dashed var(--border-color);
}
.avatar-upload-placeholder {
  width: 80px; height: 80px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  border: 2px dashed var(--border-color); cursor: pointer; color: var(--text-muted);
}
.avatar-upload-preview:hover, .avatar-upload-placeholder:hover {
  border-color: var(--accent-primary);
}
.empty-posts {
  padding: var(--spacing-xl) 0;
}
</style>
