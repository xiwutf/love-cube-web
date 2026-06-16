/** 2026 端午社区活动窗口（心声 + 联谊动态共用） */

export const DRAGON_BOAT_2026_WINDOW = {
  start: '2026-06-16',
  end: '2026-06-22',
}

function parseDateOnly(iso) {
  const [y, m, d] = iso.split('-').map(Number)
  return new Date(y, m - 1, d)
}

export function isDragonBoat2026Active(date = new Date()) {
  const day = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const start = parseDateOnly(DRAGON_BOAT_2026_WINDOW.start)
  const end = parseDateOnly(DRAGON_BOAT_2026_WINDOW.end)
  return day >= start && day < end
}
