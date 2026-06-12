<template>
  <section class="admin-page admin-page-users">
    <section class="platform-card">
      <h1 class="platform-title">用户管理</h1>
      <p class="platform-subtitle">展示用户列表并支持角色与状态管理</p>
    </section>

    <section class="platform-card users-stats-card">
      <p class="platform-text">
        当前 {{ filteredUsers.length }} / {{ users.length }} 位用户
        （第 {{ currentPage }} / {{ totalPages }} 页）
      </p>
      <div class="users-overview">
        <span class="overview-chip">已认证 {{ stats.verifiedCount }}</span>
        <span class="overview-chip">男性 {{ stats.maleCount }}</span>
        <span class="overview-chip">女性 {{ stats.femaleCount }}</span>
        <span class="overview-chip">未知性别 {{ stats.unknownGenderCount }}</span>
        <span class="overview-chip">已开通联谊 {{ stats.fellowshipEnabledCount }}</span>
        <span class="overview-chip">已上传图片 {{ stats.hasPhotosCount }}</span>
        <span class="overview-chip">近7天活跃 {{ stats.recentLoginCount }}</span>
      </div>
    </section>

    <section v-if="growthDashboard" class="platform-card growth-dashboard-card">
      <h2 class="growth-dashboard-title">联谊成长概览</h2>
      <div class="growth-dashboard-grid">
        <article class="growth-dashboard-card">
          <h3>资料完成度分布</h3>
          <ul class="growth-dashboard-list">
            <li><span>0–39%</span><strong>{{ growthDashboard.completionBuckets?.['0_39'] ?? 0 }}</strong></li>
            <li><span>40–59%</span><strong>{{ growthDashboard.completionBuckets?.['40_59'] ?? 0 }}</strong></li>
            <li><span>60–79%</span><strong>{{ growthDashboard.completionBuckets?.['60_79'] ?? 0 }}</strong></li>
            <li><span>80–99%</span><strong>{{ growthDashboard.completionBuckets?.['80_99'] ?? 0 }}</strong></li>
            <li><span>100%</span><strong>{{ growthDashboard.completionBuckets?.['100'] ?? 0 }}</strong></li>
          </ul>
        </article>
        <article class="growth-dashboard-card">
          <h3>认证状态</h3>
          <ul class="growth-dashboard-list">
            <li><span>未认证</span><strong>{{ growthDashboard.verificationStats?.none ?? 0 }}</strong></li>
            <li><span>仅实名</span><strong>{{ growthDashboard.verificationStats?.realnameOnly ?? 0 }}</strong></li>
            <li><span>仅真人</span><strong>{{ growthDashboard.verificationStats?.photoOnly ?? 0 }}</strong></li>
            <li><span>双认证</span><strong>{{ growthDashboard.verificationStats?.both ?? 0 }}</strong></li>
          </ul>
        </article>
        <article class="growth-dashboard-card">
          <h3>联谊状态</h3>
          <ul class="growth-dashboard-list">
            <li><span>未开通</span><strong>{{ growthDashboard.fellowshipStats?.notEnabled ?? 0 }}</strong></li>
            <li><span>已开通</span><strong>{{ growthDashboard.fellowshipStats?.enabled ?? 0 }}</strong></li>
            <li><span>已进推荐池</span><strong>{{ growthDashboard.fellowshipStats?.matchVisible ?? 0 }}</strong></li>
          </ul>
        </article>
        <article class="growth-dashboard-card">
          <h3>主要缺失项</h3>
          <ul class="growth-dashboard-list">
            <li><span>缺头像</span><strong>{{ growthDashboard.missingItemStats?.avatar ?? 0 }}</strong></li>
            <li><span>缺年龄</span><strong>{{ growthDashboard.missingItemStats?.age ?? 0 }}</strong></li>
            <li><span>缺地区</span><strong>{{ growthDashboard.missingItemStats?.city ?? 0 }}</strong></li>
            <li><span>缺认证</span><strong>{{ growthDashboard.missingItemStats?.verification ?? 0 }}</strong></li>
            <li><span>缺照片</span><strong>{{ growthDashboard.missingItemStats?.photos ?? 0 }}</strong></li>
            <li><span>缺简介</span><strong>{{ growthDashboard.missingItemStats?.bio ?? 0 }}</strong></li>
            <li><span>未开通联谊</span><strong>{{ growthDashboard.missingItemStats?.fellowshipEnabled ?? 0 }}</strong></li>
          </ul>
        </article>
      </div>
    </section>

    <section class="platform-card admin-filter-card">
      <div class="admin-filter-bar">
        <input
          v-model.trim="filters.keyword"
          class="admin-filter-input"
          type="text"
          placeholder="搜索用户名 / 手机号 / 用户ID"
        >
        <select v-model="filters.role" class="admin-select">
          <option value="">全部角色</option>
          <option value="user">普通用户</option>
          <option value="admin">管理员</option>
        </select>
        <select v-model="filters.status" class="admin-select">
          <option value="">全部状态</option>
          <option value="active">active</option>
          <option value="banned">banned</option>
        </select>
        <select v-model="filters.fellowshipEnabled" class="admin-select">
          <option value="">联谊全部</option>
          <option value="enabled">已开通</option>
          <option value="disabled">未开通</option>
        </select>
        <select v-model="filters.gender" class="admin-select">
          <option value="">性别全部</option>
          <option value="male">男性</option>
          <option value="female">女性</option>
          <option value="unknown">未知</option>
        </select>
        <select v-model="filters.hasPhotos" class="admin-select">
          <option value="">图片全部</option>
          <option value="yes">已上传图片</option>
          <option value="no">未上传图片</option>
        </select>
        <select v-model="filters.completionBucket" class="admin-select">
          <option value="">完成度全部</option>
          <option value="0_39">0–39%</option>
          <option value="40_59">40–59%</option>
          <option value="60_79">60–79%</option>
          <option value="80_99">80–99%</option>
          <option value="100">100%</option>
        </select>
        <select v-model="filters.verificationFilter" class="admin-select">
          <option value="">认证全部</option>
          <option value="none">未认证</option>
          <option value="realnameOnly">实名</option>
          <option value="photoOnly">真人</option>
          <option value="both">双认证</option>
        </select>
        <select v-model="filters.fellowshipOpsFilter" class="admin-select">
          <option value="">联谊运营全部</option>
          <option value="notEnabled">未开通</option>
          <option value="enabled">已开通</option>
          <option value="matchVisible">已进推荐池</option>
        </select>
        <select v-model="filters.lowActive" class="admin-select">
          <option value="">活跃全部</option>
          <option value="yes">低活跃（近7天未登录）</option>
        </select>
        <select v-model="filters.missingItem" class="admin-select">
          <option value="">缺失项全部</option>
          <option value="avatar">缺头像</option>
          <option value="age">缺年龄</option>
          <option value="city">缺地区</option>
          <option value="verification">缺认证</option>
          <option value="photos">缺照片</option>
          <option value="bio">缺简介</option>
          <option value="fellowship">未开通联谊</option>
        </select>
        <button class="admin-btn" type="button" @click="resetFilters">重置筛选</button>
      </div>
    </section>

    <section class="platform-card outreach-card">
      <h2 class="outreach-title">运营触达</h2>
      <p class="platform-text">
        当前筛选命中 <strong>{{ filteredUsers.length }}</strong> 人
        <span v-if="suggestedOutreachSegment">，推荐动作：{{ suggestedOutreachLabel }}</span>
      </p>
      <div class="outreach-actions">
        <button class="admin-btn" type="button" :disabled="outreachLoading" @click="openOutreachDialog(false)">
          预览触达用户
        </button>
        <button class="admin-btn primary" type="button" :disabled="outreachLoading" @click="openOutreachDialog(true)">
          发送运营提醒
        </button>
      </div>
    </section>

    <section v-if="growthCampaigns.length" class="platform-card campaign-history-card">
      <h2 class="outreach-title">运营活动记录</h2>
      <div class="admin-table-wrap">
        <table class="admin-table">
          <thead>
            <tr>
              <th>活动名称</th>
              <th>分群</th>
              <th>模板</th>
              <th>发送</th>
              <th>打开</th>
              <th>点击</th>
              <th>完成</th>
              <th>打开率</th>
              <th>点击率</th>
              <th>完成率</th>
              <th>点击→完成</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in growthCampaigns" :key="item.id">
              <td>{{ item.name }}</td>
              <td>{{ segmentLabel(item.segment) }}</td>
              <td>{{ templateLabel(item.templateCode) }}</td>
              <td>{{ item.sentCount }}</td>
              <td>{{ item.openedCount }}</td>
              <td>{{ item.clickedCount }}</td>
              <td>{{ item.completedCount }}</td>
              <td>{{ formatFunnelRate(item.openRate) }}</td>
              <td>{{ formatFunnelRate(item.clickRate) }}</td>
              <td>{{ formatFunnelRate(item.completeRate) }}</td>
              <td>{{ formatFunnelRate(item.clickToCompleteRate) }}</td>
              <td><span class="admin-tag" :class="campaignStatusClass(item.status)">{{ item.status }}</span></td>
              <td>{{ formatDate(item.createdAt) }}</td>
              <td class="outreach-row-actions">
                <button class="admin-btn" type="button" @click="openCampaignDetail(item)">查看详情</button>
                <button class="admin-btn" type="button" :disabled="campaignRefreshingId === item.id" @click="refreshCampaignConversion(item)">
                  刷新转化
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="admin-table-wrap admin-desktop-only">
      <table class="admin-table users-table">
        <colgroup>
          <col class="cg-actions">
          <col class="cg-avatar">
          <col class="cg-user">
          <col class="cg-phone">
          <col class="cg-tag">
          <col class="cg-age">
          <col class="cg-completion">
          <col class="cg-level">
          <col class="cg-tag">
          <col class="cg-tag">
          <col class="cg-tag">
          <col class="cg-tag">
          <col class="cg-tag">
          <col class="cg-benefits">
          <col class="cg-missing">
          <col class="cg-role">
          <col class="cg-tag">
          <col class="cg-location">
          <col class="cg-login">
          <col class="cg-date">
        </colgroup>
        <thead>
          <tr>
            <th class="col-actions-head">操作</th>
            <th class="col-avatar-head">头像</th>
            <th class="col-user-head">用户</th>
            <th class="col-phone-head">手机</th>
            <th>性别</th>
            <th class="col-age">年龄</th>
            <th>资料完成度</th>
            <th>等级</th>
            <th>徽章</th>
            <th>认证</th>
            <th>联谊状态</th>
            <th>曝光加成</th>
            <th>已解锁权益</th>
            <th>待补项</th>
            <th class="col-role-head">角色</th>
            <th>状态</th>
            <th class="col-location">地区</th>
            <th class="col-login">最近登录</th>
            <th class="col-date">注册时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in paginatedUsers" :key="item.userId">
            <td class="col-actions">
              <button
                class="admin-btn primary users-table-detail-btn"
                type="button"
                :title="`用户详情：${item.username || item.userId}`"
                @click="openDetailDialog(item)"
              >
                详情
              </button>
            </td>
            <td class="col-avatar">
              <img
                v-if="rowAvatarUrl(item)"
                class="users-table-avatar-img"
                :src="rowAvatarUrl(item)"
                alt=""
                loading="lazy"
              >
              <span v-else class="users-table-avatar-empty" aria-hidden="true" />
            </td>
            <td class="col-user" :title="item.username || `用户${item.userId}`">
              {{ item.username || `用户${item.userId}` }}
            </td>
            <td class="col-phone" :title="item.phone || '无手机号'">{{ item.phone || '无手机号' }}</td>
            <td><span class="admin-tag" :class="genderTagClass(item.gender)">{{ genderLabel(item.gender) }}</span></td>
            <td class="col-age">{{ formatAdminAge(item.age) }}</td>
            <td class="col-completion">
              <div class="users-completion-cell">
                <div class="users-completion-bar" role="progressbar" :aria-valuenow="item.profileCompletionRate" aria-valuemin="0" aria-valuemax="100">
                  <span class="users-completion-fill" :style="{ width: `${item.profileCompletionRate}%` }" />
                </div>
                <span class="users-completion-text">{{ item.profileCompletionRate }}%</span>
              </div>
            </td>
            <td class="col-level" :title="item.growthTitle || '-'">
              Lv{{ item.growthLevel || 1 }}
              <span v-if="item.growthTitle" class="users-level-title">{{ item.growthTitle }}</span>
            </td>
            <td>{{ item.badgeCount || 0 }}</td>
            <td><span class="admin-tag" :class="verificationTierTagClass(item.verificationTier)">{{ verificationTierLabel(item) }}</span></td>
            <td><span class="admin-tag" :class="fellowshipOpsTagClass(item)">{{ fellowshipOpsLabel(item) }}</span></td>
            <td>{{ formatExposureBoost(item.exposureBoostPercent) }}</td>
            <td class="col-benefits" :title="formatBenefitsFull(item.unlockedBenefits)">{{ formatBenefitsShort(item.unlockedBenefits) }}</td>
            <td class="col-missing" :title="formatMissingFull(item.profileMissingItems)">{{ formatMissingShort(item.profileMissingItems) }}</td>
            <td class="col-role">
              <select v-model="item.role" class="admin-select" :disabled="!canEditRole(item)">
                <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </td>
            <td><span class="admin-tag" :class="item.status || 'active'">{{ item.status || 'active' }}</span></td>
            <td class="col-location" :title="item.location || '-'">{{ item.location || '-' }}</td>
            <td class="col-login" :title="formatDate(item.lastLoginAt)">{{ formatDate(item.lastLoginAt) }}</td>
            <td class="col-date" :title="formatDate(item.createdAt)">{{ formatDate(item.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!loading && !filteredUsers.length" description="暂无匹配用户" />
    </section>

    <div class="admin-list admin-mobile-only">
      <article v-for="item in paginatedUsers" :key="item.userId" class="admin-row">
        <div class="admin-toolbar users-mobile-actions-top">
          <button class="admin-btn primary" type="button" @click="openDetailDialog(item)">用户详情</button>
        </div>
        <div class="admin-row-head">
          <div class="users-mobile-head-left">
            <img
              v-if="rowAvatarUrl(item)"
              class="users-mobile-avatar"
              :src="rowAvatarUrl(item)"
              alt=""
              loading="lazy"
            >
            <span v-else class="users-mobile-avatar users-mobile-avatar-empty" aria-hidden="true" />
            <strong>{{ item.username || `用户${item.userId}` }}</strong>
          </div>
          <span class="admin-tag" :class="item.status || 'active'">{{ item.status || 'active' }}</span>
        </div>
        <p class="admin-row-meta">{{ item.phone || '无手机号' }}</p>
        <p class="admin-row-meta">性别：{{ genderLabel(item.gender) }} · 年龄：{{ formatAdminAge(item.age) }}</p>
        <p class="admin-row-meta">完成度：{{ item.profileCompletionRate }}% · Lv{{ item.growthLevel || 1 }} · 徽章 {{ item.badgeCount || 0 }}</p>
        <p class="admin-row-meta">认证：{{ verificationTierLabel(item) }} · 联谊：{{ fellowshipOpsLabel(item) }} · 曝光 {{ formatExposureBoost(item.exposureBoostPercent) }}</p>
        <p class="admin-row-meta">待补：{{ formatMissingShort(item.profileMissingItems) }}</p>
        <p class="admin-row-meta">地区：{{ item.location || '-' }} · 登录：{{ formatDate(item.lastLoginAt) }}</p>
        <p class="admin-row-meta">注册：{{ formatDate(item.createdAt) }}</p>
      </article>
      <van-empty v-if="!loading && !filteredUsers.length" description="暂无匹配用户" />
    </div>

    <AdminDetailDialogShell
      :visible="detailDialog.visible"
      :title="detailDialog.title"
      :loading="detailDialog.loading"
      :max-width="960"
      @update:visible="onDetailDialogVisible"
    >
      <section class="user-detail-wrap">
        <div class="user-detail-meta">
          <div class="user-detail-avatar-col">
            <img
              v-if="detailAvatarDisplayUrl"
              class="user-detail-avatar-img"
              :src="detailAvatarDisplayUrl"
              :alt="`${detailDialog.user?.username || '用户'}的头像`"
            >
            <div v-else class="user-detail-avatar-placeholder">无头像</div>
          </div>
          <div class="user-detail-meta-fields">
            <p><strong>用户ID：</strong>{{ detailDialog.user?.userId || '-' }}</p>
            <p><strong>用户名：</strong>{{ detailDialog.user?.username || '-' }}</p>
            <p><strong>手机号：</strong>{{ detailDialog.user?.phone || '-' }}</p>
            <p><strong>年龄：</strong>{{ formatAdminAge(detailDialog.user?.age) }}</p>
            <p><strong>角色：</strong>{{ detailDialog.user?.role || '-' }}</p>
            <p><strong>状态：</strong>{{ detailDialog.user?.status || '-' }}</p>
            <p><strong>联谊：</strong>{{ detailDialog.user?.fellowshipEnabled ? '已开通' : '未开通' }}</p>
          </div>
        </div>
        <section v-if="detailDialog.user" class="user-growth-insight">
          <h4>联谊成长与权益</h4>
          <div class="user-growth-grid">
            <p><strong>资料完成度：</strong>{{ detailDialog.user.profileCompletionRate }}%</p>
            <p><strong>曝光加成：</strong>{{ formatExposureBoost(detailDialog.user.exposureBoostPercent) }}</p>
            <p><strong>当前等级：</strong>Lv{{ detailDialog.user.growthLevel || 1 }} {{ detailDialog.user.growthTitle || '' }}</p>
            <p><strong>认证状态：</strong>{{ verificationTierLabel(detailDialog.user) }}</p>
            <p><strong>联谊状态：</strong>{{ fellowshipOpsLabel(detailDialog.user) }}</p>
            <p><strong>徽章数：</strong>{{ detailDialog.user.badgeCount || 0 }}</p>
          </div>
          <p><strong>待补项：</strong>{{ formatMissingFull(detailDialog.user.profileMissingItems) }}</p>
          <p><strong>当前徽章：</strong>{{ formatBadgeList(detailDialog.user.badges, detailDialog.user.verifiedBadges) }}</p>
          <p><strong>已解锁权益：</strong>{{ formatBenefitsFull(detailDialog.user.unlockedBenefits) }}</p>
          <p><strong>下一步可解锁：</strong>{{ formatBenefitsFull(detailDialog.user.nextBenefits) }}</p>
        </section>
        <div v-if="detailDialog.user" class="user-detail-actions">
          <div class="user-detail-action-card">
            <p class="user-detail-action-title">账号设置</p>
            <div class="user-detail-role-row">
              <select v-model="detailDialog.user.role" class="admin-select" :disabled="!canEditRole(detailDialog.user)">
                <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
              <button
                class="admin-btn"
                type="button"
                :disabled="savingRoleUserId === detailDialog.user.userId || !canEditRole(detailDialog.user)"
                @click="saveRole(detailDialog.user)"
              >
                {{ savingRoleUserId === detailDialog.user.userId ? '保存中...' : '保存角色' }}
              </button>
            </div>
            <div class="user-action-group user-detail-actions-grid">
              <button class="admin-btn" type="button" @click="setStatus(detailDialog.user, 'active')">解封</button>
              <button class="admin-btn" type="button" @click="setStatus(detailDialog.user, 'banned')">封禁</button>
              <button class="admin-btn" type="button" @click="setFellowship(detailDialog.user, true)">开通联谊</button>
              <button class="admin-btn" type="button" @click="setFellowship(detailDialog.user, false)">关闭联谊</button>
            </div>
          </div>
          <div class="user-detail-action-card user-detail-action-card-danger">
            <p class="user-detail-action-title">高风险操作</p>
            <p class="user-detail-action-hint">执行前请确认用户身份及申诉记录，操作不可逆或影响登录。</p>
            <div class="user-action-group user-detail-actions-grid user-detail-actions-grid-danger">
              <button
                v-if="canResetPassword(detailDialog.user)"
                class="admin-btn warning"
                type="button"
                @click="resetPassword(detailDialog.user)"
              >
                重置密码
              </button>
              <button
                v-if="canForceDelete(detailDialog.user)"
                class="admin-btn danger"
                type="button"
                @click="forceDelete(detailDialog.user)"
              >
                强制删除
              </button>
            </div>
          </div>
        </div>
        <div class="user-photo-header">
          <h4>上传图片（{{ detailDialog.photos.length }}）</h4>
          <div class="user-photo-filters">
            <button
              class="admin-btn"
              :class="{ primary: detailDialog.photoFilter === 'all' }"
              type="button"
              @click="detailDialog.photoFilter = 'all'"
            >
              全部
            </button>
            <button
              class="admin-btn"
              :class="{ primary: detailDialog.photoFilter === 'active' }"
              type="button"
              @click="detailDialog.photoFilter = 'active'"
            >
              仅正常
            </button>
          </div>
        </div>
        <div v-if="detailDialog.loading" class="admin-loading user-detail-loading">图片加载中...</div>
        <div v-else-if="!filteredDetailPhotos.length" class="user-photo-empty">当前筛选下暂无图片</div>
        <div v-else class="user-photo-grid">
          <a
            v-for="photo in filteredDetailPhotos"
            :key="photo.id"
            class="user-photo-card"
            :href="resolvePhotoUrl(photo.photoUrl)"
            target="_blank"
            rel="noopener noreferrer"
          >
            <img :src="resolvePhotoUrl(photo.photoUrl)" alt="用户上传图片" loading="lazy">
            <span class="user-photo-badge">{{ formatPhotoStatus(photo.status) }}</span>
            <span v-if="photo.primary" class="user-photo-primary">主图</span>
            <span class="user-photo-time">{{ formatDate(photo.createdAt) }}</span>
          </a>
        </div>
      </section>
    </AdminDetailDialogShell>

    <AdminDetailDialogShell
      v-model:visible="outreachDialog.visible"
      :title="outreachDialog.sendMode ? '发送运营提醒' : '预览触达用户'"
      :loading="outreachLoading"
      :max-width="720"
    >
      <div class="outreach-form">
        <label class="outreach-field">
          <span>分群 segment</span>
          <select v-model="outreachDialog.segment" class="admin-select" @change="loadOutreachPreview">
            <option v-for="opt in segmentOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </label>
        <label class="outreach-field">
          <span>运营模板</span>
          <select v-model="outreachDialog.templateCode" class="admin-select" @change="syncTemplatePreview">
            <option v-for="opt in templateOptions" :key="opt.code" :value="opt.code">{{ opt.title }}</option>
          </select>
        </label>
        <label v-if="outreachDialog.sendMode" class="outreach-field">
          <span>活动名称</span>
          <input v-model.trim="outreachDialog.name" class="admin-filter-input" type="text" placeholder="例如：未认证用户引导">
        </label>
        <p class="platform-text">目标人数：<strong>{{ outreachDialog.targetCount }}</strong></p>
        <article v-if="outreachDialog.templatePreview" class="outreach-template-preview">
          <h4>将发送的内容</h4>
          <p><strong>{{ outreachDialog.templatePreview.title }}</strong></p>
          <p>{{ outreachDialog.templatePreview.content }}</p>
          <p v-if="outreachDialog.templatePreview.actionUrl" class="outreach-action-url">
            跳转：{{ outreachDialog.templatePreview.actionUrl }}
          </p>
        </article>
        <div v-if="outreachDialog.sampleUsers.length" class="outreach-sample">
          <h4>样例用户</h4>
          <ul>
            <li v-for="user in outreachDialog.sampleUsers" :key="user.id">
              {{ user.nickname || `用户${user.id}` }} · 完成度 {{ user.profileCompletionRate }}% · {{ user.verificationTier }}
            </li>
          </ul>
        </div>
        <div v-if="outreachDialog.sendMode" class="outreach-confirm-actions">
          <button class="admin-btn" type="button" :disabled="outreachLoading" @click="outreachDialog.visible = false">取消</button>
          <button class="admin-btn primary" type="button" :disabled="outreachLoading || !outreachDialog.targetCount" @click="confirmSendCampaign">
            确认发送
          </button>
        </div>
      </div>
    </AdminDetailDialogShell>

    <AdminDetailDialogShell
      v-model:visible="campaignDetailDialog.visible"
      title="活动详情"
      :loading="campaignDetailDialog.loading"
      :max-width="900"
    >
      <div v-if="campaignDetailDialog.campaign" class="campaign-detail">
        <p class="platform-text">
          {{ campaignDetailDialog.campaign.name }} · {{ segmentLabel(campaignDetailDialog.campaign.segment) }}
        </p>
        <div class="campaign-funnel-grid">
          <span>发送 {{ campaignDetailDialog.campaign.sentCount }}</span>
          <span>打开 {{ campaignDetailDialog.campaign.openedCount }}（{{ formatFunnelRate(campaignDetailDialog.campaign.openRate) }}）</span>
          <span>点击 {{ campaignDetailDialog.campaign.clickedCount }}（{{ formatFunnelRate(campaignDetailDialog.campaign.clickRate) }}）</span>
          <span>完成 {{ campaignDetailDialog.campaign.completedCount }}（{{ formatFunnelRate(campaignDetailDialog.campaign.completeRate) }}）</span>
          <span>点击→完成 {{ formatFunnelRate(campaignDetailDialog.campaign.clickToCompleteRate) }}</span>
        </div>
        <div class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>用户ID</th>
                <th>状态</th>
                <th>发送时间</th>
                <th>打开时间</th>
                <th>点击次数</th>
                <th>最近点击</th>
                <th>完成时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in campaignDetailDialog.deliveries" :key="row.id">
                <td>{{ row.userId }}</td>
                <td>{{ row.status }}</td>
                <td>{{ formatDate(row.sentAt) }}</td>
                <td>{{ formatDate(row.openedAt) }}</td>
                <td>{{ row.clickedCount ?? 0 }}</td>
                <td>{{ formatDate(row.lastClickedAt || row.clickedAt) }}</td>
                <td>{{ formatDate(row.completedAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </AdminDetailDialogShell>

    <section v-if="filteredUsers.length" class="platform-card admin-pagination-card">
      <div class="admin-pagination">
        <label class="admin-page-size">
          每页
          <select v-model.number="pageSize" class="admin-select">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          条
        </label>
        <div class="admin-page-controls">
          <div class="admin-page-actions">
            <button class="admin-btn" type="button" :disabled="currentPage <= 1" @click="goFirstPage">首页</button>
            <button class="admin-btn" type="button" :disabled="currentPage <= 1" @click="goPrevPage">上一页</button>
            <span class="admin-page-indicator">{{ currentPage }} / {{ totalPages }}</span>
            <button class="admin-btn" type="button" :disabled="currentPage >= totalPages" @click="goNextPage">下一页</button>
            <button class="admin-btn" type="button" :disabled="currentPage >= totalPages" @click="goLastPage">末页</button>
          </div>
          <div class="admin-page-jump">
            <span class="admin-page-jump-label">跳转至</span>
            <input
              v-model.number="jumpPageDraft"
              class="admin-page-jump-input"
              type="number"
              :min="1"
              :max="totalPages"
              aria-label="页码"
              @keyup.enter="goJumpPage"
            >
            <span class="admin-page-jump-suffix">页</span>
            <button class="admin-btn primary" type="button" @click="goJumpPage">确定</button>
          </div>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import AdminDetailDialogShell from '@/components/admin/AdminDetailDialogShell.vue'
import {
  forceDeleteAdminUser,
  getAdminFellowshipGrowthDashboard,
  getAdminUserPhotos,
  getAdminUsers,
  resetAdminUserPassword,
  updateAdminUserFellowshipStatus,
  updateAdminUserRole,
  updateAdminUserStatus
} from '@/api/adminContent.js'
import {
  getGrowthCampaignDetail,
  listGrowthCampaigns,
  listGrowthCampaignTemplates,
  previewGrowthCampaignSegment,
  refreshGrowthCampaignConversion,
  sendGrowthCampaign
} from '@/api/adminGrowthCampaign.js'
import { useUserStore } from '@/stores/user.js'

const route = useRoute()
const loading = ref(false)
const users = ref([])
const growthDashboard = ref(null)
const growthCampaigns = ref([])
const outreachLoading = ref(false)
const campaignRefreshingId = ref(null)
const segmentOptions = [
  { value: 'LOW_COMPLETION', label: '完成度 0–39%' },
  { value: 'MEDIUM_COMPLETION', label: '完成度 40–79%' },
  { value: 'NEARLY_COMPLETE', label: '完成度 80–99%' },
  { value: 'UNVERIFIED', label: '未认证' },
  { value: 'MISSING_CITY', label: '缺地区' },
  { value: 'MISSING_AVATAR', label: '缺头像' },
  { value: 'MISSING_PHOTOS', label: '缺照片' },
  { value: 'MISSING_BIO', label: '缺简介' },
  { value: 'NOT_ENABLE_FELLOWSHIP', label: '未开通联谊' },
  { value: 'LOW_ACTIVITY', label: '近7天无活跃' }
]
const templateOptions = ref([])
const outreachDialog = reactive({
  visible: false,
  sendMode: false,
  segment: 'UNVERIFIED',
  templateCode: 'VERIFY_NOW',
  name: '',
  targetCount: 0,
  sampleUsers: [],
  templatePreview: null
})
const campaignDetailDialog = reactive({
  visible: false,
  loading: false,
  campaign: null,
  deliveries: []
})
const savingRoleUserId = ref(null)
const userStore = useUserStore()
const currentPage = ref(1)
const jumpPageDraft = ref(1)
const pageSize = ref(10)
const filters = reactive({
  keyword: '',
  role: '',
  status: '',
  fellowshipEnabled: '',
  gender: '',
  hasPhotos: '',
  completionBucket: '',
  verificationFilter: '',
  fellowshipOpsFilter: '',
  lowActive: '',
  missingItem: ''
})
const detailDialog = reactive({
  visible: false,
  loading: false,
  user: null,
  title: '用户详情',
  photos: [],
  photoFilter: 'all'
})
const filteredDetailPhotos = computed(() => {
  const list = Array.isArray(detailDialog.photos) ? detailDialog.photos : []
  if (detailDialog.photoFilter === 'active') {
    return list.filter((item) => String(item.status || '').toUpperCase() === 'ACTIVE')
  }
  return list
})

const roleOptions = [
  { value: 'user', label: '普通用户' },
  { value: 'admin', label: '管理员' }
]

const currentAdminRole = computed(() => normalizeRole(userStore.syncCurrentUser()?.role || 'user'))
const stats = computed(() => {
  let verifiedCount = 0
  let maleCount = 0
  let femaleCount = 0
  let unknownGenderCount = 0
  let fellowshipEnabledCount = 0
  let hasPhotosCount = 0
  let recentLoginCount = 0
  for (const item of users.value) {
    if (isUserCertified(item)) verifiedCount += 1
    if (item.fellowshipEnabled) fellowshipEnabledCount += 1
    if (item.hasUploadedPhotos) hasPhotosCount += 1
    if (item.gender === 'male') maleCount += 1
    else if (item.gender === 'female') femaleCount += 1
    else unknownGenderCount += 1
    if (isRecentLogin(item.lastLoginAt)) recentLoginCount += 1
  }
  return { verifiedCount, maleCount, femaleCount, unknownGenderCount, fellowshipEnabledCount, hasPhotosCount, recentLoginCount }
})

const filteredUsers = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return users.value.filter((item) => {
    if (filters.role && item.role !== filters.role) return false
    if (filters.status && (item.status || 'active') !== filters.status) return false
    if (filters.fellowshipEnabled === 'enabled' && !item.fellowshipEnabled) return false
    if (filters.fellowshipEnabled === 'disabled' && item.fellowshipEnabled) return false
    if (filters.gender && item.gender !== filters.gender) return false
    if (filters.hasPhotos === 'yes' && !item.hasUploadedPhotos) return false
    if (filters.hasPhotos === 'no' && item.hasUploadedPhotos) return false
    if (filters.completionBucket && item.completionBucket !== filters.completionBucket) return false
    if (filters.verificationFilter && item.verificationTier !== filters.verificationFilter) return false
    if (filters.fellowshipOpsFilter) {
      const ops = resolveFellowshipOpsKey(item)
      if (ops !== filters.fellowshipOpsFilter) return false
    }
    if (filters.lowActive === 'yes' && isRecentLogin(item.lastLoginAt)) return false
    if (filters.missingItem) {
      const keys = (item.profileMissingItems || []).map((row) => row.key)
      if (!keys.includes(filters.missingItem)) return false
    }
    if (!keyword) return true
    const username = String(item.username || '').toLowerCase()
    const phone = String(item.phone || '').toLowerCase()
    const userId = String(item.userId || '').toLowerCase()
    return username.includes(keyword) || phone.includes(keyword) || userId.includes(keyword)
  })
})
const totalPages = computed(() => Math.max(1, Math.ceil(filteredUsers.value.length / pageSize.value)))
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

const suggestedOutreachSegment = computed(() => inferSegmentFromFilters())
const suggestedOutreachLabel = computed(() => segmentLabel(suggestedOutreachSegment.value))

watch(currentPage, (p) => {
  jumpPageDraft.value = p
}, { immediate: true })

function normalizeUsers(rows) {
  return (Array.isArray(rows) ? rows : [])
    .map((item) => {
      const photoVerified = Boolean(item.photoVerified)
      const realnameVerified = Boolean(item.realnameVerified)
      return {
      userId: item.userId ?? item.id ?? null,
      username: item.username || '',
      phone: item.phone || '',
      gender: normalizeGender(item.gender ?? item.sex ?? item.userGender),
      age: normalizeAdminAge(item.age),
      role: normalizeRole(item.role || 'user'),
      photoVerified,
      realnameVerified,
      verificationStatus: resolveVerificationStatus(item, photoVerified, realnameVerified),
      verificationTier: item.verificationTier || resolveVerificationTier(photoVerified, realnameVerified),
      status: item.status || 'active',
      fellowshipEnabled: Boolean(item.fellowshipEnabled),
      fellowshipMatchVisible: Boolean(item.fellowshipMatchVisible),
      fellowshipPoolEligible: Boolean(item.fellowshipPoolEligible),
      profileCompletionRate: Number(item.profileCompletionRate ?? 0),
      profileMissingItems: Array.isArray(item.profileMissingItems) ? item.profileMissingItems : [],
      growthLevel: Number(item.growthLevel ?? 1),
      growthTitle: item.growthTitle || '',
      badgeCount: Number(item.badgeCount ?? 0),
      badges: Array.isArray(item.badges) ? item.badges : [],
      verifiedBadges: Array.isArray(item.verifiedBadges) ? item.verifiedBadges : [],
      exposureBoostPercent: Number(item.exposureBoostPercent ?? 0),
      unlockedBenefits: Array.isArray(item.unlockedBenefits) ? item.unlockedBenefits : [],
      nextBenefits: Array.isArray(item.nextBenefits) ? item.nextBenefits : [],
      completionBucket: item.completionBucket || resolveCompletionBucket(Number(item.profileCompletionRate ?? 0)),
      hasUploadedPhotos: Boolean(item.hasUploadedPhotos ?? Number(item.uploadedPhotoCount || 0) > 0),
      uploadedPhotoCount: Number(item.uploadedPhotoCount || 0),
      avatarUrl: String(item.avatarUrl || item.avatar || item.profilePhoto || '').trim(),
      location: item.location || item.city || item.region || '',
      lastLoginAt: item.lastLoginAt || item.lastActiveAt || item.loginAt || null,
      createdAt: item.createdAt || null,
      canForceDelete: !!item.canForceDelete,
      canResetPassword: !!item.canResetPassword
    }})
    .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
}

function resolveVerificationTier(photoVerified, realnameVerified) {
  if (photoVerified && realnameVerified) return 'both'
  if (realnameVerified) return 'realnameOnly'
  if (photoVerified) return 'photoOnly'
  return 'none'
}

function resolveCompletionBucket(rate) {
  if (rate >= 100) return '100'
  if (rate >= 80) return '80_99'
  if (rate >= 60) return '60_79'
  if (rate >= 40) return '40_59'
  return '0_39'
}

function resolveFellowshipOpsKey(item) {
  if (!item?.fellowshipEnabled) return 'notEnabled'
  if (item.fellowshipPoolEligible) return 'matchVisible'
  return 'enabled'
}

function verificationTierLabel(item) {
  const tier = item?.verificationTier || resolveVerificationTier(item?.photoVerified, item?.realnameVerified)
  if (tier === 'both') return '双认证'
  if (tier === 'realnameOnly') return '实名'
  if (tier === 'photoOnly') return '真人'
  return '未认证'
}

function verificationTierTagClass(tierOrItem) {
  const tier = typeof tierOrItem === 'string'
    ? tierOrItem
    : (tierOrItem?.verificationTier || resolveVerificationTier(tierOrItem?.photoVerified, tierOrItem?.realnameVerified))
  if (tier === 'both') return 'active'
  if (tier === 'realnameOnly' || tier === 'photoOnly') return 'pending'
  return 'disabled'
}

function fellowshipOpsLabel(item) {
  const key = resolveFellowshipOpsKey(item)
  if (key === 'matchVisible') return '已进推荐池'
  if (key === 'enabled') return '已开通'
  return '未开通'
}

function fellowshipOpsTagClass(item) {
  const key = resolveFellowshipOpsKey(item)
  if (key === 'matchVisible') return 'active'
  if (key === 'enabled') return 'pending'
  return 'disabled'
}

function formatExposureBoost(value) {
  const n = Number(value)
  if (!Number.isFinite(n) || n <= 0) return '-'
  return `+${n}%`
}

function benefitTitle(row) {
  return row?.title || row?.label || row?.name || row?.benefitText || '权益'
}

function formatBenefitsShort(list) {
  const rows = Array.isArray(list) ? list : []
  if (!rows.length) return '-'
  const labels = rows.map(benefitTitle)
  if (labels.length <= 2) return labels.join('、')
  return `${labels.slice(0, 2).join('、')} +${labels.length - 2}`
}

function formatBenefitsFull(list) {
  const rows = Array.isArray(list) ? list : []
  if (!rows.length) return '暂无'
  return rows.map(benefitTitle).join('、')
}

function formatMissingShort(list) {
  const rows = Array.isArray(list) ? list : []
  if (!rows.length) return '-'
  const labels = rows.map((row) => row.label || row.key)
  if (labels.length <= 3) return labels.join('、')
  return `${labels.slice(0, 3).join('、')} +${labels.length - 3}`
}

function formatMissingFull(list) {
  const rows = Array.isArray(list) ? list : []
  if (!rows.length) return '无'
  return rows.map((row) => row.label || row.key).join('、')
}

function formatBadgeList(badges, verifiedBadges) {
  const rows = [
    ...(Array.isArray(verifiedBadges) ? verifiedBadges : []),
    ...(Array.isArray(badges) ? badges : [])
  ]
  if (!rows.length) return '暂无'
  return rows.map((row) => row.name || row.label || row.code || '徽章').join('、')
}

function resolveVerificationStatus(item, photoVerified, realnameVerified) {
  if (photoVerified || realnameVerified) return 'approved'
  return item.verificationStatus || 'none'
}

function isUserCertified(item) {
  if (item?.photoVerified || item?.realnameVerified) return true
  return isVerified(item?.verificationStatus)
}

function normalizeGender(value) {
  const raw = String(value ?? '').trim().toLowerCase()
  if (['male', 'man', 'm', '1', 'boy', '男'].includes(raw)) return 'male'
  if (['female', 'woman', 'f', '2', 'girl', '女'].includes(raw)) return 'female'
  return 'unknown'
}

function normalizeAdminAge(value) {
  const n = Number(value)
  if (!Number.isFinite(n)) return null
  const i = Math.trunc(n)
  if (i <= 0 || i >= 150) return null
  return i
}

function formatAdminAge(value) {
  const n = normalizeAdminAge(value)
  return n == null ? '-' : `${n}岁`
}

function genderLabel(value) {
  return value === 'male' ? '男性' : value === 'female' ? '女性' : '未知'
}

function genderTagClass(value) {
  return value === 'male' ? 'active' : value === 'female' ? 'pending' : 'disabled'
}

function isVerified(value) {
  const raw = String(value || '').toLowerCase()
  return ['verified', 'approved', 'passed', 'success', 'done', 'active', 'completed', '1', 'true'].includes(raw)
}

function isRecentLogin(value) {
  if (!value) return false
  const time = new Date(value).getTime()
  if (Number.isNaN(time)) return false
  return Date.now() - time <= 7 * 24 * 60 * 60 * 1000
}

function verificationLabel(value) {
  const raw = String(value || '').toLowerCase()
  if (['none', '', 'unverified', 'not_verified', '0', 'false'].includes(raw)) return '未认证'
  if (['pending', 'reviewing', 'processing'].includes(raw)) return '审核中'
  if (['rejected', 'failed', 'denied'].includes(raw)) return '未通过'
  if (isVerified(raw)) return '已认证'
  return value || '未认证'
}

function verificationTagClass(value) {
  const raw = String(value || '').toLowerCase()
  if (['pending', 'reviewing', 'processing'].includes(raw)) return 'pending'
  if (['rejected', 'failed', 'denied'].includes(raw)) return 'rejected'
  if (isVerified(raw)) return 'active'
  return 'disabled'
}

function normalizeRole(value) {
  const role = String(value || '').trim().toLowerCase()
  return role === 'admin' ? 'admin' : 'user'
}

function canEditRole(item) {
  if (!item?.userId) return false
  return currentAdminRole.value === 'admin'
}

function canForceDelete(item) {
  return !!item?.canForceDelete
}

function canResetPassword(item) {
  return !!item?.canResetPassword
}

function resetFilters() {
  filters.keyword = ''
  filters.role = ''
  filters.status = ''
  filters.fellowshipEnabled = ''
  filters.gender = ''
  filters.hasPhotos = ''
  filters.completionBucket = ''
  filters.verificationFilter = ''
  filters.fellowshipOpsFilter = ''
  filters.lowActive = ''
  filters.missingItem = ''
  currentPage.value = 1
}

function goFirstPage() {
  currentPage.value = 1
}

function goPrevPage() {
  if (currentPage.value > 1) currentPage.value -= 1
}

function goNextPage() {
  if (currentPage.value < totalPages.value) currentPage.value += 1
}

function goLastPage() {
  currentPage.value = totalPages.value
}

function goJumpPage() {
  const nRaw = Number(jumpPageDraft.value)
  if (!Number.isFinite(nRaw)) {
    showToast({ type: 'fail', message: '请输入有效页码' })
    jumpPageDraft.value = currentPage.value
    return
  }
  let n = Math.trunc(nRaw)
  const max = totalPages.value
  if (max < 1) return
  if (n < 1) n = 1
  if (n > max) n = max
  currentPage.value = n
  jumpPageDraft.value = n
}

async function loadGrowthDashboard() {
  try {
    growthDashboard.value = await getAdminFellowshipGrowthDashboard()
  } catch {
    growthDashboard.value = null
  }
}

async function loadUsers() {
  loading.value = true
  try {
    const data = await getAdminUsers()
    users.value = normalizeUsers(data)
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '用户列表加载失败' })
  } finally {
    loading.value = false
  }
}

async function loadGrowthCampaigns() {
  try {
    const data = await listGrowthCampaigns()
    growthCampaigns.value = Array.isArray(data) ? data : []
  } catch {
    growthCampaigns.value = []
  }
}

async function loadTemplateOptions() {
  try {
    const data = await listGrowthCampaignTemplates()
    templateOptions.value = Array.isArray(data) ? data : []
  } catch {
    templateOptions.value = []
  }
}

function inferSegmentFromFilters() {
  if (filters.verificationFilter === 'none' || filters.missingItem === 'verification') return 'UNVERIFIED'
  if (filters.missingItem === 'city') return 'MISSING_CITY'
  if (filters.missingItem === 'avatar') return 'MISSING_AVATAR'
  if (filters.missingItem === 'photos') return 'MISSING_PHOTOS'
  if (filters.missingItem === 'bio') return 'MISSING_BIO'
  if (filters.fellowshipOpsFilter === 'notEnabled' || filters.missingItem === 'fellowship') return 'NOT_ENABLE_FELLOWSHIP'
  if (filters.lowActive === 'yes') return 'LOW_ACTIVITY'
  if (filters.completionBucket === '0_39') return 'LOW_COMPLETION'
  if (filters.completionBucket === '40_59' || filters.completionBucket === '60_79') return 'MEDIUM_COMPLETION'
  if (filters.completionBucket === '80_99') return 'NEARLY_COMPLETE'
  return 'UNVERIFIED'
}

function segmentLabel(segment) {
  const hit = segmentOptions.find((item) => item.value === segment)
  return hit?.label || segment || '-'
}

function templateLabel(code) {
  const hit = templateOptions.value.find((item) => item.code === code)
  return hit?.title || code || '-'
}

function campaignStatusClass(status) {
  const raw = String(status || '').toUpperCase()
  if (raw === 'SENT' || raw === 'COMPLETED') return 'active'
  if (raw === 'FAILED') return 'disabled'
  return 'pending'
}

function formatFunnelRate(value) {
  if (value == null || Number.isNaN(Number(value))) return '-'
  return `${(Number(value) * 100).toFixed(1)}%`
}

function syncTemplatePreview() {
  const hit = templateOptions.value.find((item) => item.code === outreachDialog.templateCode)
  outreachDialog.templatePreview = hit
    ? { title: hit.title, content: hit.content, actionUrl: hit.actionUrl }
    : null
}

async function openOutreachDialog(sendMode) {
  outreachDialog.sendMode = sendMode
  outreachDialog.segment = suggestedOutreachSegment.value
  outreachDialog.name = `${segmentLabel(outreachDialog.segment)}运营触达`
  outreachDialog.visible = true
  await loadOutreachPreview()
}

async function loadOutreachPreview() {
  outreachLoading.value = true
  try {
    const data = await previewGrowthCampaignSegment(outreachDialog.segment)
    outreachDialog.targetCount = Number(data?.targetCount || 0)
    outreachDialog.sampleUsers = Array.isArray(data?.sampleUsers) ? data.sampleUsers : []
    if (data?.suggestedTemplateCode) {
      outreachDialog.templateCode = data.suggestedTemplateCode
    }
    if (data?.suggestedTemplate) {
      outreachDialog.templatePreview = {
        title: data.suggestedTemplate.title,
        content: data.suggestedTemplate.content,
        actionUrl: data.suggestedTemplate.actionUrl
      }
    } else {
      syncTemplatePreview()
    }
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '分群预览失败' })
  } finally {
    outreachLoading.value = false
  }
}

