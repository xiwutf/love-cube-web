<template>
  <div class="modules-admin">
    <div class="ma-header">
      <h2 class="ma-title">模块管理</h2>
      <p class="ma-desc">平台模块配置雏形，当前为静态展示。后续可接入 modules 数据表实现动态管理。</p>
    </div>

    <div class="ma-table-wrap">
      <table class="ma-table">
        <thead>
          <tr>
            <th>排序</th>
            <th>模块名称</th>
            <th>状态</th>
            <th>入口路由</th>
            <th>是否展示</th>
            <th>备注</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="mod in modules" :key="mod.moduleKey">
            <td class="ma-sort">{{ mod.sort }}</td>
            <td class="ma-name">
              <span class="ma-dot" :style="{ background: mod.color }"></span>
              {{ mod.name }}
            </td>
            <td>
              <span class="ma-status" :class="mod.status === 'active' ? 'ma-status--active' : 'ma-status--planned'">
                {{ mod.status === 'active' ? '已开放' : '规划中' }}
              </span>
            </td>
            <td class="ma-route">{{ mod.entryRoute || '—' }}</td>
            <td>
              <span class="ma-show" :class="mod.visible ? 'ma-show--on' : 'ma-show--off'">
                {{ mod.visible ? '展示' : '隐藏' }}
              </span>
            </td>
            <td class="ma-remark">{{ mod.remark }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="ma-tip">* 当前为平台模块配置雏形，后续可接入 modules 数据表实现动态排序与状态切换。</p>
  </div>
</template>

<script setup>
const modules = [
  { moduleKey: 'fellowship',     name: '联谊交友', status: 'active',  entryRoute: '/fellowship',    visible: true,  sort: 1, color: '#f45b7a', remark: '首个重点模块，已全面上线' },
  { moduleKey: 'events',         name: '活动中心', status: 'active',  entryRoute: '/events',         visible: true,  sort: 2, color: '#1f4fd8', remark: '活动发布与报名' },
  { moduleKey: 'articles',       name: '内容资讯', status: 'active',  entryRoute: '/articles',       visible: true,  sort: 3, color: '#059669', remark: '文章与资讯内容' },
  { moduleKey: 'announcements',  name: '公告通知', status: 'active',  entryRoute: '/announcements',  visible: true,  sort: 4, color: '#0891b2', remark: '平台公告与规则' },
  { moduleKey: 'local-services', name: '本地服务', status: 'planned', entryRoute: null,              visible: true,  sort: 5, color: '#d97706', remark: '后续扩展：招聘、二手、生活服务' },
  { moduleKey: 'ai-tools',       name: 'AI 工具',  status: 'planned', entryRoute: null,              visible: true,  sort: 6, color: '#7c3aed', remark: '后续接入智能工具与 AI 能力' },
]
</script>

<style scoped>
.modules-admin {
  padding: 32px 0;
}

.ma-header {
  margin-bottom: 28px;
}

.ma-title {
  font-size: 22px;
  font-weight: 900;
  color: #0f172a;
  margin: 0 0 8px;
}

.ma-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  line-height: 1.6;
}

.ma-table-wrap {
  overflow-x: auto;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
}

.ma-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.ma-table th {
  background: #f8fafc;
  color: #64748b;
  font-weight: 700;
  font-size: 12px;
  text-align: left;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  white-space: nowrap;
}

.ma-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  color: #374151;
  vertical-align: middle;
}

.ma-table tr:last-child td {
  border-bottom: none;
}

.ma-sort {
  color: #94a3b8;
  font-size: 13px;
  font-weight: 700;
  text-align: center;
}

.ma-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: #0f172a;
}

.ma-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.ma-status {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 999px;
}

.ma-status--active  { background: #dcfce7; color: #15803d; }
.ma-status--planned { background: #f1f5f9; color: #64748b; }

.ma-route {
  font-family: monospace;
  font-size: 13px;
  color: #475569;
}

.ma-show {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 999px;
}

.ma-show--on  { background: #eff6ff; color: #1d4ed8; }
.ma-show--off { background: #f1f5f9; color: #94a3b8; }

.ma-remark {
  color: #94a3b8;
  font-size: 13px;
}

.ma-tip {
  margin-top: 16px;
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.6;
}
</style>
