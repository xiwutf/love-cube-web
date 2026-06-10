/** 平台团体分类（迭代 4 语义规范）；DB value 保持兼容，展示用 label */
export const PLATFORM_GROUP_CATEGORY_OPTIONS = [
  { label: '兴趣', value: 'interest' },
  { label: '生活', value: 'life' },
  { label: '成长', value: 'growth' },
  { label: '志愿服务', value: 'volunteer' },
  { label: '地区', value: 'region' }
]

/** 历史存量 category 值的展示映射 */
const LEGACY_CATEGORY_LABELS = {
  interest: '兴趣',
  life: '生活',
  growth: '成长',
  volunteer: '志愿服务',
  region: '地区',
  family: '生活',
  study: '成长',
  service: '志愿服务',
  church: '社群'
}

export function labelForGroupCategory(value) {
  if (value == null || value === '') return '团体'
  const key = String(value).trim().toLowerCase()
  const fromOptions = PLATFORM_GROUP_CATEGORY_OPTIONS.find((o) => o.value === key)
  if (fromOptions) return fromOptions.label
  return LEGACY_CATEGORY_LABELS[key] || String(value)
}

export function buildGroupCategoryLabelMap() {
  const map = { ...LEGACY_CATEGORY_LABELS }
  for (const item of PLATFORM_GROUP_CATEGORY_OPTIONS) {
    map[item.value] = item.label
  }
  return map
}
