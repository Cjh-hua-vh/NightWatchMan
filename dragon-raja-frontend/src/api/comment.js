import request from './request'

/**
 * 获取帖子评论列表（分页）
 * @param {number} postId - 帖子ID
 * @param {Object} params - { current, size }
 */
export function getComments(postId, params) {
  return request({
    url: `/comments/post/${postId}`,
    method: 'get',
    params
  })
}

/**
 * 发表评论
 * @param {Object} data - { postId, content }
 */
export function createComment(data) {
  return request({
    url: '/comments',
    method: 'post',
    data
  })
}

/**
 * 删除评论
 * @param {number} id - 评论ID
 */
export function deleteComment(id) {
  return request({
    url: `/comments/${id}`,
    method: 'delete'
  })
}

export function adminDeleteComment(id) {
  return request({
    url: `/comments/admin/${id}`,
    method: 'delete'
  })
}

export function adminUpdateComment(id, content) {
  return request({
    url: `/comments/admin/${id}`,
    method: 'put',
    data: { content }
  })
}
