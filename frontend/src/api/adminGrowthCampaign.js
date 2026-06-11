import request from './request.js'

export function previewGrowthCampaignSegment(segment) {
  return request.get('/admin/growth/campaign/preview', { params: { segment } })
}

export function sendGrowthCampaign(payload) {
  return request.post('/admin/growth/campaign/send', payload)
}

export function listGrowthCampaigns() {
  return request.get('/admin/growth/campaigns')
}

export function getGrowthCampaignDetail(id, params = {}) {
  return request.get(`/admin/growth/campaigns/${id}`, { params })
}

export function refreshGrowthCampaignConversion(id) {
  return request.post(`/admin/growth/campaigns/${id}/refresh-conversion`)
}

export function listGrowthCampaignTemplates() {
  return request.get('/admin/growth/campaign/templates')
}
