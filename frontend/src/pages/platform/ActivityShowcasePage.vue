<template>
  <section class="showcase-page operation-shell">
    <section class="diagram-section platform-card">
      <header class="section-head">
        <h1 class="platform-section-title">参与者活动流程</h1>
        <router-link :to="eventsPath()" class="back-link">返回活动中心</router-link>
      </header>
      <MermaidFlowChart
        chart-id="participant-flow"
        aria-label="参与者活动流程图"
        :definition="participantFlowDefinition"
      />
    </section>
  </section>
</template>

<script setup>
import { usePlatformPath } from '@/composables/usePlatformPath.js'
import MermaidFlowChart from '@/components/platform/activity/MermaidFlowChart.vue'

const { eventsPath } = usePlatformPath()

const participantFlowDefinition = `flowchart TB
    subgraph ONSITE["现场入场"]
        direction TB
        P1["现场扫码"] --> P2["输入签到码"]
        P2 --> P3{"联谊专场?"}
        P3 -->|是| P4["获得编号 / 完善资料"]
        P4 --> P5["浏览嘉宾花名册"]
        P3 -->|否| P6["参与活动"]
        P5 --> P7["现场交流"]
        P6 --> P7
    end

    subgraph CONNECT["连接与互选"]
        direction TB
        P7 --> P8["记录认识的人"]
        P8 --> P9["喜欢 TA"]
        P9 --> P10{"双向喜欢?"}
        P10 -->|是| P11["互选成功"]
        P10 -->|否| P12["保留在我的喜欢"]
        P11 --> P13["查看互选结果"]
        P12 --> P13
    end

    subgraph AFTER["活动后"]
        direction TB
        P13 --> P14["活动互评"]
        P14 --> P15["后续联系"]
        P15 --> P16["再次参与活动"]
    end

    style P4 fill:#ffe4ec,stroke:#ff5f84
    style P11 fill:#ffe4ec,stroke:#ff5f84`
</script>

<style scoped>
.showcase-page {
  padding-bottom: var(--lc-space-8);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-4);
  margin-bottom: var(--lc-space-4);
}

.section-head h1 {
  margin: 0;
}

.back-link {
  flex-shrink: 0;
  font-size: var(--lc-text-sm);
  font-weight: 700;
  color: var(--lc-blue);
  text-decoration: none;
}

.back-link:hover {
  text-decoration: underline;
}
</style>
