import request from '@/utils/request'

export const getDbcFilePage = (params) => {
  return request.get('/dbc/page', { params })
}

export const getDbcFile = (id) => {
  return request.get(`/dbc/${id}`)
}

export const uploadDbcFile = (formData) => {
  return request.post('/dbc/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const getDbcMessages = (id) => {
  return request.get(`/dbc/${id}/messages`)
}

export const getDbcSignals = (id) => {
  return request.get(`/dbc/${id}/signals`)
}

export const downloadDbcFile = (id) => {
  return request.get(`/dbc/${id}/download`, { responseType: 'blob' })
}

export const dispatchToVehicle = (dbcId, vehicleId) => {
  return request.post(`/dbc/${dbcId}/dispatch/${vehicleId}`)
}

export const dispatchToVehicles = (dbcId, vehicleIds) => {
  return request.post(`/dbc/${dbcId}/dispatch`, vehicleIds)
}

export const deleteDbcFile = (id) => {
  return request.delete(`/dbc/${id}`)
}
