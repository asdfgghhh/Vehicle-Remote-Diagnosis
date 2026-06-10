import request from '@/utils/request'

export const getVehicleDashboardStats = () => {
  return request.get('/vehicle/stats')
}

export const getVehicleOnlineTrend = (params) => {
  return request.get('/vehicle/stats/online-trend', { params })
}

export const getVehicleAlertLongTrend = (params) => {
  return request.get('/vehicle/stats/alert-long-trend', { params })
}

export const getVehicleModelPage = (params) => {
  return request.get('/vehicle/model/page', { params })
}

export const getVehicleModel = (id) => {
  return request.get(`/vehicle/model/${id}`)
}

export const createVehicleModel = (data) => {
  return request.post('/vehicle/model', data)
}

export const updateVehicleModel = (id, data) => {
  return request.put(`/vehicle/model/${id}`, data)
}

export const deleteVehicleModel = (id) => {
  return request.delete(`/vehicle/model/${id}`)
}

export const getVehiclePage = (params) => {
  return request.get('/vehicle/page', { params })
}

export const getVehicle = (id) => {
  return request.get(`/vehicle/${id}`)
}

export const getVehicleByVin = (vin) => {
  return request.get(`/vehicle/vin/${encodeURIComponent(vin)}`)
}

export const createVehicle = (data) => {
  return request.post('/vehicle', data)
}

export const updateVehicle = (id, data) => {
  return request.put(`/vehicle/${id}`, data)
}

export const deleteVehicle = (id) => {
  return request.delete(`/vehicle/${id}`)
}

export const syncVehicleFromKafka = () => {
  return request.post('/vehicle/sync/kafka')
}

export const syncVehicleFromApi = (apiUrl) => {
  return request.post('/vehicle/sync/api', null, { params: { apiUrl } })
}

export const getVehicleEcus = (id) => {
  return request.get(`/vehicle/${id}/ecu`)
}

export const addVehicleEcu = (id, data) => {
  return request.post(`/vehicle/${id}/ecu`, data)
}

export const getVehicleSyncLogPage = (params) => {
  return request.get('/vehicle/sync-record/page', { params })
}

export const getVehicleSyncLog = (id) => {
  return request.get(`/vehicle/sync-record/${id}`)
}
