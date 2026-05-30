#!/bin/bash

echo "开始构建车辆远程诊断系统..."

cd /workspace/demo/Vehicle-Remote-Diagnosis

echo "1. 构建后端微服务..."
cd backend

for module in service-register service-gateway service-auth service-vehicle service-ecu-log service-dbc service-signal service-bigdata; do
    echo "构建 $module..."
    cd $module
    mvn clean package -DskipTests
    if [ $? -eq 0 ]; then
        echo "$module 构建成功"
    else
        echo "$module 构建失败"
        exit 1
    fi
    cd ..
done

echo "2. 构建前端..."
cd ../frontend
npm install
npm run build
if [ $? -eq 0 ]; then
    echo "前端构建成功"
else
    echo "前端构建失败"
    exit 1
fi

echo "3. 构建Docker镜像..."
cd ..
docker-compose build

echo "4. 启动服务..."
docker-compose up -d

echo "构建和部署完成!"
echo "访问地址: http://localhost:3000"
