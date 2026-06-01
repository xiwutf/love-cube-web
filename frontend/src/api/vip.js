import request from './request.js'

export const buyVip = (packageId, packageName, price) =>
  request.post('/payment/vip', { packageId, packageName, price })

export const fetchVipStatus = () => request.get('/payment/vip/status')
