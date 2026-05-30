import request from '@/utils/request'

export const getEcuLogPage = (params) => {
  return request.get('/ecu-log/page', { params })
}

export const getEcuLog = (id) => {
  return request.get(`/ecu-log/${id}`)
}

export const initUpload = (params) => {
  return request.post('/ecu-log/init-upload', null, { params })
}

export const uploadChunk = (formData) => {
  return request.post('/ecu-log/upload-chunk', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const mergeChunks = (params) => {
  return request.post('/ecu-log/merge-chunks', null, { params })
}

export const checkUpload = (fileMd5) => {
  return request.get('/ecu-log/check-upload', { params: { fileMd5 } })
}

export const downloadLog = (id) => {
  return request.get(`/ecu-log/download/${id}`, { responseType: 'blob' })
}
