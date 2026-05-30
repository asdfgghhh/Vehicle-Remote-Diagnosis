import request from '@/utils/request'

export const getSignalTimeline = (params) => {
  return request.get('/signal/timeline/' + params.vehicleId, { params })
}

export const getSignalPage = (vehicleId, params) => {
  return request.get(`/signal/page/${vehicleId}`, { params })
}

export const getSignalByName = (vehicleId, params) => {
  return request.get(`/signal/signal-name/${vehicleId}`, { params })
}

export const receiveSignal = (vin, payload) => {
  return request.post('/signal/receive', payload, { params: { vin } })
}
