#!/usr/bin/env bash
# 平台语义防回流检查
# 禁止在 .vue/.js/.java 文件中引入宗教语义词汇
#
# 用法：
#   bash scripts/semantic-guard.sh --git-hook   # git pre-commit 模式，exit 1 阻止提交
#   bash scripts/semantic-guard.sh              # Claude Code hook 模式，输出 JSON 阻止

PATTERN="教会|祷告|祈祷|福音|灵修|团契|牧养|主内|见证|查经|敬拜|弟兄姊妹"

# 获取暂存文件（staged）
STAGED=$(git diff --cached --name-only 2>/dev/null | grep -E '\.(vue|js|java)$' || true)
# 获取已修改但未暂存文件（git commit -a 场景）
MODIFIED=$(git diff --name-only 2>/dev/null | grep -E '\.(vue|js|java)$' || true)

# 合并、去重、排除空行
FILES=""
if [ -n "$STAGED" ] && [ -n "$MODIFIED" ]; then
  FILES=$(printf '%s\n%s\n' "$STAGED" "$MODIFIED" | sort -u | grep -v '^$')
elif [ -n "$STAGED" ]; then
  FILES="$STAGED"
elif [ -n "$MODIFIED" ]; then
  FILES="$MODIFIED"
fi

# 无目标文件 — 直接通过
if [ -z "$FILES" ]; then
  exit 0
fi

# 扫描每个文件
HIT_FILES=""
while IFS= read -r f; do
  [ -z "$f" ] && continue
  [ -f "$f" ] || continue
  if LC_ALL=C.UTF-8 grep -qP "$PATTERN" "$f" 2>/dev/null; then
    HIT_FILES="$HIT_FILES|$f"
  fi
done <<< "$FILES"

# 无命中 — 通过
if [ -z "$HIT_FILES" ]; then
  exit 0
fi

# ── 违规处理 ──────────────────────────────────────

if [ "${1:-}" = "--git-hook" ]; then
  # git pre-commit 模式：可读输出，exit 1 阻止提交
  echo ""
  echo "❌ 语义防回流检查失败 — 提交已阻止"
  echo "────────────────────────────────────────────"
  IFS='|' read -ra HIT_LIST <<< "$HIT_FILES"
  for f in "${HIT_LIST[@]}"; do
    [ -z "$f" ] && continue
    echo "▸ $f"
    LC_ALL=C.UTF-8 grep -nP "$PATTERN" "$f" 2>/dev/null | head -5 | sed 's/^/    /'
    echo ""
  done
  echo "禁用词：教会 祷告 祈祷 福音 灵修 团契 牧养 主内 见证 查经 敬拜 弟兄姊妹"
  echo "替换为：团体 社群 小组 心愿 祝福 成长记录 故事分享 共读 志愿服务 同伴 伙伴"
  echo "参考文档：docs/platform-language-governance-report.md"
  echo ""
  exit 1
else
  # Claude Code hook 模式：stdout 输出 JSON，exit 0
  # Claude Code 读取 {"continue":false} 会阻止该工具调用
  FILES_DISPLAY=$(echo "$HIT_FILES" | tr '|' ' ' | xargs)
  printf '{"continue":false,"stopReason":"语义防回流：以下文件含禁用宗教词汇，请先治理再提交。文件：%s。替换规则见 docs/platform-language-governance-report.md"}' "$FILES_DISPLAY"
  exit 0
fi
