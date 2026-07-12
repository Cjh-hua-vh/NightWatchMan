<template>
  <div class="post-detail-view" v-loading="loading">
    <!-- 粘性返回栏 -->
    <div class="sticky-back-bar">
      <div class="back-btn" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
    </div>

    <!-- 帖子内容 -->
    <div class="post-content-card" v-if="post">
      <!-- 帖子头部 -->
      <div class="post-header">
        <!-- 标签行 -->
        <div class="post-tags-row">
          <span
            class="category-tag"
            :style="{ backgroundColor: getCategoryColor(post.category) + '15', color: getCategoryColor(post.category), borderColor: getCategoryColor(post.category) + '40' }"
          >
            {{ post.categoryDesc || getCategoryName(post.category) }}
          </span>
          <span v-if="post.isTop === 1" class="top-tag">置顶</span>
        </div>

        <!-- 标题 -->
        <h1 class="post-title">{{ post.title }}</h1>

        <!-- 作者信息和操作区 -->
        <div class="post-meta">
          <div class="author-info">
            <UserCard :user="detailAuthorUser" :size="40" />
            <div class="author-detail">
              <div class="author-name">
                {{ post.nickname || post.username }}
                <span
                  class="grade-badge"
                  :style="{ color: getGradeColor(post.bloodlineGrade), borderColor: getGradeColor(post.bloodlineGrade) }"
                >
                  {{ post.bloodlineGrade }}级
                </span>
              </div>
              <div class="author-tags">
                <span v-if="post.faction" class="faction-tag">{{ post.faction }}</span>
                <span v-if="post.yanling" class="yanling-tag">{{ formatYanling(post.yanling) }}</span>
              </div>
            </div>
          </div>

          <div class="post-actions" v-if="canEdit">
            <el-button size="small" plain @click="handleEdit">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" plain @click="handleDelete">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>

        <!-- 统计信息行 -->
        <div class="post-stats">
          <span class="stat-item">
            <el-icon><View /></el-icon>
            <span>{{ post.viewCount || 0 }}</span>
          </span>
          <span class="stat-divider"></span>
          <span class="stat-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ post.commentCount || 0 }}</span>
          </span>
          <span class="stat-divider"></span>
          <span class="stat-time">{{ formatDateTime(post.createTime) }}</span>
        </div>
      </div>

      <!-- 帖子图片 -->
      <div v-if="post.images && post.images.length > 0" class="post-images">
        <div
          v-for="(img, index) in post.images"
          :key="index"
          class="post-image-item"
          @click="previewImage(img)"
        >
          <img :src="img" :alt="`图片${index + 1}`" class="post-image" @error="(e) => { e.target.style.display = 'none' }" />
        </div>
      </div>

      <!-- 帖子正文 -->
      <div class="post-body">
        <pre class="post-text">{{ post.content }}</pre>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="comment-card" v-if="post">
      <div class="comment-header">
        <el-icon><ChatDotSquare /></el-icon>
        <span>评论区</span>
      </div>
      <CommentSection ref="commentSectionRef" :post-id="postId" />
    </div>

    <!-- 图片预览弹窗 -->
    <div v-if="previewVisible" class="image-preview-mask" @click="closePreview">
      <div class="image-preview-container" @click.stop>
        <img :src="previewImageUrl" alt="预览图片" class="preview-image" />
        <div class="preview-close" @click="closePreview">
          <el-icon><Close /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, View, ChatDotRound, ChatDotSquare, Edit, Delete, Close
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getPostDetail, deletePost } from '../api/post'
import { getCategoryColor, getCategoryName, getGradeColor, formatDateTime, formatYanling } from '../utils/format'
import UserCard from '../components/UserCard.vue'
import CommentSection from '../components/CommentSection.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const postId = route.params.id
const post = ref(null)

const detailAuthorUser = computed(() => {
  const p = post.value
  if (!p) return {}
  return { id: p.userId, nickname: p.nickname, username: p.username, avatar: p.avatar, bloodlineGrade: p.bloodlineGrade, yanling: p.yanling, faction: p.faction }
})
const loading = ref(false)
const previewVisible = ref(false)
const previewImageUrl = ref('')
const commentSectionRef = ref(null)

// 是否可以编辑/删除
const canEdit = computed(() => {
  return post.value && userStore.userInfo.id === post.value.userId
})

