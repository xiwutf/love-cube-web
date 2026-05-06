export const PLATFORM_CONTENT_TYPES = [
  { key: 'all', label: '全部' },
  { key: 'dynamic', label: '动态' },
  { key: 'mood', label: '心声' },
  { key: 'event', label: '活动' },
  { key: 'guide', label: '攻略' },
  { key: 'ai', label: 'AI' }
]

export const PLATFORM_SORTS = [
  { key: 'latest', label: '最新' },
  { key: 'hot', label: '最热' },
  { key: 'recommend', label: '推荐' }
]

export const PLATFORM_DEFAULT_TAGS = ['平台建议', '活动分享', '经验攻略', '每日心声', 'AI工具']

export const platformNotices = [
  { id: 1, title: '平台公告：内容中心已上线', summary: '底部导航已统一到首页、内容、发布、团体、我的。', createdAt: '2026-04-30' }
]

export const platformContentList = [
  { id: 1, title: '第一次参加线下活动的准备清单', summary: '从破冰话题到时间安排，这里给你一份可直接执行的清单。', content: '...', type: 'guide', cover: 'https://picsum.photos/seed/guide1/640/360', images: [], tags: ['经验攻略'], authorName: '运营小组', createdAt: '2026-04-30', likeCount: 36, commentCount: 9, pinned: true },
  { id: 2, title: '今晚活动现场招募志愿者', summary: '活动中心开放报名，欢迎有组织经验的伙伴加入。', content: '...', type: 'event', cover: 'https://picsum.photos/seed/event2/640/360', images: [], tags: ['活动分享'], authorName: '活动中心', createdAt: '2026-04-29', likeCount: 24, commentCount: 12, pinned: false },
  { id: 3, title: '给自己的一段话：慢一点也没关系', summary: '每日心声投稿，记录今天的真实感受。', content: '...', type: 'mood', cover: 'https://picsum.photos/seed/mood3/640/360', images: [], tags: ['每日心声'], authorName: '匿名用户', createdAt: '2026-04-28', likeCount: 66, commentCount: 21, pinned: false },
  { id: 4, title: '平台公告：发布页功能升级', summary: '支持多图预览与标签选择，发布流程更清晰。', content: '...', type: 'dynamic', cover: 'https://picsum.photos/seed/dyn4/640/360', images: [], tags: ['平台建议'], authorName: '产品团队', createdAt: '2026-04-27', likeCount: 18, commentCount: 4, pinned: false },
  { id: 5, title: 'AI工具：高效写活动介绍的模板', summary: '三步输出结构化活动文案，适合新手快速上手。', content: '...', type: 'ai', cover: 'https://picsum.photos/seed/ai5/640/360', images: [], tags: ['AI工具'], authorName: 'AI助手', createdAt: '2026-04-26', likeCount: 43, commentCount: 16, pinned: false },
  { id: 6, title: '周末团体活动复盘', summary: '本周参与数据与成员反馈汇总。', content: '...', type: 'dynamic', cover: 'https://picsum.photos/seed/dyn6/640/360', images: [], tags: ['活动分享'], authorName: '团体管理', createdAt: '2026-04-25', likeCount: 29, commentCount: 8, pinned: false }
]
