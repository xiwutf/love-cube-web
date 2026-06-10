import request from './request.js'

export function submitReport(payload) {
  return request.post('/reports', payload)
}

export function fetchMyReports() {
  return request.get('/reports/my')
}
