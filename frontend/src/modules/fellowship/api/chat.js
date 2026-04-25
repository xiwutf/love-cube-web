import request from './request.js'

export const getChatHistory = (userId, receiverId) =>
  request.get(`/chat/history/${userId}/${receiverId}`)

export const markChatRead = (userId, receiverId) =>
  request.put(`/chat/markRead/${userId}/${receiverId}`)

export const deleteChat = (userId, receiverId) =>
  request.delete(`/delete/${userId}/${receiverId}`)
