import request from './request.js'

export const getBanners   = () => request.get('/banners')
export const getRecommends = () => request.get('/recommends')
export const getNewcomers  = () => request.get('/newcomers')
