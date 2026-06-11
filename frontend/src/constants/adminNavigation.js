/**
 * 管理后台：左侧导航、总览快捷入口、页面标题共用配置。
 * permission 为 null 表示登录后台即可可见。
 */

export const ADMIN_NAV_GROUPS = [
  {
    id: 'home',
    label: '首页',
    icon: '首',
    items: [
      {
        to: '/admin',
        label: '首页',
        icon: '首',
        permission: null,
        staffFullConsole: true,
        blurb: '运营驾驶舱'
      }
    ]
  },
  {
    id: 'platform',
    label: '平台运营',
    icon: '平',
    items: [
      {
        to: '/admin/announcements',
        label: '公告管理',
        icon: '告',
        permission: 'content.announcement.manage',
        blurb: '发布或下架公告，前台同步展示'
      },
      {
        to: '/admin/articles',
        label: '资讯管理',
        icon: '文',
        permission: 'content.article.manage',
        blurb: '编辑文章与推荐位'
      },
      {
        to: '/admin/events',
        label: '活动管理',
        icon: '活',
        permission: 'content.event.manage',
        blurb: '创建活动、报名与签到'
      },
      {
        to: '/admin/home-config',
        label: '首页配置',
        icon: '配',
        permission: 'system.manage',
        blurb: '首页区块与推荐内容'
      },
      {
        to: '/admin/local-resources',
        label: '本地资源',
        icon: '源',
        permission: 'content.manage',
        blurb: '维护本地资源并控制发布状态'
      }
    ]
  },
  {
    id: 'review',
    label: '审核中心',
    icon: '审',
    items: [
      {
        to: '/admin/verifications',
        label: '资料审核',
        icon: '资',
        permission: 'review.manage',
        blurb: '实名与资料认证审核'
      },
      {
        to: '/admin/fellowship-dynamics',
        label: '动态审核',
        icon: '动',
        permission: 'review.manage',
        blurb: '联谊用户动态与点赞明细'
      },
      {
        to: '/admin/positive-shares',
        label: '心声审核',
        icon: '心',
        permission: 'review.manage',
        blurb: '审核用户发布的心声内容'
      },
      {
        to: '/admin/help-requests',
        label: '互助审核',
        icon: '助',
        permission: 'review.manage',
        blurb: '审核互助广场需求'
      },
      {
        to: '/admin/reports',
        label: '举报处理',
        icon: '举',
        permission: 'review.manage',
        blurb: '查看举报并处置违规'
      }
    ]
  },
  {
    id: 'users',
    label: '用户与团体',
    icon: '用',
    items: [
      {
        to: '/admin/users',
        label: '用户管理',
        icon: '用',
        permission: 'user.manage',
        blurb: '查询、封禁与账号状态'
      },
      {
        to: '/admin/invites',
        label: '邀请记录',
        icon: '邀',
        permission: 'user.manage',
        blurb: '查看邀请发放与注册使用情况'
      },
      {
        to: '/admin/platform/groups',
        label: '全站团体',
        icon: '团',
        permission: 'group.manage.all',
        blurb: '平台级团体列表与新建'
      },
      {
        to: '/admin/my-groups',
        label: '我的团体',
        icon: '我',
        permission: 'group.manage.own',
        blurb: '我负责的团体维护'
      }
    ]
  },
  {
    id: 'data',
    label: '数据统计',
    icon: '数',
    items: [
      {
        to: '/admin/analytics',
        label: '访客分析',
        icon: '数',
        permission: 'system.manage',
        blurb: '流量与访问概况'
      }
    ]
  },
  {
    id: 'system',
    label: '系统管理',
    icon: '系',
    items: [
      {
        to: '/admin/modules',
        label: '模块管理',
        icon: '模',
        permission: 'system.manage',
        blurb: '功能模块显隐与配置'
      },
      {
        to: '/admin/feedbacks',
        label: '用户调研',
        icon: '卷',
        permission: 'content.manage',
        blurb: '查看用户体验调研问卷'
      }
    ]
  }
]

