#!/usr/bin/env bash
# Love Cube 健康检查。不修改任何线上目录。
set -euo pipefail

APP_DIR="/opt/lovecube-api"
WEB_DIR="/var/www/lovecube"
SERVICE_NAME="lovecube-api"
HEALTH_URL="http://127.0.0.1:8090/admin/api/announcements"
STORAGE_URL="http://127.0.0.1:8090/admin/api/upload/storage-status"
WAIT_SECONDS="${WAIT_SECONDS:-120}"
SLEEP_SECONDS=5

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] [health] $*"
}

usage() {
  echo "用法: $0 <backend|frontend>"
  exit 2
}

wait_for_backend() {
  local elapsed=0
  log "等待后端就绪: ${HEALTH_URL} (最多 ${WAIT_SECONDS}s)"
  while [ "$elapsed" -lt "$WAIT_SECONDS" ]; do
    if curl -fsS "$HEALTH_URL" >/tmp/lovecube-health.json 2>/dev/null; then
      log "公告接口已响应"
      return 0
    fi
    sleep "$SLEEP_SECONDS"
    elapsed=$((elapsed + SLEEP_SECONDS))
  done
  log "ERROR: ${WAIT_SECONDS}s 内健康检查接口无响应"
  return 1
}

check_backend() {
  if ! wait_for_backend; then
    if command -v journalctl >/dev/null 2>&1; then
      log "最近 journal 日志:"
      journalctl -u "$SERVICE_NAME" -n 120 --no-pager || true
    fi
    if [ -f "$APP_DIR/logs/love-cube.log" ]; then
      log "最近应用文件日志:"
      tail -n 80 "$APP_DIR/logs/love-cube.log" || true
    fi
    return 1
  fi

  log "检查存储模式: ${STORAGE_URL}"
  if ! curl -fsS "$STORAGE_URL" >/tmp/lovecube-storage.json; then
    log "ERROR: storage-status 接口失败"
    return 1
  fi
  if ! grep -q '"mode":"oss"' /tmp/lovecube-storage.json; then
    log "ERROR: 存储模式不是 oss"
    cat /tmp/lovecube-storage.json || true
    return 1
  fi

  log "健康检查响应（前 300 字符）:"
  head -c 300 /tmp/lovecube-health.json || true
  echo
  log "存储状态:"
  cat /tmp/lovecube-storage.json || true
  echo
  log "backend OK"
}

check_frontend() {
  if [ ! -s "$WEB_DIR/index.html" ]; then
    log "ERROR: 缺少 ${WEB_DIR}/index.html"
    return 1
  fi
  log "frontend OK (${WEB_DIR}/index.html)"
}

TARGET="${1:-}"
case "$TARGET" in
  backend) check_backend ;;
  frontend) check_frontend ;;
  *) usage ;;
esac
