import request from '@/utils/request'

export const login = (data) => {
  return request.post('/auth/login', data, { showError: false })
}

export const register = (data) => {
  return request.post('/auth/register', data, { showError: false })
}

export const getUserInfo = () => {
  return request.get('/auth/userinfo')
}
