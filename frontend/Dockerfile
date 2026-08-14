# 多阶段构建 - Vue3 前端
# 阶段 1: Node.js 构建
FROM node:20-alpine AS builder

WORKDIR /build

COPY package*.json ./
RUN npm ci --registry=https://registry.npmmirror.com

COPY . .
RUN npm run build

# 阶段 2: Nginx 运行时
FROM nginx:1.25-alpine

# 复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# Nginx 配置 (含 API 代理)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 创建日志目录
RUN mkdir -p /logs && chown -R nginx:nginx /logs

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -q --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
