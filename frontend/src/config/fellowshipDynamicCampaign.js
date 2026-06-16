/** 联谊动态发布 — 话题与节日文案（按日期自动切换，节后无需改代码） */

const DEFAULT_TOPIC_TAGS = [
  '#今日感恩',
  '#联谊日常',
  '#周末计划',
  '#新朋友',
  '#成长记录',
  '#生活碎片',
]

const DEFAULT_QUICK_EMOJIS = ['✨', '💛', '🌿', '🎵', '☕', '📖', '🌅', '💭', '🤝', '🎉', '☀️']

/** 2026 端午 · #端午晒一晒（含 6/16 预热，6/22 起自动恢复默认） */
const DRAGON_BOAT_2026 = {
  start: '2026-06-16',
  end: '2026-06-22',
  topicTags: [
    '#端午晒一晒',
    '#假期小确幸',
    '#今日感恩',
    '#一个人也要好好过',
    '#和朋友的瞬间',
    '#家乡味道',
  ],
  composeLead: '端午假期，想和大家分享什么？',
  placeholder: '粽子、家常、祝福、独处时光…带话题 #端午晒一晒 更容易被看见。',
  quickEmojis: ['🌿', '✨', '💛', '🫔', '🎋', '☕', '🌅', '💭', '🤝', '🎉', '☀️', '🐲'],
  campaignBanner: {
    kicker: '端午活动',
    text: '话题广场正在接龙「家乡端午怎么过」；动态带 #端午晒一晒 或 #丑粽子大赛 更容易被看见。',
    link: '/m/platform/topics',
    linkLabel: '去话题广场接龙',
  },
  publishTips: [
    '参与 #端午晒一晒 或 #丑粽子大赛，晒图更容易被看见。',
    '话题广场有「家乡端午怎么过」接龙，欢迎用一两句分享习俗。',
    '尊重他人，避免人身攻击；勿发广告、外链引流或隐私信息。',
  ],
}

function parseDateOnly(iso) {
  const [y, m, d] = iso.split('-').map(Number)
  return new Date(y, m - 1, d)
}

function isWithinCampaign(campaign, date = new Date()) {
  const day = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const start = parseDateOnly(campaign.start)
  const end = parseDateOnly(campaign.end)
  return day >= start && day < end
}

const DEFAULT_PUBLISH_TIPS = [
  '尊重他人，避免人身攻击与不当言论。',
  '勿发布广告、外链引流或隐私信息（电话、住址等）。',
  '内容会展示在动态广场，请使用友善、真实的表达。',
]

function buildCampaignView(campaign) {
  return {
    topicTags: campaign.topicTags,
    composeLead: campaign.composeLead,
    placeholder: campaign.placeholder,
    quickEmojis: campaign.quickEmojis,
    campaignBanner: campaign.campaignBanner || null,
    publishTips: campaign.publishTips || DEFAULT_PUBLISH_TIPS,
  }
}

export function getDynamicPublishCampaign(date = new Date()) {
  if (isWithinCampaign(DRAGON_BOAT_2026, date)) {
    return buildCampaignView(DRAGON_BOAT_2026)
  }
  return {
    topicTags: DEFAULT_TOPIC_TAGS,
    composeLead: '今天想和大家分享什么？',
    placeholder: '可以写心情、故事、心愿或生活片段…真诚分享更容易遇见同路人。',
    quickEmojis: DEFAULT_QUICK_EMOJIS,
    campaignBanner: null,
    publishTips: DEFAULT_PUBLISH_TIPS,
  }
}
