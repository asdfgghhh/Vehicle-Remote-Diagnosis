#!/bin/bash

# Doris启动脚本
# 使用方法: ./start-doris.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DORIS_DIR="$(dirname "$SCRIPT_DIR")"

echo "============================================"
echo "  车辆远程诊断 - Doris 启动脚本"
echo "============================================"
echo ""

# 检查Docker是否可用
if ! command -v docker &> /dev/null; then
    echo "❌ 错误: Docker 未安装或未启动"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    if ! docker compose version &> /dev/null; then
        echo "❌ 错误: docker-compose 或 docker compose 未安装"
        exit 1
    fi
fi

echo "✅ Docker 已就绪"

# 检查网络
if ! docker network ls | grep -q "vrd-network"; then
    echo "创建网络: vrd-network"
    docker network create vrd-network
fi

cd "$DORIS_DIR"

# 清理旧容器
echo ""
echo "清理旧的Doris容器..."
docker-compose -f docker-compose-doris.yml down --volumes 2>/dev/null || true

# 启动Doris FE和BE
echo ""
echo "启动Doris FE和BE..."
if command -v docker-compose &> /dev/null; then
    docker-compose -f docker-compose-doris.yml up -d
else
    docker compose -f docker-compose-doris.yml up -d
fi

echo ""
echo "============================================"
echo "  Doris正在启动中..."
echo "============================================"
echo ""
echo "预计需要1-2分钟"
echo ""
echo "状态检查命令:"
echo "  docker-compose -f docker-compose-doris.yml logs -f"
echo ""
echo "FE Web控制台: http://localhost:8030"
echo "FE MySQL端口: localhost:9030"
echo ""
echo "等待FE就绪后，运行以下命令初始化数据库:"
echo "  ./init-doris-db.sh"
echo ""
echo "============================================"