async function confirmSendCampaign() {
  if (!outreachDialog.targetCount) {
    showToast({ type: 'fail', message: '当前分群无目标用户' })
    return
  }
  try {
    await showConfirmDialog({
      title: '确认发送',
      message: `将向 ${outreachDialog.targetCount} 位用户发送「${outreachDialog.templatePreview?.title || outreachDialog.templateCode}」站内提醒，确认继续吗？`
    })
  } catch {
    return
  }
  outreachLoading.value = true
  try {
    const result = await sendGrowthCampaign({
      segment: outreachDialog.segment,
      templateCode: outreachDialog.templateCode,
      channel: 'IN_APP',
      name: outreachDialog.name || `${segmentLabel(outreachDialog.segment)}运营触达`
    })
    showToast({
      type: 'success',
      message: `已发送 ${result?.sentCount ?? 0} 人，失败 ${result?.failedCount ?? 0} 人`
    })
    outreachDialog.visible = false
    await loadGrowthCampaigns()
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '发送失败' })
  } finally {
    outreachLoading.value = false
  }
}

async function openCampaignDetail(item) {
  campaignDetailDialog.visible = true
  campaignDetailDialog.loading = true
  campaignDetailDialog.campaign = item
  campaignDetailDialog.deliveries = []
  try {
    const data = await getGrowthCampaignDetail(item.id, { page: 1, size: 50 })
    campaignDetailDialog.campaign = data?.campaign || item
    campaignDetailDialog.deliveries = Array.isArray(data?.deliveries) ? data.deliveries : []
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '活动详情加载失败' })
  } finally {
    campaignDetailDialog.loading = false
  }
}

