#!/bin/bash

echo "启动车辆远程诊断系统..."

cd /workspace/demo/Vehicle-Remote-Diagnosis

echo "1. 创建必要的目录..."
mkdir -p /data/vrd/{uploads,logs,temp,dbc,signals,data}

echo "2. 启动基础设施服务..."
docker-compose up -d mysql redis zookeeper kafka mosquitto hadoop

echo "等待基础设施服务启动..."
sleep 30

echo "3. 启动微服务..."
docker-compose up -d service-register

sleep 10

docker-compose up -d service-gateway service-auth service-vehicle service-ecu-log service-dbc service-signal service-bigdata

sleep 10

echo "4. 启动前端..."
docker-compose up -d frontend

echo ""
echo "系统已启动!"
echo "前端地址: http://localhost:3000"
echo "API网关: http://localhost:8080"
echo "服务注册中心: http://localhost:8761"
echo "Kafka: localhost:9092"
echo "MQTT: localhost:1883"
echo "Hadoop: http://localhost:50070"
