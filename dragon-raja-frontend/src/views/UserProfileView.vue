<template>
  <div class="user-profile-view" v-loading="loading">
    <div class="profile-card" v-if="profile">
      <div class="profile-header">
        <el-avatar :size="80" :src="profile.avatar">
          {{ (profile.nickname || profile.username)?.charAt(0) }}
        </el-avatar>
        <div class="profile-info">
          <div class="info-name">
            {{ profile.nickname || profile.username }}
            <span class="grade-badge" :style="{ color: getGradeColor(profile.bloodlineGrade), borderColor: getGradeColor(profile.bloodlineGrade) }">
              {{ profile.bloodlineGrade }}级
            </span>
            <span v-if="profile.faction" class="faction-text">{{ profile.faction }}</span>
          </div>
          <div class="info-username">@{{ profile.username }}</div>
          <div class="info-yanling" v-if="profile.yanling">🔥 言灵：<span class="yanling-text">{{ profile.yanling }}</span></div>
          <div class="info-signature" v-if="profile.signature">{{ profile.signature }}</div>
          <div class="info-time">注册时间：{{ formatDate(profile.createTime) }}</div>
        </div>
      </div>
    </div>

    <div class="posts-section" v-if="profile">
      <div class="section-title">TA 的帖子</div>
      <div v-if="posts.length === 0" class="empty-posts">
        <el-empty description="暂无帖子" />
      </div>
      <PostCard v-for="post in posts" :key="post.id" :post="post" />
      <Pagination v-if="total > 0" :current="currentPage" :total="total" :size="pageSize" @change="handlePageChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfile } from '../api/user'
import { getUserPosts } from '../api/post'
import { getGradeColor, formatDate } from '../utils/format'
import PostCard from '../components/PostCard.vue'
import Pagination from '../components/Pagination.vue'

const route = useRoute()
const loading = ref(false)
const profile = ref(null)
const posts = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

async function loadProfile() {
  loading.value = true
  try {
    const userId = route.params.id
    const res = await getProfile(userId)
    profile.value = res.data
    await loadPosts()
  } catch (e) {
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

async function loadPosts() {
  try {
    const res = await getUserPosts(profile.value.id, { current: currentPage.value, size: pageSize.value })
    posts.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载帖子失败', e)
  }
}

function handlePageChange(page) {
  currentPage.value = page
  loadPosts()
}

onMounted(loadProfile)
</script>

<style scoped>
.user-profile-view { padding-bottom: var(--spacing-xl); }

.profile-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.profile-header {
  display: flex;
  gap: var(--spacing-lg);
}

.profile-info { flex: 1; }

.info-name {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
  min-width: 0;
}

.info-name > :first-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.grade-badge {
  padding: 1px 8px;
  border: 1px solid;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  white-space: nowrap;
}

.faction-text { color: var(--text-secondary); font-size: 14px; flex-shrink: 0; white-space: nowrap; }

.info-username { color: var(--text-secondary); font-size: 14px; margin-bottom: var(--spacing-sm); }

.info-yanling {
  color: var(--warning);
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
  background: var(--bg-primary);
  border-left: 3px solid var(--accent-primary);
  border-radius: var(--radius-sm);
  margin-bottom: var(--spacing-sm);
}

.info-time { color: var(--text-muted); font-size: 12px; }

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.empty-posts { padding: var(--spacing-xl) 0; }
</style>