// 加载帖子详情
async function loadPostDetail() {
  loading.value = true
  try {
    const res = await getPostDetail(postId)
    post.value = res.data
    // 滚动到指定评论
    const commentId = route.query.comment
    if (commentId) {
      nextTick(() => {
        setTimeout(() => {
          commentSectionRef.value?.scrollToComment(Number(commentId))
        }, 500)
      })
    }
  } catch (error) {
    console.error('加载帖子详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 编辑帖子
function handleEdit() {
  router.push(`/post/create?id=${postId}`)
}

// 删除帖子
function handleDelete() {
  ElMessageBox.confirm('确定要删除这篇帖子吗？删除后不可恢复。', '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deletePost(postId)
        ElMessage.success('删除成功')
        router.push('/')
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {})
}

function previewImage(url) {
  previewImageUrl.value = url
  previewVisible.value = true
}

function closePreview() {
  previewVisible.value = false
  previewImageUrl.value = ''
}

onMounted(() => {
  loadPostDetail()
})
</script>

<style scoped>
.post-detail-view {
  max-width: 800px;
  margin: 0 auto;
}

.sticky-back-bar {
  position: sticky;
  top: 64px;
  z-index: 100;
  display: flex;
  align-items: center;
  padding: 8px 0;
  margin-bottom: var(--spacing-md);
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 6px 14px;
  background: rgba(10, 14, 39, 0.85);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 14px;
  transition: all var(--transition-fast);
}

.back-btn:hover {
  color: var(--accent-primary);
  border-color: var(--accent-primary);
}

.post-content-card {
  background: rgba(10, 14, 39, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3);
}

.post-header {
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px solid var(--border-color);
  margin-bottom: var(--spacing-lg);
}

.post-tags-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.category-tag {
  padding: 3px 12px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  border: 1px solid;
  white-space: nowrap;
}

.top-tag {
  padding: 3px 10px;
  background: rgba(255, 215, 0, 0.12);
  border: 1px solid rgba(255, 215, 0, 0.25);
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 215, 0, 0.8);
}

.post-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: var(--spacing-lg);
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-md);
}

.author-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.author-detail {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.author-name {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 600;
}

.grade-badge {
  padding: 1px 8px;
  border: 1px solid;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 700;
}

.author-tags {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.faction-tag {
  padding: 1px 6px;
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.15);
  border-radius: var(--radius-sm);
  font-size: 11px;
  color: rgba(0, 212, 255, 0.7);
}

.yanling-tag {
  padding: 1px 6px;
  background: rgba(255, 152, 0, 0.08);
  border: 1px solid rgba(255, 152, 0, 0.15);
  border-radius: var(--radius-sm);
  font-size: 11px;
  color: rgba(255, 152, 0, 0.7);
}

.post-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.post-stats {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding-top: var(--spacing-sm);
  border-top: 1px dashed var(--border-color);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 13px;
}

.stat-divider {
  width: 1px;
  height: 12px;
  background: var(--border-color);
}

.stat-time {
  color: var(--text-muted);
  font-size: 12px;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.post-image-item {
  width: calc(50% - 8px);
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.post-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.post-body {
  color: var(--text-primary);
  line-height: 1.9;
}

.post-text {
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 15px;
  line-height: 1.9;
}

.comment-card {
  background: rgba(10, 14, 39, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--accent-primary);
  font-size: 16px;
  font-weight: 600;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border-color);
}

@media (max-width: 768px) {
  .post-content-card, .comment-card {
    border-radius: 0;
    padding: var(--spacing-md);
  }
  .post-title {
    font-size: 20px;
  }
  .post-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
}

.image-preview-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  cursor: zoom-out;
}

.image-preview-container {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  cursor: default;
}

.preview-image {
  max-width: 100%;
  max-height: 85vh;
  object-fit: contain;
  border-radius: var(--radius-sm);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

.preview-close {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  color: #fff;
  cursor: pointer;
  transition: all var(--transition-fast);
  z-index: 10;
}

.preview-close:hover {
  background: rgba(255, 255, 255, 0.2);
  color: var(--accent-primary);
}

.post-image-item {
  cursor: zoom-in;
  transition: transform var(--transition-fast);
}

.post-image-item:hover {
  transform: scale(1.02);
}
</style>
