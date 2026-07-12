<template>
  <div class="post-card scroll-reveal" @click="goDetail" ref="cardRef">
    <!-- 置顶标识 -->
    <div class="pin-badge" v-if="post.isTop === 1">
      <el-icon class="pin-icon"><Top /></el-icon>
      <span class="pin-text">置顶</span>
    </div>

    <!-- 卡片内容 -->
    <div class="card-body">
      <!-- 标题区 -->
      <div class="card-header">
        <span
          class="category-tag"
          :style="{ backgroundColor: getCategoryColor(post.category) + '20', color: getCategoryColor(post.category), borderColor: getCategoryColor(post.category) + '60' }"
        >
          {{ post.categoryDesc || getCategoryName(post.category) }}
        </span>
        <h3 class="post-title" v-html="highlightedTitle"></h3>
      </div>

      <!-- 搜索内容片段 -->
      <div v-if="keyword && post.content" class="card-snippet" v-html="highlightedSnippet"></div>

      <!-- 图片缩略图 -->
      <div v-if="post.images && post.images.length > 0" class="card-images">
        <img
          v-for="(img, index) in post.images.slice(0, 3)"
          :key="index"
          :src="img"
          :alt="`图片${index + 1}`"
          class="card-image"
          @error="(e) => { e.target.style.display = 'none' }"
        />
        <span v-if="post.images.length > 3" class="image-more">+{{ post.images.length - 3 }}</span>
      </div>

      <!-- 作者信息 -->
      <div class="card-author">
        <UserCard :user="authorUser" :size="24" show-name />
      </div>

      <!-- 底部信息 -->
      <div class="card-footer">
        <div class="stats">
          <span class="stat-item">
            <el-icon><View /></el-icon>
            <span>{{ post.viewCount || 0 }}</span>
          </span>
          <span class="stat-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ post.commentCount || 0 }}</span>
          </span>
        </div>
        <span class="post-time">{{ formatRelativeTime(post.createTime) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Top, View, ChatDotRound } from '@element-plus/icons-vue'
import { getCategoryColor, getCategoryName, formatRelativeTime, highlightText } from '../utils/format'
import UserCard from './UserCard.vue'

const props = defineProps({
  post: {
    type: Object,
    required: true,
    default: () => ({})
  },
  keyword: {
    type: String,
    default: ''
  }
})

const cardRef = ref(null)
let observer = null

const highlightedTitle = computed(() => {
  if (!props.keyword) return props.post.title || ''
  return highlightText(props.post.title, props.keyword)
})

const highlightedSnippet = computed(() => {
  if (!props.keyword || !props.post.content) return ''
  const snippet = props.post.content.length > 120
    ? props.post.content.substring(0, 120) + '...'
    : props.post.content
  return highlightText(snippet, props.keyword)
})

const authorUser = computed(() => ({
  id: props.post.userId,
  nickname: props.post.nickname,
  username: props.post.username,
  avatar: props.post.avatar,
  bloodlineGrade: props.post.bloodlineGrade,
  yanling: props.post.yanling,
  faction: props.post.faction
}))

const router = useRouter()

function goDetail() {
  router.push(`/post/${props.post.id}`)
}

onMounted(() => {
  if (cardRef.value) {
    const rect = cardRef.value.getBoundingClientRect()
    const viewportHeight = window.innerHeight || document.documentElement.clientHeight
    if (rect.top < viewportHeight + 100 && rect.bottom > -100) {
      cardRef.value.classList.add('visible')
    }
  }

  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible')
        }
      })
    },
    { threshold: 0.1, rootMargin: '0px 0px -50px 0px' }
  )
  if (cardRef.value) {
    observer.observe(cardRef.value)
  }
})

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
  }
})
</script>

<style scoped>
.post-card {
  position: relative;
  background: var(--glass-ultralight);
  backdrop-filter: var(--glass-ultralight-blur);
  -webkit-backdrop-filter: var(--glass-ultralight-blur);
  border: 1px solid var(--glass-ultralight-border);
  border-radius: var(--radius-xl);
  padding: 0;
  margin-bottom: var(--spacing-md);
  cursor: pointer;
  transition: all var(--transition-normal);
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25), 0 0 0 1px rgba(255, 255, 255, 0.04) inset;
  text-shadow: var(--text-shadow-glass);
}

