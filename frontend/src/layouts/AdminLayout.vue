<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <router-link to="/admin" class="admin-brand">Love Cube Admin</router-link>
      <nav class="admin-nav">
        <router-link to="/admin/announcements">公告管理</router-link>
        <router-link to="/admin/articles">资讯管理</router-link>
        <router-link to="/admin/events">活动管理</router-link>
        <router-link to="/admin/users">用户管理</router-link>
        <router-link to="/admin/verifications">认证审核</router-link>
        <router-link to="/admin/reports">举报处理</router-link>
      </nav>
      <router-link to="/" class="back-home">返回官网</router-link>
    </aside>
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px 1fr;
  background: #f3f6fa;
}

.admin-sidebar {
  padding: 18px 14px;
  border-right: 1px solid #e2e8f0;
  background: #fff;
  position: sticky;
  top: 0;
  height: 100vh;
  display: grid;
  align-content: start;
  gap: 14px;
}

.admin-brand {
  font-size: 18px;
  font-weight: 800;
  text-decoration: none;
  color: #0f172a;
}

.admin-nav {
  display: grid;
  gap: 6px;
}

.admin-nav a,
.back-home {
  text-decoration: none;
  color: #475569;
  padding: 8px 10px;
  border-radius: 10px;
  font-weight: 600;
}

.admin-nav a.router-link-exact-active {
  background: #fff0f4;
  color: #d9486d;
}

.back-home {
  color: #d9486d;
  background: #fff5f8;
}

.admin-main {
  min-height: 100vh;
}

@media (max-width: 1023px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    position: static;
    height: auto;
    border-right: 0;
    border-bottom: 1px solid #e2e8f0;
  }

  .admin-nav {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  }
}
</style>