async function refreshCampaignConversion(item) {
  campaignRefreshingId.value = item.id
  try {
    const result = await refreshGrowthCampaignConversion(item.id)
    showToast({
      type: 'success',
      message: `转化已刷新：完成 ${result?.completedCount ?? item.completedCount} 人`
    })
    await loadGrowthCampaigns()
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '刷新转化失败' })
  } finally {
    campaignRefreshingId.value = null
  }
}

function formatDate(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('zh-CN', { hour12: false })
}

async function setStatus(item, status) {
  try {
    const result = await updateAdminUserStatus(item.userId, status)
    item.status = result.status
    showToast({ type: 'success', message: result.message || '状态已更新' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '操作失败' })
  }
}

async function setFellowship(item, fellowshipEnabled) {
  try {
    const result = await updateAdminUserFellowshipStatus(item.userId, fellowshipEnabled)
    item.fellowshipEnabled = Boolean(result.fellowshipEnabled)
    showToast({ type: 'success', message: result.message || '联谊状态已更新' })
    await loadUsers()
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '操作失败' })
  }
}

async function saveRole(item) {
  if (!item?.userId) {
    showToast({ type: 'fail', message: '缺少用户ID，无法保存' })
    return
  }
  if (!canEditRole(item)) {
    showToast({ type: 'fail', message: '当前账号无法修改角色' })
    return
  }
  if (!['user', 'admin'].includes(item.role)) {
    showToast({ type: 'fail', message: '仅可设置 USER/ADMIN' })
    return
  }
  try {
    savingRoleUserId.value = item.userId
    await updateAdminUserRole(item.userId, item.role)
    showToast({ type: 'success', message: '角色已更新' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '角色更新失败' })
  } finally {
    savingRoleUserId.value = null
  }
}

