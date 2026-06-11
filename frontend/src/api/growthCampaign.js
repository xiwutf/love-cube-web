import request from './request.js'

export function reportGrowthCampaignClick(deliveryId) {
  return request.post(`/growth/campaign/delivery/${deliveryId}/click`)
}
