<template>
  <div class="chat-detail">
    <!-- 头部 -->
    <header class="cd-topbar">
      <button class="cd-back" @click="router.push('/chat')">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <UserCard :user="targetUser" :size="38" hide-send-msg />
      <span class="cd-name">{{ targetUser.nickname || targetUser.username }}</span>
    </header>

    <!-- 消息区 -->
    <div class="cd-body" ref="bodyRef">
      <div v-if="messages.length === 0" class="cd-empty-tip">暂无消息</div>
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="cd-row"
        :class="{ self: isSelf(msg) }"
        @contextmenu.prevent
        @mousedown="startMsgPress(msg)"
        @mouseup="cancelMsgPress"
        @mouseleave="cancelMsgPress"
        @touchstart="startMsgPress(msg)"
        @touchend="cancelMsgPress"
        @touchcancel="cancelMsgPress"
      >
        <UserCard v-if="!isSelf(msg)" :user="targetUser" :size="34" hide-send-msg class="cd-avatar"/>
        <div class="cd-bubble">
          <div class="cd-text">{{ msg.content }}</div>
          <div class="cd-time">{{ formatTime(msg.createTime) }}</div>
        </div>
        <el-avatar v-if="isSelf(msg)" :size="34" :src="userStore.userInfo?.avatar" class="cd-avatar"/>
      </div>
      <div ref="bottomRef"/>
    </div>

    <!-- 输入区 -->
    <footer class="cd-foot" ref="footRef">
      <input
        v-model="inputText"
        class="cd-input"
        placeholder="输入消息..."
        @keyup.enter="handleSend"
        @focus="onInputFocus"
      />
      <button class="cd-send" :disabled="!inputText.trim()" @click="handleSend">发送</button>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getChatHistory, sendPrivateMessage, deleteMessage } from '../api/message'
import { getProfile } from '../api/user'
import UserCard from '../components/UserCard.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const targetId = Number(route.params.targetId)
const targetUser = ref({})
const messages = ref([])
const inputText = ref('')
const bodyRef = ref(null)
const bottomRef = ref(null)
const footRef = ref(null)
let pollTimer = null
let knownIds = new Set()
let msgPressTimer = null
let msgPressTarget = null

function isSelf(msg) {
  return String(msg.senderId) === String(userStore.userInfo?.id)
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

function isNearBottom() {
  const el = bodyRef.value
  if (!el) return true
  return el.scrollHeight - el.scrollTop - el.clientHeight < 80
}

async function loadChat() {
  try {
    const userRes = await getProfile(targetId)
    targetUser.value = userRes.data || { nickname: '用户', username: '用户' }
  } catch {
    targetUser.value = { nickname: '用户', username: '用户' }
  }
  try {
    const chatRes = await getChatHistory(targetId)
    messages.value = chatRes.data || []
    knownIds = new Set(messages.value.map(m => m.id))
    await nextTick()
    scrollToBottom(false)
  } catch { /* */ }
}

async function pollNewMessages() {
  try {
    const chatRes = await getChatHistory(targetId)
    const latest = chatRes.data || []
    const newMsgs = latest.filter(m => !knownIds.has(m.id))
    if (newMsgs.length > 0) {
      const wasNearBottom = isNearBottom()
      newMsgs.forEach(m => knownIds.add(m.id))
      messages.value.push(...newMsgs)
      if (wasNearBottom) {
        await nextTick()
        scrollToBottom()
      }
    }
  } catch { /* silent */ }
}

// 输入框获焦时，延迟滚动保证在键盘上方
function onInputFocus() {
  setTimeout(() => {
    footRef.value?.scrollIntoView({ block: 'end', behavior: 'smooth' })
  }, 300)
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  inputText.value = ''
  try {
    const res = await sendPrivateMessage(targetId, text)
    const msgId = res.data
    messages.value.push({
      id: msgId,
      senderId: userStore.userInfo?.id,
      receiverId: targetId,
      content: text,
      createTime: new Date().toISOString()
    })
    knownIds.add(msgId)
    await nextTick()
    scrollToBottom()
  } catch { ElMessage.error('发送失败') }
}

function scrollToBottom(smooth = true) {
  bottomRef.value?.scrollIntoView({ behavior: smooth ? 'smooth' : 'instant' })
}

function startMsgPress(msg) {
  msgPressTarget = msg
  msgPressTimer = setTimeout(() => {
    handleDeleteMsg()
  }, 600)
}

function cancelMsgPress() {
  if (msgPressTimer) { clearTimeout(msgPressTimer); msgPressTimer = null }
}

async function handleDeleteMsg() {
  if (!msgPressTarget) return
  const msg = msgPressTarget
  try {
    await ElMessageBox.confirm('确定删除此消息？', '删除消息', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMessage(msg.id)
    knownIds.delete(msg.id)
    messages.value = messages.value.filter(m => m.id !== msg.id)
    ElMessage.success('已删除')
  } catch { /* cancelled */ }
  msgPressTarget = null
  msgPressTimer = null
}

onMounted(() => {
  loadChat()
  pollTimer = setInterval(pollNewMessages, 3000)

  // 键盘弹起时动态调整容器高度（安卓全屏兼容）
  const setVH = () => {
    const h = window.visualViewport ? window.visualViewport.height : window.innerHeight
    ;(document.querySelector('.chat-detail') || document.body).style.height = h + 'px'
  }
  setVH()
  window.visualViewport?.addEventListener('resize', setVH)
  window.addEventListener('resize', setVH)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
  if (msgPressTimer) clearTimeout(msgPressTimer)
})
</script>

<style scoped>
.chat-detail {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
  background: var(--glass-light);
  backdrop-filter: var(--glass-light-blur);
  -webkit-backdrop-filter: var(--glass-light-blur);
}

/* ===== 顶部栏 ===== */
.cd-topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  height: 56px;
  background: var(--glass-heavy);
  backdrop-filter: var(--glass-heavy-blur);
  -webkit-backdrop-filter: var(--glass-heavy-blur);
  border-bottom: 1px solid var(--glass-heavy-border);
  flex-shrink: 0;
  z-index: 2;
}
.cd-back {
  background: none;
  border: none;
  color: var(--text-primary, #333);
  cursor: pointer;
  padding: 6px;
  margin-left: -6px;
  border-radius: 6px;
  display: flex;
  transition: background 0.15s;
}
.cd-back:hover { background: var(--bg-hover, rgba(0,0,0,0.05)); }
.cd-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #111);
  flex: 1;
}

/* ===== 消息区 ===== */
.cd-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.cd-body::-webkit-scrollbar { width: 4px; }
.cd-body::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.12); border-radius: 2px; }

