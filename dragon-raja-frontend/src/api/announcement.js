import request from './request'

/**
 * 获取公告列表（分页）
 * @param {Object} params - { current, size }
 */
export function getAnnouncements(params) {
  return request({
    url: '/announcements',
    method: 'get',
    params
  })
}

/**
 * 获取最新置顶公告
 */
export function getLatestAnnouncement() {
  return request({
    url: '/announcements/latest',
    method: 'get'
  })
}

/**
 * 发布公告
 * @param {Object} data - { title, content, isTop }
 */
export function createAnnouncement(data) {
  return request({
    url: '/announcements',
    method: 'post',
    data
  })
}

/**
 * 编辑公告
 * @param {number} id - 公告ID
 * @param {Object} data - { title, content, isTop }
 */
export function updateAnnouncement(id, data) {
  return request({
    url: `/announcements/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除公告
 * @param {number} id - 公告ID
 */
export function deleteAnnouncement(id) {
  return request({
    url: `/announcements/${id}`,
    method: 'delete'
  })
}