.card-body {
  padding: var(--spacing-md-lg);
}

.post-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--accent-glow-soft), transparent);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.post-card:hover {
  border-color: var(--border-light);
  box-shadow: 
    0 8px 30px rgba(0, 0, 0, 0.4),
    0 0 20px var(--accent-glow-soft);
  transform: translateY(-6px);
}

.post-card:hover::before {
  opacity: 1;
}

/* 置顶标识 */
.pin-badge {
  position: absolute;
  top: 0;
  right: var(--spacing-md);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.15), rgba(255, 215, 0, 0.05));
  border: 1px solid rgba(255, 215, 0, 0.25);
  border-radius: 0 0 var(--radius-md) var(--radius-md);
  box-shadow: 
    0 2px 8px rgba(255, 215, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  z-index: 10;
  animation: pinGlow 3s ease-in-out infinite;
}

@keyframes pinGlow {
  0%, 100% {
    box-shadow: 
      0 2px 8px rgba(255, 215, 0, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.1);
  }
  50% {
    box-shadow: 
      0 2px 12px rgba(255, 215, 0, 0.18),
      inset 0 1px 0 rgba(255, 255, 255, 0.15);
  }
}

.pin-icon {
  font-size: 12px;
  color: var(--grade-s);
  filter: drop-shadow(0 0 4px var(--gold-glow));
}

.pin-text {
  color: var(--grade-s);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-shadow: 0 0 6px var(--gold-glow);
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm-md);
  margin-bottom: var(--spacing-sm-md);
}

/* 图片缩略图 */
.card-images {
  display: flex;
  gap: 4px;
  margin-bottom: var(--spacing-sm-md);
}

.card-image {
  width: calc((100% - 8px) / 3);
  height: 60px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.image-more {
  width: calc((100% - 8px) / 3);
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 600;
}

.category-tag {
  flex-shrink: 0;
  padding: 3px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 500;
  border: 1px solid;
  white-space: nowrap;
}

.post-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--transition-fast);
  line-height: 1.5;
}

.post-card:hover .post-title {
  color: var(--accent-primary);
}

/* 搜索内容片段 */
.card-snippet {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.6;
  margin-bottom: var(--spacing-sm-md);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-primary);
  border-radius: var(--radius-sm);
  border-left: 2px solid var(--border-light);
  overflow: hidden;
}

/* 搜索关键词高亮 */
:deep(.search-hl) {
  background: rgba(255, 215, 0, 0.25);
  color: #ffd700;
  font-weight: 600;
  padding: 1px 2px;
  border-radius: 2px;
}

/* 作者信息 */
.card-author {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm-md);
}

.author-name {
  color: var(--text-secondary);
  font-size: 13px;
}

/* 底部信息 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--spacing-sm-md);
  border-top: 1px solid var(--border-color);
}

.stats {
  display: flex;
  gap: var(--spacing-md-lg);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-muted);
  font-size: 12px;
}

.stat-item el-icon {
  font-size: 14px;
}

.post-time {
  color: var(--text-muted);
  font-size: 11px;
}

@media (max-width: 768px) {
  .post-card {
    border-radius: var(--radius-lg);
  }
  
  .card-body {
    padding: var(--spacing-md);
  }
  
  .post-title {
    font-size: 15px;
  }
  
  .pin-badge {
    padding: 3px 8px;
    right: var(--spacing-sm);
  }
  
  .pin-text {
    font-size: 10px;
  }
  
  .pin-icon {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .card-body {
    padding: var(--spacing-sm-md);
  }
  
  .post-card {
    margin-bottom: var(--spacing-sm);
  }
  
  .card-header {
    gap: var(--spacing-sm);
  }
  
  .category-tag {
    padding: 2px 8px;
    font-size: 11px;
  }
  
  .post-title {
    font-size: 14px;
  }
  
  .stats {
    gap: var(--spacing-md);
  }
  
  .stat-item {
    font-size: 11px;
  }
}
</style>