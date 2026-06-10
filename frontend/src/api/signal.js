import request from '@/utils/request'

export const getSignalTimeline = (params) => {
  const { vehicleId, vin, startTime, endTime } = params
  return request.get('/signal/timeline/' + vehicleId, {
    params: { vin, startTime, endTime }
  })
}

export const getSignalPage = (vehicleId, params) => {
  return request.get(`/signal/page/${vehicleId}`, { params })
}

export const getSignalByName = (vehicleId, params) => {
  return request.get(`/signal/signal-name/${vehicleId}`, { params })
}

export const receiveSignal = (vin, payload) => {
  return request.post('/signal/vehicle/receive', payload, { params: { vin } })
}
