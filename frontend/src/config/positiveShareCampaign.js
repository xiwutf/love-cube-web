import { isDragonBoat2026Active } from '@/config/dragonBoat2026Campaign.js'

const DEFAULT = {
  active: false,
  theme: 'default',
  pageKicker: '每日心声',
  pageTitle: '每日心声',
  pageSubtitle: '记录温暖与成长，一句鼓励也许能照亮另一个人的一天',
  editorPlaceholder: '今天有什么值得感恩、被鼓励、想记录的事情？',
  campaignBanner: null,
  topicLink: null,
}

const DRAGON_BOAT = {
  active: true,
  theme: 'dragon-boat',
  pageKicker: '端午活动',
  pageTitle: '端午心声',
  pageSubtitle: '晒出粽子、家常与祝福，带 #端午晒一晒 更容易被看见',
  editorPlaceholder: '粽子、家常、祝福、独处时光…可带 #端午晒一晒 或 #丑粽子大赛',
  campaignBanner: {
    kicker: '端午晒一晒',
    text: '话题广场正在接龙「家乡端午怎么过」；心声与动态带话题标签更容易被推荐。',
    linkLabel: '去话题广场',
  },
  topicLink: {
    label: '家乡端午怎么过 · 去接龙',
  },
}

export function getPositiveShareCampaign(date = new Date()) {
  if (isDragonBoat2026Active(date)) {
    return { ...DRAGON_BOAT }
  }
  return { ...DEFAULT }
}
