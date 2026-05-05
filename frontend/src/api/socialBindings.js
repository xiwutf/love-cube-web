import request from './request.js'

export const getSocialBindings = () => request.get('/social-bindings')

export const mockBindWechatOfficial = () => request.post('/social-bindings/wechat/mock-bind')

export const deleteSocialBinding = (id) => request.delete(`/social-bindings/${id}`)