async function forceDelete(item) {
  if (!canForceDelete(item)) {
    showToast({ type: 'fail', message: '当前账号无强制删除权' })
    return
  }
  try {
    await showConfirmDialog({
      title: '强制删除账号',
      message: `该操作将直接删除用户 ${item.username || `用户${item.userId}`} 的数据库账号，且不可恢复，确认继续吗？`
    })
  } catch {
    return
  }
  try {
    const result = await forceDeleteAdminUser(item.userId)
    users.value = users.value.filter((u) => u.userId !== item.userId)
    showToast({ type: 'success', message: result.message || '用户已删除' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '强制删除失败' })
  }
}

async function resetPassword(item) {
  if (!canResetPassword(item)) {
    showToast({ type: 'fail', message: '当前账号无重置密码权限' })
    return
  }
  try {
    await showConfirmDialog({
      title: '重置密码',
      message: `确认将用户 ${item.username || `用户${item.userId}`} 的密码重置为 123456 吗？`
    })
  } catch {
    return
  }
  try {
    const result = await resetAdminUserPassword(item.userId)
    showToast({ type: 'success', message: result.message || '密码已重置为 123456' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '重置密码失败' })
  }
}

function onDetailDialogVisible(visible) {
  if (!visible) {
    detailDialog.visible = false
    return
  }
  detailDialog.visible = true
}

