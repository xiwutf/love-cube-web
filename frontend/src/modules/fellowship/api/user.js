import request from './request.js'

export const getMe          = ()     => request.get('/users/me')
export const updateProfile  = (data) => request.put('/users/profile', data)
