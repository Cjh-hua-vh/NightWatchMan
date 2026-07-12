import request from './request'

/** 新闻列表 */
export function getNewsList(params) { return request({ url: '/news', method: 'get', params }) }
/** 新闻详情 */
export function getNewsDetail(id) { return request({ url: `/news/${id}`, method: 'get' }) }
