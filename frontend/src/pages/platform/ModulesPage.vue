<template>
  <div class="modules-page">

    <section class="mp-hero">
      <div class="mc">
        <p class="mp-kicker">PLATFORM MODULES</p>
        <h1 class="mp-title">模块中心</h1>
        <p class="mp-sub">连接人与服务的多功能平台，探索 Love Cube 全部功能入口</p>
      </div>
    </section>

    <section class="mp-body">
      <div class="mc">
        <div class="mp-grid">
          <component
            v-for="mod in modules"
            :key="mod.moduleKey"
            :is="mod.status === 'active' ? 'router-link' : 'div'"
            :to="mod.status === 'active' ? mod.entryRoute : undefined"
            class="mp-card"
            :class="{ 'mp-card--planned': mod.status === 'planned' }"
          >
            <div class="mp-card-top">
              <span class="mp-icon" :style="{ color: mod.color, background: mod.iconBg }">
                <svg viewBox="0 0 24 24" fill="currentColor" width="26" height="26">
                  <path :d="mod.icon" />
                </svg>
              </span>
              <span class="mp-badge" :class="mod.status === 'active' ? 'mp-badge--active' : 'mp-badge--planned'">
                {{ mod.status === 'active' ? '已开放' : '规划中' }}
              </span>
            </div>
            <h2 class="mp-name">{{ mod.name }}</h2>
            <p class="mp-desc">{{ mod.description }}</p>
            <div class="mp-footer">
              <span v-if="mod.status === 'active'" class="mp-enter">进入模块 →</span>
              <span v-else class="mp-coming">敬请期待</span>
            </div>
          </component>
        </div>
      </div>
    </section>

    <section class="mp-note">
      <div class="mc">
        <p class="mp-note-text">
          Love Cube 当前以联谊模块为首个重点能力，同时持续扩展内容、活动和后续模块。
          <router-link to="/about" class="mp-note-link">了解平台愿景 →</router-link>
        </p>
      </div>
    </section>

  </div>
</template>

<script setup>
const ICONS = {
  heart:    'M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z',
  calendar: 'M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 18H5V8h14v11z',
  article:  'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z',
  bell:     'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z',
  map:      'M20.5 3l-.16.03L15 5.1 9 3 3.36 4.9c-.21.07-.36.25-.36.48V20.5c0 .28.22.5.5.5l.16-.03L9 18.9l6 2.1 5.64-1.9c.21-.07.36-.25.36-.48V3.5c0-.28-.22-.5-.5-.5zM15 19l-6-2.11V5l6 2.11V19z',
  robot:    'M20 9V7c0-1.1-.9-2-2-2h-3c0-1.66-1.34-3-3-3S9 3.34 9 5H6c-1.1 0-2 .9-2 2v2c-1.66 0-3 1.34-3 3s1.34 3 3 3v4c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2v-4c1.66 0 3-1.34 3-3s-1.34-3-3-3zm-2 10H6V7h12v12zm-9-6c-.83 0-1.5-.67-1.5-1.5S8.17 10 9 10s1.5.67 1.5 1.5S9.83 13 9 13zm6 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm-3 2.5c-1.38 0-2.5-.56-2.5-1.25h5c0 .69-1.12 1.25-2.5 1.25z'
}

const modules = [
  {
    moduleKey:   'fellowship',
    name:        '联谊交友',
    description: '真实资料认证、私信互动、举报治理的联谊社交服务',
    status:      'active',
    entryRoute:  '/fellowship',
    icon:        ICONS.heart,
    iconBg:      '#fff0f4',
    color:       '#f45b7a',
    sort:        1
  },
  {
    moduleKey:   'events',
    name:        '活动中心',
    description: '平台活动、线下聚会、主题活动展示与报名',
    status:      'active',
    entryRoute:  '/events',
    icon:        ICONS.calendar,
    iconBg:      '#eff6ff',
    color:       '#1f4fd8',
    sort:        2
  },
  {
    moduleKey:   'articles',
    name:        '内容资讯',
    description: '平台资讯、生活内容、干货文章发布与阅读',
    status:      'active',
    entryRoute:  '/articles',
    icon:        ICONS.article,
    iconBg:      '#f0fdf4',
    color:       '#059669',
    sort:        3
  },
  {
    moduleKey:   'announcements',
    name:        '公告通知',
    description: '平台公告、规则说明、重要通知实时发布',
    status:      'active',
    entryRoute:  '/announcements',
    icon:        ICONS.bell,
    iconBg:      '#ecfeff',
    color:       '#0891b2',
    sort:        4
  },
  {
    moduleKey:   'local-services',
    name:        '本地服务',
    description: '后续可扩展招聘、二手车、生活服务等本地模块',
    status:      'planned',
    entryRoute:  null,
    icon:        ICONS.map,
    iconBg:      '#fffbeb',
    color:       '#d97706',
    sort:        5
  },
  {
    moduleKey:   'ai-tools',
    name:        'AI 工具',
    description: '后续接入智能助手、内容生成、效率工具等 AI 能力',
    status:      'planned',
    entryRoute:  null,
    icon:        ICONS.robot,
    iconBg:      '#f5f3ff',
    color:       '#7c3aed',
    sort:        6
  }
]
</script>

