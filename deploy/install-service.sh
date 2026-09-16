#!/usr/bin/env bash
# 第一次服务器初始化：安装/更新 systemd service。
# 不迁移项目目录，不修改 nginx，不删除旧目录。
set -euo pipefail

APP_DIR="/opt/lovecube-api"
SERVICE_NAME="lovecube-api"
UNIT_DEST="/etc/systemd/system/${SERVICE_NAME}.service"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
UNIT_SRC="${SCRIPT_DIR}/lovecube-api.service"
ENV_FILE="${APP_DIR}/lovecube-api.env"
ENV_EXAMPLE="${SCRIPT_DIR}/lovecube-api.env.example"
JAR_PATH="${APP_DIR}/lovecube-api.jar"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] [install-service] $*"
}

if [ "$(id -u)" -eq 0 ]; then
  SUDO=""
else
  SUDO="sudo"
fi

if [ ! -f "$UNIT_SRC" ]; then
  log "ERROR: 找不到 unit 文件: $UNIT_SRC"
  exit 1
fi

JAVA_BIN="$(command -v java || true)"
if [ -z "$JAVA_BIN" ]; then
  log "ERROR: 未找到 java，请先安装 JDK 17+"
  exit 1
fi
log "使用 Java: $JAVA_BIN"
java -version 2>&1 || true

log "确保项目目录存在（不迁移现有路径）: $APP_DIR"
$SUDO mkdir -p "$APP_DIR" "$APP_DIR/backup" "$APP_DIR/logs" "$APP_DIR/uploads" "$APP_DIR/deploy"

if [ -d /root/uploads ] || [ -d /root/logs ]; then
  log "WARN: 发现 /root/uploads 或 /root/logs。WorkingDirectory 将固定为 ${APP_DIR}，不会自动迁移这些目录。"
fi

if [ ! -f "$ENV_FILE" ]; then
  if [ -f "$ENV_EXAMPLE" ]; then
    $SUDO cp "$ENV_EXAMPLE" "$ENV_FILE"
    $SUDO chmod 600 "$ENV_FILE"
    log "已创建空的 ${ENV_FILE}（请用 GitHub Secrets / install-infra 写入真实值）"
  else
    $SUDO touch "$ENV_FILE"
    $SUDO chmod 600 "$ENV_FILE"
    log "已创建空的 ${ENV_FILE}"
  fi
else
  log "保留已有 EnvironmentFile: $ENV_FILE"
fi

if [ ! -f "$APP_DIR/application.yml" ]; then
  log "WARN: ${APP_DIR}/application.yml 不存在。启动仍依赖该文件，本脚本不会生成或覆盖它。"
fi

UNIT_TMP="$(mktemp)"
sed "s|@@JAVA_BIN@@|${JAVA_BIN}|g" "$UNIT_SRC" >"$UNIT_TMP"
$SUDO cp "$UNIT_TMP" "$UNIT_DEST"
$SUDO chmod 644 "$UNIT_DEST"
rm -f "$UNIT_TMP"
log "已安装 unit: $UNIT_DEST"

$SUDO cp -f "$SCRIPT_DIR/"*.sh "$APP_DIR/deploy/" 2>/dev/null || true
$SUDO cp -f "$UNIT_SRC" "$APP_DIR/deploy/" 2>/dev/null || true
$SUDO chmod 755 "$APP_DIR/deploy/"*.sh 2>/dev/null || true

log "daemon-reload"
$SUDO systemctl daemon-reload

stop_legacy_nohup() {
  if $SUDO systemctl is-active --quiet "$SERVICE_NAME"; then
    log "systemd 已接管 ${SERVICE_NAME}，跳过旧 nohup 清理"
    return 0
  fi

  local pids=""
  pids="$(ss -ltnp 2>/dev/null | awk '/:8090/ {print $NF}' | sed -n 's/.*pid=\([0-9]\+\).*/\1/p' | sort -u || true)"
  if [ -z "${pids:-}" ]; then
    pids="$(pgrep -f "$JAR_PATH" || true)"
  fi
  if [ -n "${pids:-}" ]; then
    log "停止旧的手动/nohup 进程: $pids"
    # shellcheck disable=SC2086
    echo "$pids" | xargs -r $SUDO kill || true
    sleep 3
    local still=""
    still="$(ss -ltnp 2>/dev/null | awk '/:8090/ {print $NF}' | sed -n 's/.*pid=\([0-9]\+\).*/\1/p' | sort -u || true)"
    if [ -z "${still:-}" ]; then
      still="$(pgrep -f "$JAR_PATH" || true)"
    fi
    if [ -n "${still:-}" ]; then
      log "强制结束残留进程: $still"
      echo "$still" | xargs -r $SUDO kill -9 || true
    fi
  else
    log "未发现旧的 8090/nohup 进程"
  fi
}

stop_legacy_nohup

log "enable ${SERVICE_NAME}"
$SUDO systemctl enable "$SERVICE_NAME"

if $SUDO systemctl is-active --quiet "$SERVICE_NAME"; then
  log "服务已在运行，restart 以加载最新 unit"
  $SUDO systemctl restart "$SERVICE_NAME"
else
  if [ ! -f "$JAR_PATH" ]; then
    log "WARN: ${JAR_PATH} 尚不存在，先 enable，待日常 deploy.sh 发布 jar 后再启动"
    log "SUCCESS: systemd 已安装并开机自启（jar 待发布）"
    exit 0
  fi
  log "start ${SERVICE_NAME}"
  $SUDO systemctl start "$SERVICE_NAME"
fi

if $SUDO systemctl is-active --quiet "$SERVICE_NAME"; then
  log "SUCCESS: ${SERVICE_NAME} 已启用且正在运行"
  $SUDO systemctl --no-pager --full status "$SERVICE_NAME" || true
else
  log "ERROR: ${SERVICE_NAME} 启动失败"
  $SUDO journalctl -u "$SERVICE_NAME" -n 120 --no-pager || true
  exit 1
fi
