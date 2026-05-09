import request from './request.js'

export function trackVisit(payload) {
  return request.post('/analytics/track', payload)
}

export function getAdminAnalyticsOverview() {
  return request.get('/admin/analytics/overview')
}

export function getAdminAnalyticsTrend(range = '7d') {
  return request.get('/admin/analytics/trend', { params: { range } })
}

export function getAdminAnalyticsTopPages(range = '7d') {
  return request.get('/admin/analytics/top-pages', { params: { range } })
}

export function getAdminAnalyticsSources(range = '7d') {
  return request.get('/admin/analytics/sources', { params: { range } })
}

export function getAdminAnalyticsClientDistribution(range = '7d') {
  return request.get('/admin/analytics/client-distribution', { params: { range } })
}

export function getAdminAnalyticsVisitors(page = 1, pageSize = 20, loggedInOnly = false) {
  return request.get('/admin/analytics/visitors', { params: { page, pageSize, loggedInOnly } })
}
