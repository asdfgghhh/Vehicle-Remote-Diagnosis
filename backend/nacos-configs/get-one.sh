#!/bin/bash
# Get a single nacos config
NACOS="http://localhost:8848"
NS="57f964ac-059c-4e2b-8138-47bba2b9afb0"
GROUP="DEFAULT_GROUP"
DATA_ID="$1"

TOKEN=$(curl -s -X POST "$NACOS/nacos/v1/auth/users/login" -d "username=nacos&password=nacos" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

# Nacos 3.x: GET /nacos/v3/admin/cs/config
curl -s "$NACOS/nacos/v3/admin/cs/config?dataId=$DATA_ID&groupName=$GROUP&namespaceId=$NS&accessToken=$TOKEN"
