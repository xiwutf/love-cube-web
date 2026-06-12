<template>
  <div ref="containerRef" class="mermaid-flow-chart" :aria-label="ariaLabel" role="img" />
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import mermaid from 'mermaid'

const props = defineProps({
  definition: { type: String, required: true },
  chartId: { type: String, default: 'flow-chart' },
  ariaLabel: { type: String, default: '业务流程图' },
  fontSize: { type: Number, default: 13 },
  nodeSpacing: { type: Number, default: 40 },
  rankSpacing: { type: Number, default: 48 }
})

const containerRef = ref(null)
let renderSeq = 0

async function render() {
  const el = containerRef.value
  if (!el || !props.definition) return

  const seq = ++renderSeq
  mermaid.initialize({
    startOnLoad: false,
    securityLevel: 'loose',
    theme: 'base',
    flowchart: {
      useMaxWidth: true,
      htmlLabels: true,
      curve: 'basis',
      padding: 16,
      nodeSpacing: props.nodeSpacing,
      rankSpacing: props.rankSpacing,
      diagramPadding: 12
    },
    themeVariables: {
      fontFamily: '"PingFang SC", "Microsoft YaHei", sans-serif',
      fontSize: `${props.fontSize}px`,
      primaryColor: '#fff5f8',
      primaryTextColor: '#374151',
      primaryBorderColor: '#ff5f84',
      lineColor: '#d1d5db',
      textColor: '#374151',
      mainBkg: '#ffffff',
      nodeBorder: '#e5e7eb',
      clusterBkg: '#fafafa',
      clusterBorder: '#e5e7eb',
      titleColor: '#6b7280'
    }
  })

  try {
    const id = `${props.chartId}-${seq}`
    const { svg } = await mermaid.render(id, props.definition)
    if (seq !== renderSeq) return
    el.innerHTML = svg
  } catch (err) {
    if (seq !== renderSeq) return
    el.innerHTML = '<p class="mermaid-error">流程图加载失败</p>'
    console.error('[MermaidFlowChart]', err)
  }
}

onMounted(render)
watch(
  () => [props.definition, props.fontSize, props.nodeSpacing, props.rankSpacing],
  render
)
</script>

<style scoped>
.mermaid-flow-chart {
  width: 100%;
  overflow: auto;
}

.mermaid-flow-chart :deep(svg) {
  display: block;
  margin: 0 auto;
  max-width: 100%;
  height: auto;
}

.mermaid-flow-chart :deep(.nodeLabel),
.mermaid-flow-chart :deep(.label),
.mermaid-flow-chart :deep(text) {
  font-size: 12px !important;
}

.mermaid-flow-chart :deep(.cluster-label text) {
  font-size: 13px !important;
  font-weight: 700;
}

.mermaid-flow-chart :deep(.mermaid-error) {
  margin: 0;
  padding: var(--lc-space-6);
  text-align: center;
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
}
</style>
