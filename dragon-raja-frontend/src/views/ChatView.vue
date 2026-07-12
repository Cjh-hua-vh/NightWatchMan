<template>
  <div class="chat-view">
    <div class="chat-container">
      <!-- 左侧：会话列表 -->
      <aside class="chat-sidebar" :class="{ collapsed: activeTarget }">
        <div class="sidebar-header">
          <h2 class="sidebar-title">
            <el-icon><ChatDotRound /></el-icon>
            私信
          </h2>
          <button class="refresh-btn" @click="loadSessions" :loading="loading">
            <el-icon><Refresh /></el-icon>
          </button>
        </div>
        
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="5" animated />
        </div>
        
        <div v-else-if="sessions.length === 0" class="empty-sessions">
          <el-empty description="暂无私信会话" :image-size="0" />
        </div>
        
        <div v-else class="session-list" ref="sessionListRef">
          <div
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: activeTarget === getTargetId(session), 'is-pressed': pressedSession?.id === session.id }"
            @click="selectSession(session)"
            @contextmenu.prevent
            @mousedown="startPress(session, $event)"
            @mouseup="cancelPress"
            @mouseleave="cancelPress"
            @touchstart="startPress(session, $event)"
            @touchend="cancelPress"
            @touchcancel="cancelPress"
          >
            <div class="session-avatar" @click.stop>
              <UserCard :user="getTargetUser(session)" :size="44" hide-send-msg />
              <span v-if="session.unreadCount > 0" class="unread-badge">{{ session.unreadCount }}</span>
            </div>
            <div class="session-info">
              <div class="session-name">{{ getSessionName(session) }}</div>
              <div class="session-preview">{{ session.content }}</div>
            </div>
            <div class="session-time">{{ formatTime(session.createTime) }}</div>
          </div>
        </div>
      </aside>

      <!-- 分隔线 -->
      <div class="chat-divider" v-if="activeTarget"></div>

      <!-- 右侧：聊天区域 -->
      <main class="chat-area" :class="{ active: activeTarget }">
        <template v-if="!activeTarget">
          <div class="chat-empty">
            <el-icon class="empty-icon"><ChatDotSquare /></el-icon>
            <p>选择一个会话开始聊天</p>
          </div>
        </template>
        <template v-else>
          <!-- 聊天头部 -->
          <div class="chat-header">
            <div class="chat-header-left">
              <UserCard :user="chatUser" :size="38" hide-send-msg />
              <div class="chat-header-info">
                <span class="chat-title">{{ chatUser.nickname || chatUser.username }}</span>
              </div>
            </div>
            <span class="chat-collapse" @click="activeTarget = null">收起</span>
          </div>

          <!-- 消息区 -->
          <div class="chat-messages" ref="bodyRef">
            <div v-if="chatMessages.length === 0" class="chat-empty-tip">暂无消息，发送第一条吧</div>
            <div
              v-for="msg in chatMessages"
              :key="msg.id"
              class="cd-row"
              :class="{ self: isChatSelf(msg) }"
              @contextmenu.prevent
              @mousedown="startMsgPress(msg)"
              @mouseup="cancelMsgPress"
              @mouseleave="cancelMsgPress"
              @touchstart="startMsgPress(msg)"
              @touchend="cancelMsgPress"
              @touchcancel="cancelMsgPress"
            >
              <UserCard v-if="!isChatSelf(msg)" :user="chatUser" :size="34" hide-send-msg class="cd-avatar" />
              <div class="cd-bubble">
                <div class="cd-text">{{ msg.content }}</div>
                <div class="cd-time">{{ formatChatTime(msg.createTime) }}</div>
              </div>
              <el-avatar v-if="isChatSelf(msg)" :size="34" :src="userStore.userInfo?.avatar" class="cd-avatar"/>
            </div>
            <div ref="bottomRef"/>
          </div>

          <!-- 输入区 -->
          <div class="chat-input-area">
            <input
              v-model="chatInputText"
              class="cd-input"
              placeholder="输入消息..."
              @keyup.enter="handleChatSend"
            />
            <button class="cd-send" :disabled="!chatInputText.trim()" @click="handleChatSend">发送</button>
          </div>
        </template>
      </main>
    </div>

    <!-- 长按操作菜单 -->
    <Teleport to="body">
      <div
        v-if="actionMenuVisible"
        class="action-sheet-overlay"
        tabindex="0"
        @click.self="closeActionMenu"
        @keydown.escape="closeActionMenu"
      >
        <div class="action-sheet" @click.stop>
          <div class="action-item action-hide" @click="hideSession">
            <el-icon><Hide /></el-icon>
            <span>不显示</span>
          </div>
          <div class="action-item action-delete" @click="deleteSessionAction">
            <el-icon><Delete /></el-icon>
            <span>删除</span>
          </div>
          <div class="action-item action-cancel" @click="closeActionMenu">
            <span>取消</span>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ChatDotRound, Refresh, ChatDotSquare, Hide, Delete } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getSessionList, deleteSession, getChatHistory, sendPrivateMessage, deleteMessage } from '../api/message'