export function filterAdminNavGroups(groups, hasPermission, options = {}) {
  const hideStaffFullConsoleItems = Boolean(options.hideStaffFullConsoleItems)
  return groups
    .map(group => ({
      ...group,
      items: group.items.filter((item) => {
        if (hideStaffFullConsoleItems && item.staffFullConsole) return false
        return item.permission == null || hasPermission(item.permission)
      })
    }))
    .filter(group => group.items.length > 0)
}

const SECTION_TITLES = {
  '/admin': '总览面板',
  '/admin/announcements': '平台公告管理',
  '/admin/articles': '平台资讯管理',
  '/admin/positive-shares': '心声审核',
  '/admin/help-requests': '互助审核',
  '/admin/events': '活动管理',
  '/admin/users': '用户管理',
  '/admin/invites': '邀请记录',
  '/admin/verifications': '认证审核',
  '/admin/reports': '举报处理',
  '/admin/fellowship-dynamics': '联谊动态',
  '/admin/feedbacks': '用户调研',
  '/admin/local-resources': '本地资源管理',
  '/admin/modules': '模块管理',
  '/admin/home-config': '首页配置',
  '/admin/analytics': '访客分析',
  '/admin/platform/groups': '全站团体',
  '/admin/platform/groups/create': '新建团体',
  '/admin/my-groups': '我的团体',
  '/admin/403': '无权限'
}

const PAGE_SUBTITLES = {
  '/admin':
    '运营驾驶舱聚合今日概览、待办工作台、运营数据、快捷入口与最近动态。',
  '/admin/announcements': '编辑平台公告标题与正文，控制是否在官网展示。',
  '/admin/articles': '撰写或维护平台资讯正文、封面与推荐状态。',
  '/admin/events': '创建活动、维护时间与名额，查看报名情况。',
  '/admin/feedbacks': '查看用户提交的体验调研与建议。',
  '/admin/local-resources': '维护本地资源内容并控制发布、下架状态。',
  '/admin/positive-shares': '审核用户“心声”内容，通过或驳回。',
  '/admin/help-requests': '审核用户提交的互助需求，通过后出现在互助广场。',
  '/admin/verifications': '处理实名与资料类认证申请。',
  '/admin/reports': '查看举报详情，记录处理结果。',
  '/admin/fellowship-dynamics': '浏览联谊动态正文，展开查看点赞用户与时间。',
  '/admin/users': '搜索用户、查看状态，必要时限制账号行为。',
  '/admin/invites': '按邀请人、被邀请人、邀请码和时间查询邀请注册记录。',
  '/admin/platform/groups': '浏览全站团体，可新建或进入某一团体编辑页。',
  '/admin/platform/groups/create': '填写团体资料并创建；保存后可在列表中继续维护。',
  '/admin/my-groups': '管理您作为负责人的团体列表。',
  '/admin/modules': '控制前台模块是否对用户可见及相关参数。',
  '/admin/home-config': '调整官网首页各区块内容与排序。',
  '/admin/analytics': '查看访问量与页面数据。',
  '/admin/403': ''
}

export function resolveAdminSectionTitle(path) {
  if (SECTION_TITLES[path]) return SECTION_TITLES[path]
  if (path.startsWith('/admin/platform/groups/create')) return '新建团体'
  if (/^\/admin\/platform\/groups\/[^/]+$/.test(path)) return '团体详情'
  if (/^\/admin\/my-groups\/[^/]+$/.test(path)) return '团体详情'
  return '管理中心'
}

export function getAdminPageSubtitle(path) {
  if (Object.prototype.hasOwnProperty.call(PAGE_SUBTITLES, path)) {
    return PAGE_SUBTITLES[path]
  }
  if (path.startsWith('/admin/platform/groups/create')) {
    return '填写团体资料并创建；保存后可在全站团体列表中继续编辑。'
  }
  if (/^\/admin\/platform\/groups\/[^/]+$/.test(path)) {
    return '维护该团体的资料、成员与展示内容。'
  }
  if (/^\/admin\/my-groups\/[^/]+$/.test(path)) {
    return '维护您所负责团体的资料与内容。'
  }
  return ''
}
