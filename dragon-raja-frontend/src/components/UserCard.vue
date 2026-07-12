<template>
  <el-popover
    placement="bottom"
    trigger="click"
    :width="280"
    :popper-options="{ strategy: 'fixed' }"
    popper-class="user-card-popover"
  >
    <template #reference>
      <span class="user-card-trigger">
        <el-avatar :size="size" :src="user.avatar" class="trigger-avatar">
          {{ (user.nickname || user.username)?.charAt(0) }}
        </el-avatar>
        <span v-if="showName" class="trigger-name">{{ user.nickname || user.username }}</span>
      </span>
    </template>

    <div class="user-card-body">
      <div class="card-header">
        <el-avatar :size="48" :src="user.avatar">
          {{ (user.nickname || user.username)?.charAt(0) }}
        </el-avatar>
        <div class="card-info">
          <div class="card-name">
            {{ user.nickname || user.username }}
            <span v-if="user.faction" class="card-faction">{{ user.faction }}</span>
          </div>
          <div class="card-grade" :style="{ color: getGradeColor(user.bloodlineGrade) }">
            {{ user.bloodlineGrade }}级混血种
          </div>
        </div>
      </div>

      <div class="card-details">
        <div class="detail-row" v-if="user.yanling">
          <span class="detail-label">言灵</span>
          <span class="detail-value yanling-value">{{ user.yanling }}</span>
        </div>
        <div class="detail-row" v-if="user.signature">
          <span class="detail-label">签名</span>
          <span class="detail-value">{{ user.signature }}</span>
        </div>
      </div>

      <div class="card-actions" :class="{ centered: hideSendMsg && !isSelf }">
        <button class="card-action-btn" v-if="!isSelf && !hideSendMsg" @click.stop="handleSendMsg">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          发私信
        </button>
        <button class="card-action-btn outline" v-if="!isSelf && !isAlreadyFriend" @click.stop="handleAddFriend" :disabled="friendApplied">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
          {{ friendBtnText }}
        </button>
      </div>
      <div class="card-footer">
        <a class="card-link" @click="goProfile">查看个人信息 →</a>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGradeColor, formatDate } from '../utils/format'
import { useUserStore } from '../stores/user'
import request from '../api/request'
import { checkFriend } from '../api/friend'

const props = defineProps({
  user: { type: Object, required: true },
  size: { type: Number, default: 32 },
  showName: { type: Boolean, default: false },
  hideSendMsg: { type: Boolean, default: false }
})

const router = useRouter()
const userStore = useUserStore()
const friendApplied = ref(false)
const isAlreadyFriend = ref(false)

onMounted(async () => {
  if (props.user?.id && !isSelf.value) {
    try {
      const res = await checkFriend(props.user.id)
      isAlreadyFriend.value = res.data?.isFriend || false
    } catch { /* ignore */ }
  }
})

const isSelf = computed(() => {
  const uid = userStore.userInfo?.id
  const pid = props.user?.id
  if (!uid || !pid) return false
  return String(uid) === String(pid)
})
const friendBtnText = computed(() => friendApplied.value ? '已申请' : '添加好友')

function goProfile() {
  if (props.user.id) {
    router.push(`/user/${props.user.id}`)
  }
}

function handleSendMsg() {
  router.push(`/chat/${props.user.id}`)
}

async function handleAddFriend() {
  if (friendApplied.value) return
  try {
    await request({ url: '/friend/apply', method: 'post', data: { friendId: props.user.id } })
    friendApplied.value = true
    ElMessage.success('好友申请已发送')
  } catch { /* */ }
}
</script>

<style scoped>
.user-card-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.trigger-avatar { flex-shrink: 0; }
.trigger-name { font-size: 13px; color: var(--text-secondary); }

.user-card-body {
  padding: 4px 0;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}
.card-info { flex: 1; min-width: 0; }
.card-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}
.card-faction {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 6px;
}
.card-grade {
  font-size: 13px;
  font-weight: 600;
}

.card-details { margin-bottom: 12px; }
.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 4px 0;
  font-size: 13px;
}
.detail-label { color: var(--text-muted); flex-shrink: 0; margin-right: 12px; }
.detail-value { color: var(--text-secondary); text-align: right; }
.yanling-value { color: var(--warning); font-weight: 500; }

/* 操作按钮 */
.card-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}
.card-actions.centered {
  justify-content: center;
}
.card-action-btn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 7px 10px;
  border: none;
  border-radius: 7px;
  background: var(--accent-primary);
  color: #061327;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity var(--transition-fast), transform var(--transition-fast);
  line-height: 1;
}
.card-action-btn:hover { opacity: 0.85; transform: translateY(-1px); }
.card-action-btn:active { transform: translateY(0); }
.card-action-btn.outline {
  background: transparent;
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
}
.card-action-btn.outline:hover {
  border-color: var(--accent-primary);
  color: var(--accent-primary);
  opacity: 1;
}
.card-action-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

.card-footer {
  text-align: right;
  padding-top: 8px;
  border-top: 1px solid var(--border-color);
}
.card-link {
  color: var(--accent-primary);
  font-size: 13px;
  cursor: pointer;
  text-decoration: none;
  transition: opacity var(--transition-fast);
}
.card-link:hover {
  opacity: 0.8;
  text-decoration: underline;
}
</style>

<style>
.user-card-popover {
  z-index: 10000 !important;
}
</style>
