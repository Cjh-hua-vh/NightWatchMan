import request from './request'

/** 好友列表 */
export function getFriendList() { return request({ url: '/friend/list', method: 'get' }) }
/** 好友申请列表 */
export function getPendingRequests() { return request({ url: '/friend/requests', method: 'get' }) }
/** 发送好友申请 */
export function applyFriend(friendId) { return request({ url: '/friend/apply', method: 'post', data: { friendId } }) }
/** 接受申请 */
export function acceptFriend(friendId) { return request({ url: `/friend/accept/${friendId}`, method: 'put' }) }
/** 拒绝申请 */
export function rejectFriend(friendId) { return request({ url: `/friend/reject/${friendId}`, method: 'put' }) }
/** 删除好友 */
export function removeFriend(friendId) { return request({ url: `/friend/${friendId}`, method: 'delete' }) }
/** 检查是否为好友 */
export function checkFriend(targetId) { return request({ url: `/friend/check/${targetId}`, method: 'get' }) }
