import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePlatformPath } from '@/composables/usePlatformPath.js'

/** 今日/每周/新人/账号任务「去完成」链接，在 /m/platform 与 /pc/platform 下走对应玩法路径 */
export function usePlayTaskRoutes() {
  const route = useRoute()
  const {
    platformPath,
    groupsPath,
    contentPath,
    articlesPath,
    mePath,
    isMobileShell,
    isPcShell
  } = usePlatformPath()

  const meBase = computed(() => {
    if (isMobileShell.value) return '/m/fellowship/me'
    if (isPcShell.value) return '/pc/platform/me'
    return '/me'
  })

  const codeToRoute = computed(() => {
    const content = platformPath('content')
    return {
      DAILY_LOGIN: platformPath('checkin'),
      DAILY_POST: platformPath('positive-share'),
      DAILY_COMMENT: isMobileShell.value
        ? platformPath('positive-share')
        : `${content}?type=mood`,
      DAILY_VIEW: isMobileShell.value ? platformPath('topics') : isPcShell.value ? articlesPath() : content,
      DAILY_LIKE: isMobileShell.value
        ? platformPath('positive-share')
        : `${content}?type=mood`
    }
  })

  const weeklyCodeToRoute = computed(() => ({
    WEEKLY_LOGIN_5: platformPath('checkin'),
    WEEKLY_POST_3: platformPath('positive-share'),
    WEEKLY_LIKE_10: platformPath('positive-share'),
    WEEKLY_COMMENT_3: platformPath('positive-share')
  }))

  const newcomerCodeToRoute = computed(() => ({
    NEWCOMER_D1_LOGIN: platformPath('checkin'),
    NEWCOMER_D2_POST: platformPath('positive-share'),
    NEWCOMER_D3_GROUP: groupsPath(),
    NEWCOMER_D4_LIKE: platformPath('positive-share'),
    NEWCOMER_D5_COMMENT: platformPath('positive-share'),
    NEWCOMER_D6_VIEW: isMobileShell.value ? platformPath('topics') : platformPath('content'),
    NEWCOMER_D7_STREAK: platformPath('checkin')
  }))

  function weeklyTaskRoute(code) {
    return weeklyCodeToRoute.value[code] || platformPath('tasks')
  }

  function newcomerTaskRoute(code) {
    return newcomerCodeToRoute.value[code] || platformPath('tasks')
  }

  function dailyTaskRoute(code) {
    return codeToRoute.value[code] || meBase.value
  }

  function accountTaskRoute(code) {
    const map = {
      ACC_AVATAR: mePath({ panel: 'edit' }),
      ACC_PHOTO: isMobileShell.value || isPcShell.value ? mePath({ panel: 'edit' }) : '/me/profile',
      ACC_BIO: mePath({ panel: 'edit' }),
      ACC_JOIN_GROUP: groupsPath(),
      ACC_FIRST_POST: platformPath('positive-share'),
      ACC_BIND_PHONE: isMobileShell.value || isPcShell.value ? mePath({ panel: 'settings' }) : '/me/security'
    }
    return map[code] || meBase.value
  }

  function mockTasks() {
    return [
      {
        title: '每日签到',
        exp: 2,
        current: 0,
        total: 1,
        done: false,
        to: platformPath('checkin')
      },
      {
        title: '完善资料',
        exp: 5,
        current: 0,
        total: 1,
        done: false,
        to: mePath({ panel: 'edit' })
      },
      {
        title: '发布动态',
        exp: 10,
        current: 0,
        total: 1,
        done: false,
        to: platformPath('positive-share')
      },
      {
        title: '浏览内容',
        exp: 1,
        current: 0,
        total: 3,
        done: false,
        to: isMobileShell.value ? platformPath('topics') : isPcShell.value ? articlesPath() : '/articles'
      }
    ]
  }

  return {
    isMobileShell,
    isPcShell,
    dailyTaskRoute,
    weeklyTaskRoute,
    newcomerTaskRoute,
    accountTaskRoute,
    mockTasks
  }
}
