import request from './request'

/**
 * 获取帖子列表（分页）
 * @param {Object} params - { current, size, category, keyword }
 */
export function getPosts(params) {
  return request({ url: '/posts', method: 'get', params })
}

/** 获取指定用户的帖子 */
export function getUserPosts(userId, params) {
  return request({ url: '/posts/user/' + userId, method: 'get', params })
}

/**
 * 获取热门帖子
 */
export function getHotPosts() {
  return request({
    url: '/posts/hot',
    method: 'get'
  })
}

/**
 * 获取帖子详情
 * @param {number} id - 帖子ID
 */
export function getPostDetail(id) {
  return request({
    url: `/posts/${id}`,
    method: 'get'
  })
}

/**
 * 发布帖子
 * @param {Object} data - { title, content, category }
 */
export function createPost(data) {
  return request({
    url: '/posts',
    method: 'post',
    data
  })
}

/**
 * 编辑帖子
 * @param {number} id - 帖子ID
 * @param {Object} data - { title, content, category }
 */
export function updatePost(id, data) {
  return request({
    url: `/posts/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除帖子
 * @param {number} id - 帖子ID
 */
export function deletePost(id) {
  return request({
    url: `/posts/${id}`,
    method: 'delete'
  })
}

/**
 * 置顶/取消置顶帖子
 * @param {number} id - 帖子ID
 */
export function toggleTop(id) {
  return request({
    url: `/posts/${id}/top`,
    method: 'put'
  })
}