import { getProfile } from '../api/user'
import { formatDateTime } from '../utils/format'
import UserCard from '../components/UserCard.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ====== 会话列表 ======
const sessions = ref([])
const loading = ref(false)
const sessionListRef = ref(null)
let pollTimer = null
let pressTimer = null
let wasLongPress = false
const pressedSession = ref(null)
const actionMenuVisible = ref(false)

// ====== 聊天面板 ======
const activeTarget = ref(null)
const chatUser = ref({})
const chatMessages = ref([])
const chatInputText = ref('')
const bodyRef = ref(null)
const bottomRef = ref(null)
let chatPollTimer = null
let knownIds = new Set()
let msgPressTimer = null
let msgPressTarget = null

// ====== 隐藏会话 ======
const HIDDEN_KEY = 'chat_hidden_sessions'
function loadHiddenKeys() {
  try { const raw = localStorage.getItem(HIDDEN_KEY); return raw ? new Set(JSON.parse(raw)) : new Set() }
  catch { return new Set() }
}
function saveHiddenKeys(set) { localStorage.setItem(HIDDEN_KEY, JSON.stringify([...set])) }
function getSessionKey(session) {
  const uid = String(userStore.userInfo?.id)
  return `${uid}_${getTargetId(session)}`
}

// ====== 辅助函数 ======
function getTargetId(session) {
  const senderId = Number(session.senderId)
  const receiverId = Number(session.receiverId)
  const myId = Number(userStore.userInfo?.id)
  return (senderId === myId && !Number.isNaN(myId)) ? receiverId : senderId
}
function getSessionName(session) {
  const targetId = getTargetId(session)
  if (String(targetId) === String(session.senderId)) return session.senderName || session.sender || '未知用户'
  return session.receiverName || '未知用户'
}
function getSessionAvatar(session) { return session?.avatar || null }
function getTargetUser(session) {
  const targetId = getTargetId(session)
  return { id: targetId, nickname: getSessionName(session), avatar: getSessionAvatar(session) }
}
function formatTime(time) { if (!time) return ''; return formatDateTime(time) }
function formatChatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  return `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
function isChatSelf(msg) { return String(msg.senderId) === String(userStore.userInfo?.id) }

// ====== 会话列表逻辑 ======
function selectSession(session) {
  if (wasLongPress) { wasLongPress = false; return }
  if (actionMenuVisible.value) { closeActionMenu(); return }
  const targetId = getTargetId(session)
  // 移动端跳转独立页面，桌面端右侧展示
  if (window.innerWidth <= 768) {
    router.push(`/chat/${targetId}`)
  } else {
    openChat(targetId)
  }
}

async function loadSessions(showLoading = true) {
  if (showLoading) loading.value = true
  try {
    const res = await getSessionList()
    const hiddenKeys = loadHiddenKeys()
    const all = res.data || []
    sessions.value = all.filter(s => {
      const key = getSessionKey(s)
      if (hiddenKeys.has(key)) {
        if (s.unreadCount && s.unreadCount > 0) {
          hiddenKeys.delete(key); saveHiddenKeys(hiddenKeys); return true
        }
        return false
      }
      return true
    })
    // 如果当前打开的用户在列表中，同步未读数
    if (activeTarget.value) {
      const cur = sessions.value.find(s => getTargetId(s) === activeTarget.value)
      if (cur && cur.unreadCount) {
        // 当前聊天会话有未读（别人新发的），不清零
      }
    }
  } catch (error) { console.error('加载会话失败:', error) }
  finally { if (showLoading) loading.value = false }
}

// ====== 聊天面板逻辑 ======
function isNearBottom() {
  const el = bodyRef.value; if (!el) return true
  return el.scrollHeight - el.scrollTop - el.clientHeight < 80
}
function scrollToBottom() { bottomRef.value?.scrollIntoView({ behavior: 'instant' }) }

async function openChat(targetId) {
  activeTarget.value = targetId
  chatMessages.value = []
  chatInputText.value = ''
  knownIds = new Set()
  if (chatPollTimer) clearInterval(chatPollTimer)
  
  try {
    const userRes = await getProfile(targetId)
    chatUser.value = userRes.data || { nickname: '用户', username: '用户' }
  } catch { chatUser.value = { nickname: '用户', username: '用户' } }
  
  try {
    const chatRes = await getChatHistory(targetId)
    chatMessages.value = chatRes.data || []
    knownIds = new Set(chatMessages.value.map(m => m.id))
    await nextTick(); scrollToBottom()
  } catch { /* */ }
  
  chatPollTimer = setInterval(pollChatMessages, 3000)
}

async function pollChatMessages() {
  if (!activeTarget.value) return
  try {
    const res = await getChatHistory(activeTarget.value)
    const latest = res.data || []
    const newMsgs = latest.filter(m => !knownIds.has(m.id))
    if (newMsgs.length > 0) {
      const wasNear = isNearBottom()
      newMsgs.forEach(m => knownIds.add(m.id))
      chatMessages.value.push(...newMsgs)
      if (wasNear) { await nextTick(); scrollToBottom() }
    }
  } catch { /* */ }
}

async function handleChatSend() {
  const text = chatInputText.value.trim()
  if (!text) return
  chatInputText.value = ''
  try {
    const res = await sendPrivateMessage(activeTarget.value, text)
    const msgId = res.data
    chatMessages.value.push({
      id: msgId, senderId: userStore.userInfo?.id,
      receiverId: activeTarget.value, content: text,
      createTime: new Date().toISOString()
    })
    knownIds.add(msgId)
    await nextTick(); scrollToBottom()
    loadSessions(false) // 刷新会话列表
  } catch { ElMessage.error('发送失败') }
}

// ====== 消息长按删除 ======
function startMsgPress(msg) {
  msgPressTarget = msg
  msgPressTimer = setTimeout(() => handleDeleteMsg(), 600)
}
function cancelMsgPress() { if (msgPressTimer) { clearTimeout(msgPressTimer); msgPressTimer = null } }

async function handleDeleteMsg() {
  if (!msgPressTarget) return
  const msg = msgPressTarget
  try {
    await ElMessageBox.confirm('确定删除此消息？', '删除消息', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await deleteMessage(msg.id)
    knownIds.delete(msg.id)
    chatMessages.value = chatMessages.value.filter(m => m.id !== msg.id)
    ElMessage.success('已删除')
  } catch { /* */ }
  msgPressTarget = null; msgPressTimer = null
}

// ====== 会话长按菜单 ======
function startPress(session) {
  wasLongPress = false
  pressTimer = setTimeout(() => {
    wasLongPress = true; pressedSession.value = session; actionMenuVisible.value = true
    nextTick(() => { const el = document.querySelector('.action-sheet-overlay'); if (el) el.focus() })
  }, 600)
}
function cancelPress() { if (pressTimer) { clearTimeout(pressTimer); pressTimer = null } }
function closeActionMenu() { actionMenuVisible.value = false; pressedSession.value = null }

function hideSession() {
  const session = pressedSession.value; if (!session) return
  const hiddenKeys = loadHiddenKeys(); hiddenKeys.add(getSessionKey(session)); saveHiddenKeys(hiddenKeys)
  sessions.value = sessions.value.filter(s => s.id !== session.id)
  ElMessage.success('会话已隐藏'); closeActionMenu()
}

async function deleteSessionAction() {
  const session = pressedSession.value; if (!session) return
  const targetId = Number(getTargetId(session))
  if (!targetId || targetId <= 0) {
    ElMessage.error('无法识别会话目标，请刷新重试')
    closeActionMenu()
    return
  }
  const name = getSessionName(session)
  closeActionMenu()
  try {
    await ElMessageBox.confirm(`确定删除与「${name}」的私信会话及所有聊天记录？`, '删除会话', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await deleteSession(targetId)
    sessions.value = sessions.value.filter(s => s.id !== session.id)
    if (activeTarget.value === getTargetId(session)) activeTarget.value = null
    ElMessage.success('会话已删除')
  } catch { /* */ }
}

// ====== 生命周期 ======
onMounted(async () => {
  await loadSessions()
  // 如果 URL 带 user 参数，自动打开聊天
  const userId = route.query.user
  if (userId) {
    const targetId = Number(userId)
    // 等待会话列表加载后检查目标是否在列表中
    const exists = sessions.value.some(s => getTargetId(s) === targetId)
    if (exists || window.innerWidth > 768) {
      openChat(targetId)
    }
  }
  pollTimer = setInterval(() => loadSessions(false), 5000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
  if (chatPollTimer) clearInterval(chatPollTimer)
  if (pressTimer) clearTimeout(pressTimer)
  if (msgPressTimer) clearTimeout(msgPressTimer)
})
</script>

<style scoped>
.chat-view {
  min-height: calc(100vh - 144px);
  padding: var(--spacing-md) 0;
}
.chat-container {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  background: var(--glass-light);
  backdrop-filter: var(--glass-light-blur);
  -webkit-backdrop-filter: var(--glass-light-blur);
  border: 1px solid var(--glass-light-border);
  border-radius: var(--radius-xl);
  overflow: hidden;
  height: calc(100vh - 200px);
}
.chat-divider {
  width: 1px;
  background: var(--border-color);
  flex-shrink: 0;
}

/* 左侧栏 */
.chat-sidebar {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--spacing-md-lg); border-bottom: 1px solid var(--border-color);
}
.sidebar-title { display: flex; align-items: center; gap: var(--spacing-sm); font-size: 17px; font-weight: 600; color: var(--accent-primary); }
.refresh-btn { padding: 6px; background: transparent; border: 1px solid var(--border-color); border-radius: var(--radius-sm); color: var(--text-secondary); cursor: pointer; transition: all var(--transition-fast); }
.refresh-btn:hover { border-color: var(--accent-primary); color: var(--accent-primary); }
.loading-state { padding: var(--spacing-md); }
.empty-sessions { flex: 1; display: flex; align-items: center; justify-content: center; }
.session-list { flex: 1; overflow-y: auto; padding: var(--spacing-sm); }
.session-item {
  display: flex; align-items: center; gap: var(--spacing-md);
  padding: var(--spacing-md); border-radius: var(--radius-md);
  cursor: pointer; transition: all var(--transition-fast); margin-bottom: 4px;
}
.session-item:hover { background: var(--bg-hover); }
.session-item.active { background: rgba(0, 212, 255, 0.1); border: 1px solid rgba(0, 212, 255, 0.2); }
.session-item.is-pressed { background: rgba(0, 212, 255, 0.08); }
.session-avatar { position: relative; flex-shrink: 0; }
.unread-badge {
  position: absolute; top: -4px; right: -4px; min-width: 18px; height: 18px;
  padding: 0 4px; background: var(--danger); border-radius: 9px;
  font-size: 11px; font-weight: 600; color: #fff;
  display: flex; align-items: center; justify-content: center;
}
.session-info { flex: 1; min-width: 0; }
.session-name { font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 2px; }
.session-preview { font-size: 12px; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-time { font-size: 11px; color: var(--text-muted); flex-shrink: 0; }

/* 右侧聊天区 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.chat-empty {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; color: var(--text-muted);
}
.empty-icon { font-size: 48px; margin-bottom: var(--spacing-md); opacity: 0.3; }
.chat-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; border-bottom: 1px solid var(--border-color); flex-shrink: 0;
}
.chat-header-left { display: flex; align-items: center; gap: 10px; }
.chat-title { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.chat-collapse {
  font-size: 13px;
  color: var(--text-muted);
  cursor: pointer;
  transition: color var(--transition-fast);
}
.chat-collapse:hover { color: var(--accent-primary); }
.chat-messages {
  flex: 1; overflow-y: auto; padding: 16px 20px;
  display: flex; flex-direction: column; gap: 16px;
}
.chat-messages::-webkit-scrollbar { width: 4px; }
.chat-messages::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 2px; }
.chat-empty-tip { flex: 1; display: flex; align-items: center; justify-content: center; color: var(--text-muted); font-size: 13px; }
.cd-row { display: flex; align-items: flex-start; gap: 8px; }
.cd-row.self { justify-content: flex-end; }
.cd-avatar { flex-shrink: 0; margin-top: 2px; }
.cd-bubble {
  max-width: 60%; padding: 10px 14px;
  border-radius: 6px 18px 18px 18px;
  background: var(--glass-medium); border: 1px solid var(--glass-medium-border);
  backdrop-filter: var(--glass-medium-blur);
  -webkit-backdrop-filter: var(--glass-medium-blur);
}
.cd-row.self .cd-bubble {
  border-radius: 18px 6px 18px 18px;
  background: #95ec69; border-color: #95ec69; color: #1a1a1a;
}
.cd-row.self .cd-bubble .cd-time { color: rgba(0,0,0,0.45); }
.cd-text { font-size: 14px; line-height: 1.6; color: var(--text-primary); word-break: break-word; white-space: pre-wrap; }
.cd-time { font-size: 11px; color: var(--text-muted); margin-top: 4px; text-align: right; }
.chat-input-area {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; border-top: 1px solid var(--border-color); flex-shrink: 0;
}
.cd-input {
  flex: 1; height: 38px; padding: 0 14px;
  border: 1px solid var(--border-color); border-radius: 6px;
  font-size: 14px; outline: none; background: var(--bg-primary);
  color: var(--text-primary); font-family: inherit; transition: border-color 0.15s;
}
.cd-input:focus { border-color: #07c160; }
.cd-send {
  padding: 0 22px; height: 38px; border: none; border-radius: 6px;
  background: #07c160; color: #fff; font-size: 14px; font-weight: 500;
  cursor: pointer; white-space: nowrap; transition: opacity 0.15s; font-family: inherit;
}
.cd-send:hover { opacity: 0.9; }
.cd-send:disabled { opacity: 0.4; cursor: not-allowed; }

/* 操作菜单 */
.action-sheet-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0,0,0,0.55); display: flex;
  align-items: flex-end; justify-content: center; outline: none;
  animation: fadeIn 0.15s ease-out;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
.action-sheet {
  width: 100%; max-width: 500px; display: flex; flex-direction: column;
  margin: 0 16px 24px 16px; border-radius: 14px; overflow: hidden;
  background: #1c2135; border: 1px solid rgba(255,255,255,0.08);
  box-shadow: 0 -4px 24px rgba(0,0,0,0.5); animation: slideUp 0.2s ease-out;
}
@keyframes slideUp { from { transform: translateY(100%); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
.action-item {
  display: flex; align-items: center; justify-content: center; gap: 8px;
  padding: 16px; font-size: 16px; cursor: pointer;
  transition: background 0.15s ease; border-bottom: 1px solid rgba(255,255,255,0.06);
}
.action-item:last-child { border-bottom: none; }
.action-item:active { background: rgba(255,255,255,0.05); }
.action-hide { color: #8892b0; }
.action-delete { color: #ff4d4f; font-weight: 600; }
.action-cancel { color: #8892b0; font-size: 15px; margin-top: 8px; border-radius: 14px; border-bottom: none; background: #1c2135; border: 1px solid rgba(255,255,255,0.08); }

/* 移动端适配 */
@media (max-width: 768px) {
  .chat-container { flex-direction: column; height: calc(100vh - 160px); }
  .chat-sidebar { width: 100%; }
  .chat-sidebar.collapsed { display: none; }
  .chat-area { display: none; }
  .chat-area.active {
    display: flex; position: absolute; inset: 0; z-index: 5;
    background: var(--glass-heavy);
    backdrop-filter: var(--glass-heavy-blur);
    -webkit-backdrop-filter: var(--glass-heavy-blur);
  }
  .chat-divider { display: none; }
  .cd-bubble { max-width: 75%; }
}

@media (max-width: 480px) {
  .cd-bubble { max-width: 85%; padding: 8px 12px; }
  .cd-text { font-size: 15px; }
}
</style>
