#!/bin/bash

echo "停止车辆远程诊断系统..."

cd /workspace/demo/Vehicle-Remote-Diagnosis

docker-compose down

echo "所有服务已停止"
