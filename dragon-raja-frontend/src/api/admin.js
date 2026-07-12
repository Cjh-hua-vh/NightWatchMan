import request from './request'

/**
 * 获取用户管理列表（分页）
 * @param {Object} params - { current, size }
 */
export function getUsers(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

/**
 * 审核用户（通过/拒绝）
 * @param {number} id - 用户ID
 * @param {Object} data - { status: 1-通过 2-拒绝 }
 */
export function auditUser(id, data) {
  return request({
    url: `/admin/users/${id}/audit`,
    method: 'put',
    data
  })
}

/**
 * 封禁/解封用户
 * @param {number} id - 用户ID
 */
export function banUser(id) {
  return request({
    url: `/admin/users/${id}/ban`,
    method: 'put'
  })
}

/**
 * 获取帖子管理列表（分页）
 * @param {Object} params - { current, size }
 */
export function getAdminPosts(params) {
  return request({
    url: '/admin/posts',
    method: 'get',
    params
  })
}

/**
 * 管理员删除帖子
 * @param {number} id - 帖子ID
 */
export function adminDeletePost(id) {
  return request({
    url: `/admin/posts/${id}`,
    method: 'delete'
  })
}

export function adminUpdatePost(id, data) {
  return request({
    url: `/admin/posts/${id}`,
    method: 'put',
    data
  })
}

/** 管理员编辑用户 */
export function updateUser(id, data) {
  return request({ url: `/admin/users/${id}`, method: 'put', data })
}

/** 删除用户（连同帖子评论） */
export function deleteUser(id) {
  return request({ url: `/admin/users/${id}`, method: 'delete' })
}
