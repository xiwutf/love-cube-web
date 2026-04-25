import request from './request.js'

export const getChatList       = ()         => request.get('/messages/chat')
export const getInteractList   = ()         => request.get('/messages/interact')
export const getVisitorList    = ()         => request.get('/messages/visitor')
export const getUnreadCount    = ()         => request.get('/messages/unread')
export const markInteractRead  = ()         => request.put('/messages/interact/markRead')
export const markVisitorRead   = ()         => request.put('/messages/visitor/markRead')
export const followUser        = (id)       => request.post(`/interactions/follow/${id}`)
export const greetUser         = (senderId, receiverId) => request.post('/greetings/greet', null, { params: { senderId, receiverId } })
