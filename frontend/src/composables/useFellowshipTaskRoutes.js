import { useFellowshipNavBase } from '@/composables/useFellowshipNavBase.js'

/** 联谊层任务「去完成」路由（不与平台团体打通） */
export function useFellowshipTaskRoutes() {
  const { fellowshipPath } = useFellowshipNavBase()

  const dailyCodeToRoute = {
    DAILY_LOGIN: '/m/platform/checkin',
    DAILY_POST: fellowshipPath('/dynamic/publish'),
    DAILY_COMMENT: fellowshipPath('/dynamic'),
    DAILY_VIEW: fellowshipPath('/discover'),
    DAILY_LIKE: fellowshipPath('/match')
  }

  const weeklyCodeToRoute = {
    WEEKLY_LOGIN_5: '/m/platform/checkin',
    WEEKLY_POST_3: fellowshipPath('/dynamic/publish'),
    WEEKLY_LIKE_10: fellowshipPath('/match'),
    WEEKLY_COMMENT_3: fellowshipPath('/dynamic')
  }

  const newcomerCodeToRoute = {
    NEWCOMER_D1_LOGIN: '/m/platform/checkin',
    NEWCOMER_D2_POST: fellowshipPath('/dynamic/publish'),
    NEWCOMER_D3_GROUP: fellowshipPath('/match'),
    NEWCOMER_D4_LIKE: fellowshipPath('/match'),
    NEWCOMER_D5_COMMENT: fellowshipPath('/dynamic'),
    NEWCOMER_D6_VIEW: fellowshipPath('/discover'),
    NEWCOMER_D7_STREAK: '/m/platform/checkin'
  }

  function dailyTaskRoute(code) {
    return dailyCodeToRoute[code] || fellowshipPath('/tasks')
  }

  function weeklyTaskRoute(code) {
    return weeklyCodeToRoute[code] || fellowshipPath('/tasks')
  }

  function newcomerTaskRoute(code) {
    return newcomerCodeToRoute[code] || fellowshipPath('/tasks')
  }

  function accountTaskRoute(code) {
    const map = {
      ACC_AVATAR: fellowshipPath('/profile/edit'),
      ACC_PHOTO: fellowshipPath('/profile/edit'),
      ACC_BIO: fellowshipPath('/profile/edit'),
      ACC_JOIN_GROUP: fellowshipPath('/match'),
      ACC_FIRST_POST: fellowshipPath('/dynamic/publish'),
      ACC_BIND_PHONE: fellowshipPath('/change-phone')
    }
    return map[code] || fellowshipPath('/profile/edit')
  }

  return {
    dailyTaskRoute,
    weeklyTaskRoute,
    newcomerTaskRoute,
    accountTaskRoute,
    tasksPath: fellowshipPath('/tasks'),
    playCheckinPath: '/m/platform/checkin',
    playPositiveSharePath: '/m/platform/positive-share'
  }
}
