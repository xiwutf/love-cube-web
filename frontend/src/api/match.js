import request from './request.js'

export const getMatchList = ()           => request.get('/matches/list')
export const likeUser     = (userId)     => request.post(`/matches/${userId}/like`)
export const dislikeUser  = (userId)     => request.post(`/matches/${userId}/dislike`)
export const superlikeUser = (userId)    => request.post(`/matches/${userId}/superlike`)
export const filterMatches = (data)      => request.post('/matches/filter', data)
