import request from './request'

/**
 * 获取用户个人资料（不传 userId 则获取当前用户）
 * @param {number} userId - 可选，指定用户ID
 */
export function getProfile(userId) {
  const url = userId ? `/user/${userId}` : '/user/profile'
  return request({ url, method: 'get' })
}

/**
 * 更新个人资料
 * @param {Object} data - { nickname, avatar, signature, bloodlineGrade, faction }
 */
export function updateProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 * @param {Object} data - { oldPassword, newPassword }
 */
export function updatePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

/**
 * 获取在线用户列表
 */
export function getOnlineUsers() {
  return request({
    url: '/user/online',
    method: 'get'
  })
}

/**
 * 心跳：维持在线状态
 */
export function heartbeat() {
  return request({
    url: '/user/heartbeat',
    method: 'post'
  })
}
