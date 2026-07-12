<template>
  <div class="comment-section">
    <!-- 评论输入框 -->
    <div class="comment-input-area" v-if="userStore.isLoggedIn">
      <el-avatar :size="36" :src="userStore.userInfo.avatar">
        {{ userStore.userInfo.nickname?.charAt(0) }}
      </el-avatar>
      <div class="input-wrapper">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="发表你的看法..."
          maxlength="1000"
          show-word-limit
          resize="none"
        />
        <div class="input-actions">
          <el-button type="primary" :loading="submitting" :disabled="submitting" @click="submitComment">
            发表评论
          </el-button>
        </div>
      </div>
    </div>
    <div class="login-tip" v-else>
      <span>登录后即可参与讨论</span>
      <el-button type="primary" size="small" @click="router.push('/login')">去登录</el-button>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <div class="comment-count">
        共 {{ total }} 条评论
      </div>

      <div v-if="comments.length === 0 && !loading" class="empty-comments">
        <el-empty description="暂无评论，快来抢沙发吧" />
      </div>

      <div v-for="comment in comments" :key="comment.id" :data-comment-id="comment.id" class="comment-item">
        <UserCard :user="commentUser(comment)" :size="36" />
        <div class="comment-content" @click="userStore.isLoggedIn && toggleReply(comment)">
          <div class="comment-header">
            <span class="comment-author">{{ comment.nickname || comment.username }}</span>
            <span
              class="grade-badge"
              :style="{ color: getGradeColor(comment.bloodlineGrade), borderColor: getGradeColor(comment.bloodlineGrade) }"
            >
              {{ comment.bloodlineGrade }}级
            </span>
            <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
            <el-button
              v-if="canDelete(comment)"
              type="danger"
              size="small"
              text
              @click.stop="handleDelete(comment)"
            >
              删除
            </el-button>
          </div>
          <div class="comment-text">
            <span v-if="comment.replyToNickname" class="reply-mention">@{{ comment.replyToNickname }} </span>
            {{ comment.content }}
          </div>
          
          <!-- 回复输入框 -->
          <div v-if="replyExpandedId === comment.id" class="reply-input-area" @click.stop>
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="2"
              :placeholder="`回复 ${comment.nickname || comment.username}...`"
              maxlength="500"
              show-word-limit
              resize="none"
              class="reply-input"
              @keyup.enter.exact="submitReply(comment)"
            />
            <div class="reply-actions">
              <el-button size="small" @click="cancelReply">取消</el-button>
              <el-button type="primary" size="small" :loading="replying" :disabled="replying" @click="submitReply(comment)">发送</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <Pagination
      v-if="total > 0"
      :current="currentPage"
      :total="total"
      :size="pageSize"
      @change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getComments, createComment, deleteComment, adminDeleteComment } from '../api/comment'
import { getGradeColor, formatRelativeTime } from '../utils/format'
import UserCard from './UserCard.vue'
import Pagination from './Pagination.vue'

const props = defineProps({
  postId: {
    type: [Number, String],
    required: true
  }
})

const router = useRouter()
const userStore = useUserStore()

const comments = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const commentContent = ref('')
const submitting = ref(false)
const replyExpandedId = ref(null)
const replyContent = ref('')
const replying = ref(false)

// 加载评论列表
async function loadComments() {
  loading.value = true
  try {
    const res = await getComments(props.postId, {
      current: currentPage.value,
      size: pageSize.value
    })
    comments.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    loading.value = false
  }
}

// 发表评论
async function submitComment() {
  if (submitting.value) return  // 防止重复提交
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    await createComment({
      postId: props.postId,
      content: commentContent.value.trim()
    })
    ElMessage.success('评论发表成功')
    commentContent.value = ''
    currentPage.value = 1
    await loadComments()
  } catch (error) {
    console.error('发表评论失败:', error)
  } finally {
    submitting.value = false
  }
}

