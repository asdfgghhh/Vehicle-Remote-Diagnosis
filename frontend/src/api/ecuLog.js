import request from '@/utils/request'

export const getEcuLogPage = (params) => {
  return request.get('/ecu-log/page', { params })
}

export const downloadLog = (id) => {
  return request.get(`/ecu-log/download/${id}`, { responseType: 'blob' })
}