<style scoped>
.modules-page {
  --primary: #2563eb;
  --primary-dark: #1e40af;
  --accent:  #f45b7a;
  --text:    #111827;
  --text-2:  #374151;
  --text-3:  #64748b;
  --border:  #e5e7eb;
  --bg:      #f6f8fb;
  --card:    #ffffff;
  --radius-card: 16px;
  --radius-tile: 14px;
  --shadow-card: 0 14px 36px rgba(15, 23, 42, 0.06);
  --shadow-card-hover: 0 18px 40px rgba(15, 23, 42, 0.1);
  background: var(--bg);
  min-height: calc(100vh - 64px);
}

.mc {
  width: calc(100% - 48px);
  margin-left: auto;
  margin-right: auto;
}

.mp-hero {
  background: linear-gradient(140deg, #f9fbff 0%, #eef3ff 55%, #e9efff 100%);
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  margin-top: 0;
  padding: 22px 0 18px;
  box-shadow: none;
}

.mp-kicker {
  margin: 0 0 6px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.2em;
  color: var(--primary);
  text-transform: uppercase;
}

.mp-title {
  margin: 0 0 6px;
  font-size: clamp(24px, 2.6vw, 34px);
  font-weight: 800;
  color: var(--text);
  letter-spacing: 0;
  line-height: 1.14;
}

.mp-sub {
  margin: 0;
  font-size: 14px;
  color: var(--text-3);
  line-height: 1.6;
  max-width: 720px;
}

.mp-body {
  padding: 16px 0 56px;
}

.mp-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 22px;
}

.mp-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  padding: 28px 24px;
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  gap: 14px;
  box-shadow: var(--shadow-card);
  transition: transform 0.18s, box-shadow 0.18s, border-color 0.18s;
  min-height: 236px;
}

.mp-card:not(.mp-card--planned):hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
  border-color: #c7d2fe;
}

.mp-card--planned {
  cursor: default;
  opacity: 0.6;
}

.mp-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.mp-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-tile);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.mp-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
}

.mp-badge--active {
  background: #dcfce7;
  color: #15803d;
}

.mp-badge--planned {
  background: #f1f5f9;
  color: #64748b;
}

.mp-name {
  font-size: 19px;
  font-weight: 800;
  color: var(--text);
  margin: 0;
  letter-spacing: 0;
}

.mp-desc {
  font-size: 15px;
  color: var(--text-3);
  line-height: 1.75;
  margin: 0;
  flex: 1;
}

.mp-footer {
  margin-top: auto;
  padding-top: 8px;
}

.mp-enter {
  font-size: 14px;
  font-weight: 800;
  color: var(--primary-dark);
}

.mp-coming {
  font-size: 14px;
  color: var(--text-3);
  font-style: italic;
}

.mp-note {
  background: var(--card);
  border-top: 1px solid var(--border);
  padding: 32px 0;
}

.mp-note-text {
  margin: 0;
  font-size: 15px;
  color: var(--text-3);
  line-height: 1.7;
  text-align: center;
}

.mp-note-link {
  color: var(--primary-dark);
  text-decoration: none;
  font-weight: 600;
  margin-left: 8px;
}
.mp-note-link:hover { text-decoration: underline; }

@media (max-width: 1199px) {
  .mc { width: calc(100% - 32px); }
  .mp-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (min-width: 1360px) {
  .mp-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .mc { width: calc(100% - 24px); }
  .mp-hero {
    margin-top: 0;
    border-radius: 10px;
    padding: 16px 0 14px;
  }
  .mp-title { font-size: clamp(22px, 8vw, 30px); }
  .mp-sub { font-size: 13px; line-height: 1.55; }
  .mp-body { padding: 12px 0 40px; }
  .mp-grid { grid-template-columns: 1fr; gap: 14px; }
  .mp-card { padding: 24px; min-height: auto; }
  .mp-name { font-size: 17px; }
}
</style>
