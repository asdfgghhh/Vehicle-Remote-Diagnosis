import request from '@/utils/request'

// 三级诊断级别列表（整车/域/部件）
export const getDiagnosisLevels = () => {
  return request.get('/diagnosis/level')
}

// 故障树场景列表（诊断前置选择）
export const getFaultTreeScenarios = () => {
  return request.get('/diagnosis/fault-tree/scenarios')
}

// 发起三级诊断任务
export const createDiagnosisTask = (data) => {
  return request.post('/diagnosis/task', data)
}

// 查询诊断任务进度
export const getDiagnosisTask = (id) => {
  return request.get(`/diagnosis/task/${id}`)
}

// 诊断报告
export const getDiagnosisReport = (id) => {
  return request.get(`/diagnosis/report/${id}`)
}

// 诊断任务历史分页
export const getDiagnosisTaskPage = (params) => {
  return request.get('/diagnosis/task/page', { params })
}

// UDS 通用执行
export const executeUds = (data) => {
  return request.post('/diagnosis/uds', data)
}

// 会话历史
export const getDiagnosisSessions = () => {
  return request.get('/diagnosis/sessions')
}

// 支持的服务清单
export const getDiagnosisServices = () => {
  return request.get('/diagnosis/services')
}
