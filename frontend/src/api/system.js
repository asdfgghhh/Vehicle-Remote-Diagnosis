import request from '@/utils/request'

export const getUserPage = (params) => {
  return request.get('/auth/user/page', { params })
}

export const getUser = (id) => {
  return request.get(`/auth/user/${id}`)
}

export const createUser = (data) => {
  return request.post('/auth/user', data)
}

export const updateUser = (id, data) => {
  return request.put(`/auth/user/${id}`, data)
}

export const deleteUser = (id) => {
  return request.delete(`/auth/user/${id}`)
}

export const assignUserRoles = (id, roleIds) => {
  return request.put(`/auth/user/${id}/roles`, roleIds)
}

export const getRolePage = (params) => {
  return request.get('/auth/role/page', { params })
}

export const getRoleList = () => {
  return request.get('/auth/role/list')
}

export const getRole = (id) => {
  return request.get(`/auth/role/${id}`)
}

export const createRole = (data) => {
  return request.post('/auth/role', data)
}

export const updateRole = (id, data) => {
  return request.put(`/auth/role/${id}`, data)
}

export const deleteRole = (id) => {
  return request.delete(`/auth/role/${id}`)
}
