#!/usr/bin/env bash
# 日常发布：备份 -> 发布 -> restart systemd -> 健康检查 -> 失败回滚。
# 不修改 nginx，不迁移目录，不删除项目根目录。
set -euo pipefail

APP_DIR="/opt/lovecube-api"
JAR_NAME="lovecube-api.jar"
JAR_PATH="${APP_DIR}/${JAR_NAME}"
BACKUP_DIR="${APP_DIR}/backup"
WEB_DIR="/var/www/lovecube"
WEB_BACKUP_DIR="/var/www/lovecube-backups"
SERVICE_NAME="lovecube-api"
KEEP_BACKUPS=5
DEFAULT_BACKEND_RELEASE="/tmp/lovecube-api-release"
DEFAULT_FRONTEND_RELEASE="/tmp/lovecube-web-release"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] [deploy] $*"
}

usage() {
  echo "用法: $0 <backend|frontend> [release_dir]"
  exit 2
}

if [ "$(id -u)" -eq 0 ]; then
  SUDO=""
else
  SUDO="sudo"
fi

prune_backups() {
  local dir="$1"
  local keep="$2"
  [ -d "$dir" ] || return 0
  local extra
  extra="$(ls -1dt "$dir"/* 2>/dev/null | tail -n +"$((keep + 1))" || true)"
  if [ -n "$extra" ]; then
    log "清理过期备份:"
    echo "$extra"
    echo "$extra" | xargs -r $SUDO rm -rf
  fi
}

run_health() {
  local target="$1"
  bash "${SCRIPT_DIR}/health-check.sh" "$target"
}

deploy_backend() {
  local release_dir="${1:-}"
  if [ -z "$release_dir" ]; then
    release_dir="$DEFAULT_BACKEND_RELEASE"
  fi
  log "后端发布目录: $release_dir"

  if ! $SUDO systemctl cat "${SERVICE_NAME}.service" >/dev/null 2>&1; then
    log "ERROR: ${SERVICE_NAME}.service 未安装。请先跑 install-service.sh 或 GitHub Actions install-infra，禁止回退到 nohup。"
    exit 1
  fi

  $SUDO mkdir -p "$APP_DIR" "$BACKUP_DIR" "$APP_DIR/deploy" "$release_dir"
  $SUDO cp -f "$SCRIPT_DIR/"*.sh "$APP_DIR/deploy/" 2>/dev/null || true
  $SUDO chmod 755 "$APP_DIR/deploy/"*.sh 2>/dev/null || true

  local new_jar=""
  new_jar="$(ls -t "$release_dir"/*.jar 2>/dev/null | grep -v '[.]original$' | head -n 1 || true)"
  if [ -z "${new_jar:-}" ] || [ ! -f "$new_jar" ]; then
    log "ERROR: ${release_dir} 中没有可发布的 jar"
    exit 1
  fi
  log "新 jar: $new_jar"

  local backup_file=""
  if [ -f "$JAR_PATH" ]; then
    backup_file="${BACKUP_DIR}/lovecube-api-$(date +%Y%m%d-%H%M%S).jar"
    $SUDO cp -a "$JAR_PATH" "$backup_file"
    log "已备份旧 jar: $backup_file"
    prune_backups "$BACKUP_DIR" "$KEEP_BACKUPS"
  else
    log "当前没有旧 jar，跳过备份"
  fi

  $SUDO cp -f "$new_jar" "$JAR_PATH"
  $SUDO chmod 644 "$JAR_PATH"
  log "已发布 jar -> $JAR_PATH"

  rollback_backend() {
    if [ -z "${backup_file:-}" ] || [ ! -f "$backup_file" ]; then
      log "ERROR: 没有可回滚的备份 jar"
      return 1
    fi
    log "回滚 jar <- $backup_file"
    $SUDO cp -a "$backup_file" "$JAR_PATH" || return 1
    $SUDO systemctl restart "$SERVICE_NAME" || return 1
    run_health backend || return 1
    return 0
  }

  log "restart ${SERVICE_NAME}"
  if ! $SUDO systemctl restart "$SERVICE_NAME"; then
    log "ERROR: systemctl restart 失败，尝试回滚"
    $SUDO journalctl -u "$SERVICE_NAME" -n 120 --no-pager || true
    if rollback_backend; then
      log "已回滚到上一版本，但本次发布失败"
    fi
    exit 1
  fi

  if ! run_health backend; then
    log "ERROR: 健康检查失败，开始回滚"
    $SUDO journalctl -u "$SERVICE_NAME" -n 160 --no-pager || true
    if rollback_backend; then
      log "已回滚到上一版本，但本次发布失败"
    else
      log "ERROR: 回滚也失败，请立即检查 journalctl -u ${SERVICE_NAME}"
    fi
    exit 1
  fi

  log "SUCCESS: 后端发布完成"
}

deploy_frontend() {
  local release_dir="${1:-}"
  if [ -z "$release_dir" ]; then
    release_dir="$DEFAULT_FRONTEND_RELEASE"
  fi
  log "前端发布目录: $release_dir"

  if [ ! -s "${release_dir}/index.html" ]; then
    log "ERROR: ${release_dir}/index.html 不存在，拒绝发布"
    exit 1
  fi

  $SUDO mkdir -p "$WEB_DIR" "$WEB_BACKUP_DIR"

  local backup_dir=""
  if [ -s "${WEB_DIR}/index.html" ] || [ -n "$(ls -A "$WEB_DIR" 2>/dev/null || true)" ]; then
    backup_dir="${WEB_BACKUP_DIR}/$(date +%Y%m%d-%H%M%S)"
    $SUDO mkdir -p "$backup_dir"
    $SUDO cp -a "${WEB_DIR}/." "$backup_dir/"
    log "已备份旧前端: $backup_dir"
    prune_backups "$WEB_BACKUP_DIR" "$KEEP_BACKUPS"
  else
    log "当前前端目录为空，跳过备份"
  fi

  log "同步新版本到 ${WEB_DIR}（保留目录本身，不 rm -rf 项目根）"
  $SUDO find "$WEB_DIR" -mindepth 1 -maxdepth 1 -exec rm -rf {} +
  $SUDO cp -a "${release_dir}/." "$WEB_DIR/"

  rollback_frontend() {
    if [ -z "${backup_dir:-}" ] || [ ! -d "$backup_dir" ]; then
      log "ERROR: 没有可回滚的前端备份"
      return 1
    fi
    log "回滚前端 <- $backup_dir"
    $SUDO find "$WEB_DIR" -mindepth 1 -maxdepth 1 -exec rm -rf {} + || return 1
    $SUDO cp -a "${backup_dir}/." "$WEB_DIR/" || return 1
    run_health frontend || return 1
    return 0
  }

  if ! run_health frontend; then
    log "ERROR: 前端健康检查失败，开始回滚"
    if rollback_frontend; then
      log "已回滚到上一版本，但本次发布失败"
    else
      log "ERROR: 前端回滚也失败"
    fi
    exit 1
  fi

  log "SUCCESS: 前端发布完成"
}

TARGET="${1:-}"
RELEASE_DIR="${2:-}"
case "$TARGET" in
  backend)
    if [ -n "$RELEASE_DIR" ]; then
      deploy_backend "$RELEASE_DIR"
    else
      deploy_backend
    fi
    ;;
  frontend)
    if [ -n "$RELEASE_DIR" ]; then
      deploy_frontend "$RELEASE_DIR"
    else
      deploy_frontend
    fi
    ;;
  *) usage ;;
esac
