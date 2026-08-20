import request from '@/utils/request'

// 故障列表分页（支持 VIN/故障码/级别/状态/场景筛选）
export const getFaultPage = (params) => {
  return request.get('/vehicle/fault/page', { params })
}

// 智能值守上下文（近 90 天维保 / 未处理故障 / AI 推荐优先级）
export const getFaultStandby = (vin) => {
  return request.get('/vehicle/fault/standby', { params: { vin } })
}

// 一键发起远程诊断（关联故障树场景）
export const diagnoseFault = (id) => {
  return request.post(`/vehicle/fault/${id}/diagnose`)
}

// 故障树场景列表
export const getFaultSceneList = () => {
  return request.get('/vehicle/fault-scene/list')
}

// 故障趋势统计（按日/周）
export const getFaultAnalysisTrend = (granularity = 'day') => {
  return request.get('/vehicle/fault-analysis/trend', { params: { granularity } })
}

// 故障分布（按级别/域/场景/故障码 TOP N）
export const getFaultAnalysisDistribution = () => {
  return request.get('/vehicle/fault-analysis/distribution')
}
