<template>
  <article class="intro-card">
    <header class="intro-head">
      <p class="intro-badge">{{ card.badgeLabel || '—' }}</p>
      <p v-if="card.completed" class="intro-status">资料已完善</p>
      <p v-else class="intro-status pending">资料待完善</p>
    </header>

    <dl class="intro-meta">
      <div v-if="card.age"><dt>年龄</dt><dd>{{ card.age }} 岁</dd></div>
      <div v-if="card.heightCm"><dt>身高</dt><dd>{{ card.heightCm }} cm</dd></div>
      <div v-if="card.city"><dt>城市</dt><dd>{{ card.city }}</dd></div>
      <div v-if="card.occupation"><dt>职业</dt><dd>{{ card.occupation }}</dd></div>
      <div v-if="card.education"><dt>学历</dt><dd>{{ card.education }}</dd></div>
    </dl>

    <div v-if="tags.length" class="intro-tags">
      <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
    </div>

    <section v-if="card.selfIntro" class="intro-block">
      <h3>自我介绍</h3>
      <p>{{ card.selfIntro }}</p>
    </section>

    <section v-if="card.partnerRequirements" class="intro-block">
      <h3>择偶要求</h3>
      <p>{{ card.partnerRequirements }}</p>
    </section>

    <section v-if="card.idealPartnerDesc" class="intro-block">
      <h3>理想对象</h3>
      <p>{{ card.idealPartnerDesc }}</p>
    </section>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  card: { type: Object, default: () => ({}) }
})

const tags = computed(() => {
  const raw = props.card?.interestTags
  return Array.isArray(raw) ? raw : []
})
</script>

<style scoped>
.intro-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.intro-head {
  text-align: center;
  margin-bottom: 16px;
}

.intro-badge {
  margin: 0;
  font-size: 36px;
  font-weight: 800;
  color: #ff5f84;
  letter-spacing: 0.06em;
}

.intro-status {
  margin: 6px 0 0;
  font-size: 12px;
  color: #15803d;
}

.intro-status.pending {
  color: #b45309;
}

.intro-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
  margin: 0 0 12px;
}

.intro-meta div {
  display: flex;
  gap: 6px;
  font-size: 14px;
}

.intro-meta dt {
  color: #94a3b8;
}

.intro-meta dd {
  margin: 0;
  color: #334155;
  font-weight: 600;
}

.intro-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.tag {
  background: #fff1f2;
  color: #e11d48;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
}

.intro-block h3 {
  margin: 0 0 6px;
  font-size: 14px;
  color: #64748b;
}

.intro-block p {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #334155;
  white-space: pre-line;
}
</style>
