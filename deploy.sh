#!/bin/bash
# =============================================
# VRD 一键构建部署脚本
# 用法: ./deploy.sh [tag] [registry]
# 示例: ./deploy.sh v1.2.0 124.221.104.56:8211
# =============================================

set -euo pipefail

TAG=${1:-latest}
REGISTRY=${2:-124.221.104.56:8211}
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

echo "========================================"
echo "  VRD Build & Deploy"
echo "  Tag:      ${TAG}"
echo "  Registry: ${REGISTRY}"
echo "========================================"

# ============ 后端构建 ============
echo ""
echo "[1/4] Building backend with Maven..."
cd "${BACKEND_DIR}"
mvn clean package -DskipTests -B -q
cd ..
echo "  ✅ Backend JARs built"

# ============ 前端构建 ============
echo ""
echo "[2/4] Building frontend..."
cd "${FRONTEND_DIR}"
npm ci --registry=https://registry.npmmirror.com --silent
npm run build
cd ..
echo "  ✅ Frontend dist built"

# ============ Docker 镜像构建 ============
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

# ============ 推送镜像 ============
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
echo "  Registry: ${REGISTRY}"
echo "  Tag:      ${TAG}"
echo ""
echo "  To deploy, run on target server:"
echo "    export TAG=${TAG} REGISTRY=${REGISTRY}"
echo "    docker compose pull"
echo "    docker compose up -d --remove-orphans"
echo "========================================"
