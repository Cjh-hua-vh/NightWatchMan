import request from './request'

/** 获取血统测试题目 */
export function getExamQuestions() {
  return request({ url: '/blood-test/exam', method: 'get' })
}

/** 提交测试答案 */
export function submitExam(data) {
  return request({ url: '/blood-test/submit', method: 'post', data })
}

/** 跳过测试，随机分配 */
export function skipTest(bloodType) {
  return request({ url: '/blood-test/skip', method: 'post', data: { bloodType } })
}
