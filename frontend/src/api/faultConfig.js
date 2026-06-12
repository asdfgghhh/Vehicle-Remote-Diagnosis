import request from '@/utils/request'

export const getFaultConfigPage = (params) => {
  return request.get('/vehicle/fault-config/page', { params })
}

export const getFaultConfig = (id) => {
  return request.get(`/vehicle/fault-config/${id}`)
}

export const createFaultConfig = (data) => {
  return request.post('/vehicle/fault-config', data)
}

export const updateFaultConfig = (id, data) => {
  return request.put(`/vehicle/fault-config/${id}`, data)
}

export const deleteFaultConfig = (id) => {
  return request.delete(`/vehicle/fault-config/${id}`)
}