async function openDetailDialog(item) {
  detailDialog.visible = true
  detailDialog.user = item
  detailDialog.title = `用户详情：${item.username || `用户${item.userId}`}`
  detailDialog.photos = []
  detailDialog.photoFilter = 'all'
  detailDialog.loading = true
  try {
    const data = await getAdminUserPhotos(item.userId)
    const photos = Array.isArray(data?.photos) ? data.photos : []
    detailDialog.photos = photos.sort((a, b) => {
      const aPrimary = Boolean(a?.primary)
      const bPrimary = Boolean(b?.primary)
      if (aPrimary !== bPrimary) return aPrimary ? -1 : 1
      return Number(a?.sortOrder || 0) - Number(b?.sortOrder || 0)
    })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '用户图片加载失败' })
  } finally {
    detailDialog.loading = false
  }
}

/**
 * 将头像/相册地址转为当前站点可加载的 URL。
 * 库中常存完整 http(s) 指向 :8090 或 localhost；在 https 正式站会触发混合内容拦截，需改为同域 /admin/uploads/...（由 Nginx 反代到 Spring）。
 */
function resolvePhotoUrl(url) {
  const value = String(url || '').trim()
  if (!value) return ''
  const normalized = value.replace(/\\/g, '/')

  if (/^https?:\/\//i.test(normalized)) {
    try {
      const u = new URL(normalized)
      const p = u.pathname
      const q = u.search || ''
      const isLocalHost = ['localhost', '127.0.0.1'].includes((u.hostname || '').toLowerCase())
      const isHttpOnHttpsPage =
        typeof window !== 'undefined' &&
        window.location.protocol === 'https:' &&
        u.protocol === 'http:'
      // 本地地址或 HTTPS 页面引用 HTTP 资源时，改为同域 /admin/uploads，避免混合内容与本地路径不一致
      if (p.startsWith('/admin/uploads/')) {
        if (isLocalHost || isHttpOnHttpsPage) return `${p}${q}`
        return normalized
      }
      if (p.startsWith('/admin/api/uploads/')) {
        const fixed = p.replace('/admin/api/uploads/', '/admin/uploads/')
        if (isLocalHost || isHttpOnHttpsPage) return `${fixed}${q}`
        return `${u.origin}${fixed}${q}`
      }
      if (p.startsWith('/uploads/')) {
        if (isLocalHost || isHttpOnHttpsPage) return `/admin${p}${q}`
        return normalized
      }
    } catch {
      /* ignore */
    }
    return normalized
  }

  if (normalized.startsWith('/admin/uploads/')) return normalized
  if (normalized.startsWith('/admin/api/uploads/')) return normalized.replace('/admin/api/uploads/', '/admin/uploads/')
  if (normalized.startsWith('/uploads/')) return `/admin${normalized}`
  // 库中常见存法：uploads/avatar/uuid.jpg（无前置斜杠），不能落到默认 photos 路径
  if (normalized.startsWith('uploads/')) return `/admin/${normalized}`
  if (normalized.startsWith('admin/uploads/')) return `/${normalized}`
  if (normalized.startsWith('admin/api/uploads/')) return `/${normalized.replace('admin/api/uploads/', 'admin/uploads/')}`
  if (normalized.startsWith('/')) return normalized
  if (normalized.includes('/')) return `/admin/${normalized.replace(/^\//, '')}`
  return `/admin/uploads/photos/${normalized}`
}

