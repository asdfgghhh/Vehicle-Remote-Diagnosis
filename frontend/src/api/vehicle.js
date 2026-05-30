import request from '@/utils/request'

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
