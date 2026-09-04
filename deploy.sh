#!/bin/bash
# =============================================
# VRD 一键构建部署脚本
# 用法: ./deploy.sh [tag] [registry] [branch]
# 示例:
#   ./deploy.sh                                 # 默认: TAG=latest,   registry=124.221.104.56:8211, BRANCH=master
#   ./deploy.sh v1.2.0 124.221.104.56:8211 feature-TLS
# =============================================

set -euo pipefail

TAG=${1:-latest}
REGISTRY=${2:-124.221.104.56:8211}
BRANCH=${3:-master}

BACKEND_DIR="backend"
FRONTEND_DIR="frontend"
SERVICES=(
  "service-gateway"
  "service-auth"
  "service-vehicle"
  "service-ecu-log"
  "service-dbc"
  "service-signal"
  "service-access"
  "service-diagnosis"
)

# ---------- 1. 切换分支 ----------
echo "========================================"
echo "  VRD Build & Deploy"
echo "  Branch:   ${BRANCH}"
echo "  Tag:      ${TAG}"
echo "  Registry: ${REGISTRY}"
echo "========================================"

if [ ! -d .git ]; then
  echo "⚠️  当前目录非 Git 仓库，跳过切换分支步骤（直接按本地代码构建）"
else
  echo ""
  echo "[0/4] 切换到分支 ${BRANCH} ..."
  # 先 fetch 确保远端分支可见
  if ! git remote get-url origin >/dev/null 2>&1; then
    echo "⚠️  未配置 remote origin，跳过 fetch/checkout（直接按本地 detached HEAD 构建）"
  else
    git fetch --prune origin || {
      echo "⚠️  git fetch 失败（可能无外网），继续按本地已有分支/提交构建"
    }
    # 支持 branch / refs/tags/X / commit SHA
    if git show-ref --verify --quiet "refs/remotes/origin/${BRANCH}"; then
      # 远端分支优先（避免本地分支停留在旧 commit）
      git checkout -B "${BRANCH}" "origin/${BRANCH}" || git checkout "origin/${BRANCH}"
    elif git show-ref --verify --quiet "refs/tags/${BRANCH}"; then
      git checkout "refs/tags/${BRANCH}"
    elif git rev-parse --verify "${BRANCH}^{commit}" >/dev/null 2>&1; then
      git checkout "${BRANCH}"
    else
      echo "❌ 分支/标签/提交 '${BRANCH}' 在本地与远端均不存在，已终止"
      exit 1
    fi
  fi

  echo "  当前 HEAD:"
  echo "    branch  : $(git symbolic-ref --short -q HEAD || echo '(detached/tag/sha)')"
  echo "    sha7    : $(git rev-parse --short=7 HEAD)"
  echo "    date    : $(git log -1 --format=%ci)"
  echo "    message : $(git log -1 --format=%s)"
fi

# ---------- 2. 后端构建 ----------
echo ""
echo "[1/4] Building backend with Maven..."
( cd "${BACKEND_DIR}" && mvn clean package -DskipTests -B -q )
echo "  ✅ Backend JARs built"

# ---------- 3. 前端构建 ----------
echo ""
echo "[2/4] Building frontend..."
( cd "${FRONTEND_DIR}" && npm ci --registry=https://registry.npmmirror.com --silent && npm run build )
echo "  ✅ Frontend dist built"

# ---------- 4. Docker 镜像构建（同时打 tag + latest 双标签）----------
echo ""
echo "[3/4] Building Docker images..."

for svc in "${SERVICES[@]}"; do
  echo "  Building ${svc}..."
  docker build \
    --build-arg SERVICE_NAME="${svc}" \
    -t "${REGISTRY}/vrd/${svc}:${TAG}" \
    -t "${REGISTRY}/vrd/${svc}:latest" \
    -f "${BACKEND_DIR}/Dockerfile" \
    "${BACKEND_DIR}"
  echo "  ✅ ${svc}"
done

echo "  Building frontend..."
docker build \
  -t "${REGISTRY}/vrd/frontend:${TAG}" \
  -t "${REGISTRY}/vrd/frontend:latest" \
  -f "${FRONTEND_DIR}/Dockerfile" \
  "${FRONTEND_DIR}"
echo "  ✅ frontend"

# ---------- 5. 推送镜像 ----------
echo ""
echo "[4/4] Pushing images to registry..."

for svc in "${SERVICES[@]}"; do
  docker push "${REGISTRY}/vrd/${svc}:${TAG}"
  docker push "${REGISTRY}/vrd/${svc}:latest"
done

docker push "${REGISTRY}/vrd/frontend:${TAG}"
docker push "${REGISTRY}/vrd/frontend:latest"

echo ""
echo "========================================"
echo "  ✅ All images built and pushed!"
echo "  Branch:   ${BRANCH}"
echo "  Sha7:     $(git rev-parse --short=7 HEAD 2>/dev/null || echo "n/a")"
echo "  Registry: ${REGISTRY}"
echo "  Tag:      ${TAG}"
echo ""
echo "  To deploy, run on target server:"
echo "    export TAG=${TAG} REGISTRY=${REGISTRY}"
echo "    docker compose pull"
echo "    docker compose up -d --remove-orphans"
echo "========================================"
