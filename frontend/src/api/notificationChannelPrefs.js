import request from './request.js'

export const getNotificationChannelPrefs = () => request.get('/notification-channel-prefs')

export const putNotificationChannelPrefs = (body) => request.put('/notification-channel-prefs', body)

export const postNotificationChannelTest = () => request.post('/notification-channel-prefs/test')
