#!/bin/bash

# Nacos配置导入脚本

# 默认配置
NACOS_ADDR="${NACOS_ADDR:-localhost:8848}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-}"
NACOS_GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 登录Nacos获取token
login() {
    if [ -n "$NACOS_USERNAME" ] && [ -n "$NACOS_PASSWORD" ]; then
        echo "正在登录Nacos..."
        TOKEN=$(curl -s -X POST "http://$NACOS_ADDR/nacos/v1/auth/users/login" \
            -d "username=$NACOS_USERNAME&password=$NACOS_PASSWORD" \
            | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
        
        if [ -n "$TOKEN" ]; then
            echo "登录成功！"
            AUTH_PARAM="&accessToken=$TOKEN"
        else
            echo "登录失败，将使用匿名访问"
            AUTH_PARAM=""
        fi
    else
        AUTH_PARAM=""
    fi
}

# 导入单个配置文件
import_config() {
    local data_id="$1"
    local file_path="$2"
    
    if [ ! -f "$file_path" ]; then
        echo "文件不存在: $file_path"
        return 1
    fi
    
    echo "正在导入配置: $data_id"
    
    # 读取文件内容并进行URL编码
    content=$(cat "$file_path")
    
    # 构建curl命令
    response=$(curl -s -w "%{http_code}" -X POST "http://$NACOS_ADDR/nacos/v1/cs/configs" \
        -d "dataId=$data_id" \
        -d "group=$NACOS_GROUP" \
        -d "content=$content" \
        -d "type=yaml" \
        ${NACOS_NAMESPACE:+-d "namespace=$NACOS_NAMESPACE"} \
        $AUTH_PARAM)
    
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "200" ]; then
        echo "✓ 成功导入: $data_id"
        return 0
    else
        echo "✗ 导入失败: $data_id (HTTP $http_code)"
        return 1
    fi
}

# 主函数
main() {
    echo "============================================="
    echo "  Nacos配置导入脚本"
    echo "============================================="
    echo "Nacos地址: $NACOS_ADDR"
    echo "命名空间: ${NACOS_NAMESPACE:-默认}"
    echo "配置组: $NACOS_GROUP"
    echo ""
    
    # 登录
    login
    
    # 导入公共配置
    if [ -f "$SCRIPT_DIR/application.yml" ]; then
        import_config "application.yml" "$SCRIPT_DIR/application.yml"
    fi
    
    # 导入各服务配置
    for config_file in "$SCRIPT_DIR/service-"*.yml; do
        if [ -f "$config_file" ]; then
            data_id=$(basename "$config_file")
            import_config "$data_id" "$config_file"
        fi
    done
    
    echo ""
    echo "============================================="
    echo "  配置导入完成！"
    echo "============================================="
    echo ""
    echo "访问Nacos控制台: http://$NACOS_ADDR/nacos"
}

# 显示帮助信息
show_help() {
    cat << EOF
用法: $0 [选项]

选项:
  -h, --help              显示帮助信息
  -a, --addr <地址>       Nacos地址 (默认: localhost:8848)
  -n, --namespace <命名空间>  Nacos命名空间ID
  -g, --group <组>        配置组 (默认: DEFAULT_GROUP)
  -u, --username <用户名>  Nacos用户名 (默认: nacos)
  -p, --password <密码>    Nacos密码 (默认: nacos)

示例:
  $0                          # 使用默认配置导入
  $0 -a 192.168.1.100:8848   # 指定Nacos地址
  $0 -n dev -g DEV_GROUP      # 指定命名空间和组
EOF
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -a|--addr)
            NACOS_ADDR="$2"
            shift 2
            ;;
        -n|--namespace)
            NACOS_NAMESPACE="$2"
            shift 2
            ;;
        -g|--group)
            NACOS_GROUP="$2"
            shift 2
            ;;
        -u|--username)
            NACOS_USERNAME="$2"
            shift 2
            ;;
        -p|--password)
            NACOS_PASSWORD="$2"
            shift 2
            ;;
        *)
            echo "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
done

# 执行主函数
main