.cd-empty-tip {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted, #999);
  font-size: 13px;
}

.cd-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.cd-row.self { justify-content: flex-end; }
.cd-avatar { flex-shrink: 0; margin-top: 2px; }

.cd-bubble {
  max-width: 60%;
  padding: 10px 14px;
  border-radius: 6px 18px 18px 18px;
  background: var(--glass-medium);
  backdrop-filter: var(--glass-medium-blur);
  -webkit-backdrop-filter: var(--glass-medium-blur);
  border: 1px solid var(--glass-medium-border);
  position: relative;
}
.cd-row.self .cd-bubble {
  border-radius: 18px 6px 18px 18px;
  background: #95ec69;
  border-color: #95ec69;
  color: #1a1a1a;
}

.cd-row.self .cd-bubble .cd-time {
  color: rgba(0, 0, 0, 0.45);
}

.cd-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-primary, #333);
  word-break: break-word;
  white-space: pre-wrap;
}

.cd-time {
  font-size: 11px;
  color: var(--text-muted, #999);
  margin-top: 4px;
  text-align: right;
}

/* ===== 输入区 ===== */
.cd-foot {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: var(--glass-medium);
  backdrop-filter: var(--glass-medium-blur);
  -webkit-backdrop-filter: var(--glass-medium-blur);
  border-top: 1px solid var(--glass-medium-border);
  flex-shrink: 0;
  z-index: 2;
}
.cd-input {
  flex: 1;
  height: 38px;
  padding: 0 14px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  background: var(--glass-light);
  color: var(--text-primary, #333);
  font-family: inherit;
  transition: border-color 0.15s;
}
.cd-input:focus { border-color: #07c160; }
.cd-send {
  padding: 0 22px;
  height: 38px;
  border: none;
  border-radius: 6px;
  background: #07c160;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition: opacity 0.15s;
  font-family: inherit;
}
.cd-send:hover { opacity: 0.9; }
.cd-send:disabled { opacity: 0.4; cursor: not-allowed; }

/* ===== 移动端适配 ===== */
@media (max-width: 768px) {
  .cd-topbar { height: 50px; padding: 0 12px; }
  .cd-body { padding: 12px 14px; gap: 14px; }
  .cd-bubble { max-width: 75%; }
  .cd-foot { padding: 8px 12px; }
  .cd-send { padding: 0 16px; }
}

@media (max-width: 480px) {
  .cd-topbar { height: 46px; }
  .cd-bubble { max-width: 85%; padding: 8px 12px; }
  .cd-text { font-size: 15px; }
  .cd-foot { padding: 6px 10px; gap: 8px; }
}
</style>
