import request from './request.js'

export const getNotificationSettings = () => request.get('/notification-settings')

export const putNotificationSettings = (items) => request.put('/notification-settings', items)
