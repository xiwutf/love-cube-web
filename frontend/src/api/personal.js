import request from './request.js'

export const getMyLikeUsers = () => request.get('/interactions/likes/sent')
export const getMutualLikeUsers = () => request.get('/interactions/likes/mutual')
export const getLikedMeUsers = () => request.get('/interactions/likes/received')
export const getFollowingUsers = () => request.get('/interactions/following')
export const getFansUsers = () => request.get('/interactions/fans')
export const toggleFollowUser = (userId) => request.post(`/interactions/follow/${userId}`)
export const getMyDynamicCount = () => request.get('/dynamics/me/count')