function rowAvatarUrl(item) {
  return resolvePhotoUrl(item?.avatarUrl)
}

const detailAvatarDisplayUrl = computed(() => {
  const u = detailDialog.user
  if (!u) return ''
  const fromProfile = String(u.avatarUrl || '').trim()
  if (fromProfile) return resolvePhotoUrl(fromProfile)
  const list = Array.isArray(detailDialog.photos) ? detailDialog.photos : []
  const primary = list.find((p) => p.primary && String(p.photoUrl || '').trim())
  if (primary?.photoUrl) return resolvePhotoUrl(primary.photoUrl)
  const first = list.find((p) => String(p.photoUrl || '').trim())
  if (first?.photoUrl) return resolvePhotoUrl(first.photoUrl)
  return ''
})

function formatPhotoStatus(status) {
  const raw = String(status || '').toUpperCase()
  if (raw === 'ACTIVE') return '正常'
  if (raw === 'DISABLED') return '禁用'
  return raw || '未知'
}

onMounted(async () => {
  await Promise.all([loadUsers(), loadGrowthDashboard(), loadGrowthCampaigns(), loadTemplateOptions()])
  const userId = route.query.userId
  if (!userId) return
  const target = users.value.find((item) => String(item.userId) === String(userId))
  if (target) {
    openDetailDialog(target)
    return
  }
  filters.keyword = String(userId)
  currentPage.value = 1
})

