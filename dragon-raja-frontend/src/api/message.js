import request from './request'

/** 获取通知列表（不过滤私信，供顶部铃铛使用） */
export function getNotifications(params) {
  return request({ url: '/message/notifications', method: 'get', params })
}

/** 获取消息列表（邮件页面，过滤私信） */
export function getMessages(params) {
  return request({ url: '/message', method: 'get', params })
}

/** 获取全部消息（含已读），供邮件页面使用 */
export function getAllMessages(params) {
  return request({ url: '/message/all', method: 'get', params })
}

/** 获取未读消息数 */
export function getUnreadCount() {
  return request({ url: '/message/unread', method: 'get' })
}

/** 标记已读 */
export function markAsRead(id) {
  return request({ url: `/message/read/${id}`, method: 'put' })
}

/** 全部标为已读 */
export function markAllAsRead() {
  return request({ url: '/message/read-all', method: 'put' })
}

/** 标记通知已读（不含私信） */
export function markNotificationsRead() {
  return request({ url: '/message/read-notifications', method: 'put' })
}

/** 清空所有消息通知 */
export function clearAllMessages() {
  return request({ url: '/message/clear-all', method: 'delete' })
}

/** 删除消息 */
export function deleteMessage(id) {
  return request({ url: `/message/${id}`, method: 'delete' })
}

/** 批量删除消息 */
export function batchDeleteMessages(ids) {
  return request({ url: '/message/batch', method: 'delete', data: ids })
}

/** 获取私信会话列表 */
export function getSessionList() {
  return request({ url: '/message/chat/sessions', method: 'get' })
}

/** 获取聊天记录 */
export function getChatHistory(targetId) {
  return request({ url: '/message/chat/history', method: 'get', params: { targetId } })
}

/** 发送私信 */
export function sendPrivateMessage(receiverId, content) {
  return request({ url: '/message/chat/send', method: 'post', data: { receiverId, content } })
}

/** 删除私信会话 */
export function deleteSession(targetId) {
  return request({ url: `/message/chat/session/${targetId}`, method: 'delete' })
}
