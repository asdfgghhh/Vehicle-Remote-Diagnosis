import request from '@/utils/request'

// 车辆健康 - 七大域状态
export const getVehicleHealth = (vin) => {
  return request.get(`/vehicle/health/${encodeURIComponent(vin)}`)
}

// 车辆健康 - 历史趋势（近 30/90 天）
export const getVehicleHealthTrend = (vin, days = 30) => {
  return request.get(`/vehicle/health/${encodeURIComponent(vin)}/trend`, { params: { days } })
}

// 车辆健康 - 指定域详情（部件级健康）
export const getVehicleDomainHealth = (vin, domainCode) => {
  return request.get(`/vehicle/health/domain/${encodeURIComponent(vin)}/${domainCode}`)
}

// 高风险车辆列表
export const getRiskVehicleList = () => {
  return request.get('/vehicle/risk/list')
}
