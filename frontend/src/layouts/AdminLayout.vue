<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <router-link to="/admin" class="admin-brand">Love Cube Admin</router-link>
      <p class="admin-sub">运营控制台</p>
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
  grid-template-columns: 252px 1fr;
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
  gap: 10px;
}

.admin-brand {
  font-size: 18px;
  font-weight: 800;
  text-decoration: none;
  color: #0f172a;
}

.admin-sub {
  font-size: 12px;
  color: #64748b;
}

.admin-nav {
  margin-top: 8px;
  display: grid;
  gap: 6px;
}

.admin-nav a,
.back-home {
  text-decoration: none;
  color: #475569;
  padding: 9px 10px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
}

.admin-nav a.router-link-exact-active {
  background: #fff0f4;
  color: #d9486d;
}

.back-home {
  margin-top: 8px;
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
    position: sticky;
    top: 0;
    z-index: 50;
    height: auto;
    border-right: 0;
    border-bottom: 1px solid #e2e8f0;
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 12px;
  }

  .admin-brand {
    font-size: 17px;
  }

  .admin-sub {
    display: none;
  }

  .admin-nav {
    margin-top: 2px;
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 6px;
  }

  .admin-nav a,
  .back-home {
    text-align: center;
    font-size: 12px;
    padding: 8px 6px;
  }
}

@media (max-width: 767px) {
  .admin-nav {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
