#!/bin/bash

# Doris停止脚本
# 使用方法: ./stop-doris.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DORIS_DIR="$(dirname "$SCRIPT_DIR")"

echo "============================================"
echo "  停止Doris服务"
echo "============================================"
echo ""

cd "$DORIS_DIR"

if command -v docker-compose &> /dev/null; then
    docker-compose -f docker-compose-doris.yml down
else
    docker compose -f docker-compose-doris.yml down
fi

echo ""
echo "✅ Doris服务已停止"
echo ""
