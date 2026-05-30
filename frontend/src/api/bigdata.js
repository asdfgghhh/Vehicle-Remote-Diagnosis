import request from '@/utils/request'

export const queryBigdataSignals = (params) => {
  return request.get('/bigdata/signals', { params })
}

export const queryBigdataLogs = (params) => {
  return request.get('/bigdata/logs', { params })
}

export const aggregateSignals = (params) => {
  return request.get('/bigdata/aggregate', { params })
}

export const getAvailableDates = (dataType) => {
  return request.get('/bigdata/dates', { params: { dataType } })
}

export const getStatistics = (params) => {
  return request.get('/bigdata/statistics', { params })
}

export const saveToHdfs = (data, path) => {
  return request.post('/bigdata/save', null, { params: { data, path } })
}

export const readFromHdfs = (path) => {
  return request.get('/bigdata/read', { params: { path } })
}

export const listFiles = (directory) => {
  return request.get('/bigdata/files', { params: { directory } })
}
