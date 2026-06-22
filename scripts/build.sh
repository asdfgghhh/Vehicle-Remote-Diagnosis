#!/bin/bash
set -e

# 镜像仓库地址
REGISTRY="124.221.104.56:8211/vrd"

echo "============================================="
echo "  车辆远程诊断系统 - 构建与部署脚本"
echo "============================================="
echo ""
echo "镜像仓库: $REGISTRY"
echo ""

# 自动修复行尾符
find . -type f \( -name "*.sh" -o -name "mvnw" \) -exec sed -i 's/\r$//' {} \; -exec chmod +x {} \; 2>/dev/null || true

# 创建数据目录（需要 sudo 权限）
if [ ! -d "/data/vrd" ]; then
    sudo mkdir -p /data/vrd/{uploads,logs,temp,dbc,signals,data,storage}
    sudo chown -R $USER:$USER /data/vrd
    sudo chmod -R 755 /data/vrd
    echo "✅ 数据目录创建完成"
else
    echo "✅ 数据目录已存在"
fi

# Maven Wrapper
MVN="./backend/mvnw"
chmod +x "$MVN" 2>/dev/null || true

# ========== 构建后端 ==========
echo "1. 构建后端..."
cd backend
"$MVN" -f common/pom.xml clean package -DskipTests

for module in service-gateway service-auth service-vehicle service-ecu-log service-dbc service-signal service-access; do
    "$MVN" -f $module/pom.xml clean package -DskipTests
done
cd ..
echo "✅ 后端构建完成"
echo ""

# ========== 构建前端 ==========
echo "2. 构建前端..."
cd frontend
npm install --legacy-peer-deps
npm run build
cd ..
echo "✅ 前端构建完成"
echo ""

# ========== 构建镜像 ==========
echo "3. 构建 Docker 镜像..."
services=(
    "service-gateway:service-gateway"
    "service-auth:service-auth"
    "service-vehicle:service-vehicle"
    "service-ecu-log:service-ecu-log"
    "service-dbc:service-dbc"
    "service-signal:service-signal"
    "service-access:service-access"
    "frontend:frontend"
)

for item in "${services[@]}"; do
    IFS=':' read -r docker_name image_name <<< "$item"
    docker build -t "$image_name" -f "backend/$docker_name/Dockerfile" "backend/$docker_name"
    echo "✅ $image_name 构建完成"
done
echo ""

# ========== 推送镜像 ==========
echo "4. 推送镜像..."
TAG=$(date +%Y%m%d%H%M%S)

for item in "${services[@]}"; do
    IFS=':' read -r docker_name image_name <<< "$item"
    docker tag "$image_name:latest" "$REGISTRY/$image_name:$TAG"
    docker tag "$image_name:latest" "$REGISTRY/$image_name:latest"
    docker push "$REGISTRY/$image_name:$TAG"
    docker push "$REGISTRY/$image_name:latest"
    echo "✅ $image_name 推送完成"
done
echo ""

# ========== 启动服务 ==========
echo "5. 启动服务..."
docker-compose up -d
echo "✅ 服务启动中..."
sleep 30

echo ""
echo "============================================="
echo "  完成!"
echo "============================================="
echo ""
echo "访问地址:"
echo "  前端: http://localhost:3000"
echo "  网关: http://localhost:8080"
echo ""
echo "查看状态: docker-compose ps"
echo "查看日志: docker-compose logs -f <服务名>"