watch([
  () => filters.keyword,
  () => filters.role,
  () => filters.status,
  () => filters.fellowshipEnabled,
  () => filters.gender,
  () => filters.hasPhotos,
  () => filters.completionBucket,
  () => filters.verificationFilter,
  () => filters.fellowshipOpsFilter,
  () => filters.lowActive,
  () => filters.missingItem
], () => {
  currentPage.value = 1
})

watch(pageSize, () => {
  currentPage.value = 1
})

watch(totalPages, (pages) => {
  if (currentPage.value > pages) {
    currentPage.value = pages
  }
})
</script>

<style scoped>
.admin-page-users {
  max-width: none;
  width: 100%;
}

.growth-dashboard-title,
.outreach-title {
  margin: 0 0 12px;
  font-size: 16px;
  color: var(--lc-text);
}

.outreach-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.outreach-row-actions {
  display: inline-flex;
  gap: 6px;
  flex-wrap: wrap;
}

.outreach-form {
  display: grid;
  gap: 12px;
}

.outreach-field {
  display: grid;
  gap: 6px;
  font-size: 13px;
  color: var(--lc-muted-light);
}

.outreach-template-preview,
.outreach-sample {
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 12px;
  background: var(--lc-surface);
}

.outreach-template-preview h4,
.outreach-sample h4 {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--lc-text);
}

.outreach-action-url {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--lc-muted-light);
  word-break: break-all;
}

.outreach-sample ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 4px;
  font-size: 13px;
  color: var(--lc-slate);
}

.outreach-confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.campaign-funnel-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  font-size: 13px;
  color: var(--lc-slate);
}

.growth-dashboard-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.growth-dashboard-card {
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 12px;
  background: var(--lc-surface);
}

.growth-dashboard-card h3 {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--lc-muted-light);
}

.growth-dashboard-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 6px;
}

.growth-dashboard-list li {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
  color: var(--lc-slate);
}

.users-completion-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 88px;
}

.users-completion-bar {
  flex: 1;
  height: 6px;
  border-radius: 999px;
  background: var(--lc-border);
  overflow: hidden;
}

.users-completion-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--lc-blue), var(--lc-pink));
}

.users-completion-text {
  font-size: 11px;
  color: var(--lc-muted-light);
  white-space: nowrap;
}

.users-level-title {
  display: block;
  font-size: 11px;
  color: var(--lc-muted-light);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 72px;
}

.user-growth-insight {
  margin-top: 16px;
  padding: 14px;
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  background: #fff;
}

.user-growth-insight h4 {
  margin: 0 0 10px;
  font-size: 15px;
  color: var(--lc-text);
}

.user-growth-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px 12px;
  margin-bottom: 8px;
}

.user-growth-insight p {
  margin: 6px 0;
  font-size: 13px;
  color: var(--lc-slate);
}

.users-table col.cg-completion { width: 108px; }
.users-table col.cg-level { width: 88px; }
.users-table col.cg-benefits { width: 120px; }
.users-table col.cg-missing { width: 120px; }

.users-table .col-benefits,
.users-table .col-missing {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
}

.admin-select {
  min-width: 120px;
  height: 38px;
}

