#!/bin/bash

echo "============================================="
echo "  车辆远程诊断系统 - 停止脚本"
echo "============================================="

docker-compose down

echo ""
echo "✅ 服务已停止"
echo ""
echo "启动服务: ./scripts/start.sh"