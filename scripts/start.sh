#!/bin/bash
set -e

# 镜像仓库地址
REGISTRY="124.221.104.56:8211/vrd"

echo "============================================="
echo "  车辆远程诊断系统 - 启动脚本"
echo "============================================="
echo ""

# 创建数据目录（需要 sudo 权限）
if [ ! -d "/data/vrd" ]; then
    sudo mkdir -p /data/vrd/{uploads,logs,temp,dbc,signals,data,storage}
    sudo chown -R $USER:$USER /data/vrd
    sudo chmod -R 755 /data/vrd
    echo "✅ 数据目录创建完成"
else
    echo "✅ 数据目录已存在"
fi

# 服务列表
services=(
    "service-gateway"
    "service-auth"
    "service-vehicle"
    "service-ecu-log"
    "service-dbc"
    "service-signal"
    "service-access"
    "frontend"
)

# ========== 拉取镜像 ==========
echo "1. 拉取最新镜像..."
for service in "${services[@]}"; do
    docker pull "$REGISTRY/$service:latest"
done
echo "✅ 镜像拉取完成"
echo ""

# ========== 启动服务 ==========
echo "2. 启动服务..."
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
echo "停止服务: ./scripts/stop.sh"