/* 撑满容器宽度，固定列保手机号，其余列按比例分配剩余空间 */
.admin-table.users-table {
  width: 100%;
  min-width: 1800px;
  table-layout: fixed;
}

.users-table col.cg-avatar { width: 56px; }
.users-table col.cg-phone { width: 132px; }
.users-table col.cg-age { width: 52px; }
.users-table col.cg-role { width: 96px; }
.users-table col.cg-tag { width: 72px; }
.users-table col.cg-actions { width: 76px; }
.users-table col.cg-user { width: 10%; }
.users-table col.cg-location { width: 14%; }
.users-table col.cg-login { width: 14%; }
.users-table col.cg-date { width: 14%; }

.users-table :is(th, td) {
  padding: 10px 8px;
}

.users-table th {
  white-space: nowrap;
}

.users-table td {
  vertical-align: middle;
}

.users-table thead th.col-actions-head,
.users-table tbody td.col-actions {
  width: 76px;
  min-width: 76px;
  text-align: center;
}

.users-table-detail-btn {
  padding: 0 8px;
  min-height: 32px;
  font-size: 12px;
  white-space: nowrap;
}

.users-table .col-user-head,
.users-table .col-user {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.users-table .col-phone-head,
.users-table .col-phone {
  width: 132px;
  min-width: 132px;
  padding-left: 10px;
  padding-right: 10px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.02em;
  white-space: nowrap;
}
.users-table .col-role-head,
.users-table .col-role {
  width: 92px;
  min-width: 92px;
  max-width: 92px;
}

.admin-filter-card {
  margin-top: 14px;
}

.admin-pagination-card {
  margin-top: 14px;
}

.users-stats-card {
  margin-top: 14px;
}

.admin-filter-bar {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  padding: 2px 0 4px;
  -webkit-overflow-scrolling: touch;
}

.admin-filter-bar .admin-filter-input {
  flex: 1 1 200px;
  min-width: 160px;
  max-width: 280px;
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.08);
}

.admin-filter-bar :deep(.admin-select) {
  flex: 0 0 auto;
  min-width: 104px;
  width: 124px;
  max-width: 140px;
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.08);
}

.admin-filter-bar .admin-btn {
  flex-shrink: 0;
  height: 38px;
  white-space: nowrap;
  border-radius: 8px;
}

.admin-filter-input {
  height: 38px;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  padding: 0 12px;
  color: var(--lc-text);
}

.users-table :deep(.admin-select) {
  min-width: 0;
  width: 100%;
  max-width: 100%;
  font-size: 12px;
  padding: 0 6px;
}

.users-table .col-avatar-head,
.users-table .col-avatar {
  width: 56px;
  min-width: 56px;
  max-width: 56px;
  text-align: center;
  box-sizing: border-box;
}

.users-table-avatar-img {
  display: block;
  width: 40px;
  height: 40px;
  margin: 0 auto;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
}

.users-table-avatar-empty {
  display: block;
  width: 40px;
  height: 40px;
  margin: 0 auto;
  border-radius: 50%;
  border: 1px dashed var(--lc-border);
  background: var(--lc-soft);
}

.users-mobile-actions-top {
  margin-bottom: 10px;
}

.users-mobile-head-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.users-mobile-head-left strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.users-mobile-avatar {
  flex: 0 0 auto;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
}

.users-mobile-avatar-empty {
  display: inline-block;
  box-sizing: border-box;
  border-style: dashed;
  background: var(--lc-soft);
}

.users-table .col-user {
  font-weight: 600;
}

.users-table .col-age {
  width: 52px;
  min-width: 52px;
  max-width: 52px;
  text-align: center;
  color: var(--lc-muted);
}

.users-overview {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.overview-chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid var(--lc-border);
  background: var(--lc-soft);
  font-size: 12px;
  color: var(--lc-muted);
  font-weight: 600;
}

.users-table .col-date {
  min-width: 10.5rem;
  color: var(--lc-muted);
  font-size: 12px;
  white-space: nowrap;
}

.users-table .col-location {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--lc-muted);
}

.users-table .col-login {
  min-width: 10.5rem;
  color: var(--lc-muted);
  font-size: 12px;
  white-space: nowrap;
}

.user-action-group {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px;
}

.user-action-group:nth-child(2) {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.user-action-group:first-child {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.user-action-group-danger {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.user-actions :deep(.admin-btn),
.user-action-group :deep(.admin-btn) {
  width: 100%;
  min-height: 32px;
  padding: 0 10px;
  font-size: 12px;
  border-radius: 8px;
}

.user-detail-wrap {
  display: grid;
  gap: 12px;
}

.user-detail-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 14px 16px;
  padding: 12px;
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  background: var(--lc-soft);
}

.user-detail-avatar-col {
  flex: 0 0 auto;
}

.user-detail-avatar-img {
  display: block;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
}

.user-detail-avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  border: 1px dashed var(--lc-border);
  background: var(--lc-surface);
  font-size: 11px;
  color: var(--lc-subtle);
  text-align: center;
  line-height: 1.3;
  padding: 6px;
  box-sizing: border-box;
}

.user-detail-meta-fields {
  flex: 1 1 280px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px 12px;
}

.user-detail-meta-fields p {
  margin: 0;
  font-size: 13px;
  color: var(--lc-muted);
}

.user-detail-actions {
  display: grid;
  gap: 10px;
}

.user-detail-action-card {
  display: grid;
  gap: 8px;
  padding: 12px;
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  background: var(--lc-surface);
}

.user-detail-action-card-danger {
  border-color: rgba(248, 113, 113, 0.35);
  background: linear-gradient(180deg, rgba(254, 242, 242, 0.82), rgba(255, 255, 255, 0.95));
}

.user-detail-action-title {
  margin: 0;
  font-size: 13px;
  color: var(--lc-slate);
  font-weight: 700;
}

.user-detail-action-hint {
  margin: 0;
  font-size: 12px;
  color: var(--lc-subtle);
  line-height: 1.5;
}

.user-detail-role-row {
  display: grid;
  grid-template-columns: minmax(160px, 220px) minmax(100px, 120px);
  gap: 8px;
  align-items: center;
}

.user-detail-actions-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.user-detail-actions-grid-danger {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.user-photo-header h4 {
  margin: 0;
  color: var(--lc-text);
  font-size: 15px;
}

.user-photo-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}

.user-photo-filters {
  display: inline-flex;
  gap: 6px;
}

.user-photo-filters :deep(.admin-btn) {
  min-height: 30px;
  padding: 0 10px;
  font-size: 12px;
}

.user-detail-loading {
  margin-top: 0;
  padding: 24px 0;
}

.user-photo-empty {
  padding: 18px;
  border: 1px dashed var(--lc-border);
  border-radius: 10px;
  color: var(--lc-subtle);
  background: #fff;
  text-align: center;
}

.user-photo-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.user-photo-card {
  position: relative;
  display: block;
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.user-photo-card img {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  display: block;
}

.user-photo-badge {
  position: absolute;
  right: 8px;
  bottom: 8px;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: rgba(15, 23, 42, 0.7);
}

.user-photo-primary {
  position: absolute;
  left: 8px;
  top: 8px;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: rgba(37, 99, 235, 0.82);
}

.user-photo-time {
  position: absolute;
  left: 8px;
  bottom: 8px;
  font-size: 11px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(15, 23, 42, 0.7);
}

.admin-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.admin-page-size {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--lc-muted-light);
  font-size: 13px;
}

.admin-page-controls {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.admin-page-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.admin-page-jump {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
  font-size: 13px;
  color: var(--lc-muted-light);
}

.admin-page-jump-label,
.admin-page-jump-suffix {
  white-space: nowrap;
}

.admin-page-jump-input {
  width: 4.5rem;
  height: 38px;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  padding: 0 10px;
  color: var(--lc-text);
  text-align: center;
  font-size: 14px;
}

.admin-page-jump-input::-webkit-outer-spin-button,
.admin-page-jump-input::-webkit-inner-spin-button {
  margin: 0;
  appearance: none;
}

.admin-page-jump-input[type='number'] {
  appearance: textfield;
}

.admin-page-indicator {
  min-width: 56px;
  text-align: center;
  color: var(--lc-slate);
  font-weight: 600;
  font-size: 13px;
}

@media (max-width: 1023px) {
  .growth-dashboard-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .user-growth-grid {
    grid-template-columns: 1fr 1fr;
  }

  .user-detail-meta-fields {
    grid-template-columns: 1fr 1fr;
  }

  .user-photo-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .user-detail-role-row,
  .user-detail-actions-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>



