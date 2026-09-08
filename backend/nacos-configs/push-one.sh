#!/bin/bash
# Push a single nacos config file (Nacos 3.x compatible)
set -e
NACOS="http://localhost:8848"
NS="57f964ac-059c-4e2b-8138-47bba2b9afb0"
GROUP="DEFAULT_GROUP"

DATA_ID="$1"
FILE="$2"

# Login
TOKEN=$(curl -s -X POST "$NACOS/nacos/v1/auth/users/login" -d "username=nacos&password=nacos" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
  echo "login failed"
  exit 1
fi
echo "token acquired"

CONTENT=$(cat "$FILE")

# Nacos 3.x: POST /nacos/v3/admin/cs/config, params: dataId, groupName, namespaceId, content, type
RESP=$(curl -s -w "%{http_code}" -o /tmp/nacos_resp.txt -X POST "$NACOS/nacos/v3/admin/cs/config" \
  --data-urlencode "dataId=$DATA_ID" \
  --data-urlencode "groupName=$GROUP" \
  --data-urlencode "namespaceId=$NS" \
  --data-urlencode "type=yaml" \
  --data-urlencode "content=$CONTENT" \
  --data-urlencode "accessToken=$TOKEN")
BODY=$(cat /tmp/nacos_resp.txt | head -c 200)
echo "HTTP $RESP body=$BODY"
if [ "$RESP" = "200" ]; then
  echo "OK: $DATA_ID pushed"
  exit 0
fi
echo "FAILED: $DATA_ID"
exit 1