// 删除评论
function handleDelete(comment) {
  ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        // 管理员删除别人评论走admin接口
        if (userStore.isAdmin && userStore.userInfo.id !== comment.userId) {
          await adminDeleteComment(comment.id)
        } else {
          await deleteComment(comment.id)
        }
        ElMessage.success('删除成功')
        await loadComments()
      } catch (error) {
        console.error('删除评论失败:', error)
      }
    })
    .catch(() => {})
}

// 判断是否可以删除评论
const emit = defineEmits(['delete-comment'])

function commentUser(c) {
  return { id: c.userId, nickname: c.nickname, username: c.username, avatar: c.avatar, bloodlineGrade: c.bloodlineGrade }
}

function canDelete(comment) {
  // 评论者本人可删，管理员可删任意评论
  return userStore.userInfo.id === comment.userId || userStore.isAdmin
}

// 分页变化
function handlePageChange(page) {
  currentPage.value = page
  loadComments()
}

// 切换回复输入框
function toggleReply(comment) {
  if (replyExpandedId.value === comment.id) {
    replyExpandedId.value = null
    replyContent.value = ''
  } else {
    replyExpandedId.value = comment.id
    replyContent.value = ''
  }
}

// 取消回复
function cancelReply() {
  replyExpandedId.value = null
  replyContent.value = ''
}

// 提交回复
async function submitReply(comment) {
  if (replying.value) return
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replying.value = true
  try {
    await createComment({
      postId: props.postId,
      content: replyContent.value.trim(),
      replyToId: comment.id
    })
    ElMessage.success('回复成功')
    replyContent.value = ''
    replyExpandedId.value = null
    currentPage.value = 1
    await loadComments()
  } catch (error) {
    console.error('回复失败:', error)
  } finally {
    replying.value = false
  }
}

onMounted(() => {
  loadComments()
})

async function scrollToComment(commentId) {
  // 等待评论渲染完成
  await nextTick()
  const el = document.querySelector(`[data-comment-id="${commentId}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el.classList.add('comment-highlight')
    setTimeout(() => el.classList.remove('comment-highlight'), 2000)
  }
}

defineExpose({ scrollToComment })
</script>

<style scoped>
.comment-section {
  margin-top: var(--spacing-lg);
}

/* 评论输入区 */
.comment-input-area {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.input-wrapper {
  flex: 1;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-sm);
}

/* 登录提示 */
.login-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--glass-medium);
  backdrop-filter: var(--glass-medium-blur);
  -webkit-backdrop-filter: var(--glass-medium-blur);
  border: 1px solid var(--glass-medium-border);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-lg);
  color: var(--text-secondary);
}

/* 评论列表 */
.comment-count {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--border-color);
}

.empty-comments {
  padding: var(--spacing-lg) 0;
}

.comment-item {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--border-color);
  transition: background-color var(--transition-fast);
}

.comment-item:hover {
  background-color: var(--bg-hover);
  margin: 0 calc(-1 * var(--spacing-md));
  padding-left: var(--spacing-md);
  padding-right: var(--spacing-md);
  border-radius: var(--radius-sm);
}

.comment-item.comment-highlight {
  animation: commentPulse 0.6s ease-out 3;
}

@keyframes commentPulse {
  0%, 100% { background-color: transparent; }
  50% { background-color: rgba(0, 212, 255, 0.12); }
}

.comment-content {
  flex: 1;
  cursor: pointer;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.comment-author {
  color: var(--accent-secondary);
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.grade-badge {
  padding: 1px 6px;
  border: 1px solid;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
  white-space: nowrap;
}

.comment-time {
  color: var(--text-muted);
  font-size: 12px;
}

.comment-text {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.reply-mention {
  color: var(--accent-primary);
  font-weight: 500;
}

.reply-input-area {
  margin-top: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
}

.reply-input :deep(.el-input__wrapper) {
  background: var(--bg-primary);
  border-color: var(--border-color);
}

.reply-input :deep(.el-input__inner) {
  color: var(--text-primary);
  font-size: 13px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-sm);
}

@media (max-width: 768px) {
  .reply-input-area {
    padding: var(--spacing-sm-md);
  }
}
</style>
