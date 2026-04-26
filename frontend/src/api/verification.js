import request from './request.js'

export const submitVerification  = (verifyType, submitData) =>
  request.post('/verify/submit', { verifyType, submitData })

export const getMyVerifications  = () => request.get('/verify/my')

export const getVerificationStatus = (userId) => request.get(`/verify/status/${userId}`)
