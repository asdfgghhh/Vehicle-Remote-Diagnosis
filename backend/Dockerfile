# 多阶段构建 Dockerfile - 通用 Java 微服务
# 用法: docker build --build-arg SERVICE_NAME=service-dbc -f Dockerfile ..

# ============ Stage 1: Maven 构建 ============
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 先复制根 pom 和模块 pom，利用 Docker 缓存
COPY pom.xml .
COPY common/pom.xml common/
COPY service-gateway/pom.xml service-gateway/
COPY service-auth/pom.xml service-auth/
COPY service-vehicle/pom.xml service-vehicle/
COPY service-ecu-log/pom.xml service-ecu-log/
COPY service-dbc/pom.xml service-dbc/
COPY service-signal/pom.xml service-signal/
COPY service-access/pom.xml service-access/
COPY service-diagnosis/pom.xml service-diagnosis/

# 下载依赖 (利用缓存)
RUN mvn dependency:go-offline -B -q || true

# 复制全部源码
COPY common/src common/src/
COPY service-gateway/src service-gateway/src/
COPY service-auth/src service-auth/src/
COPY service-vehicle/src service-vehicle/src/
COPY service-ecu-log/src service-ecu-log/src/
COPY service-dbc/src service-dbc/src/
COPY service-signal/src service-signal/src/
COPY service-access/src service-access/src/
COPY service-diagnosis/src service-diagnosis/src/

# 构建 (跳过测试以加速 CI)
RUN mvn package -DskipTests -B -q

# ============ Stage 2: 运行时镜像 ============
FROM eclipse-temurin:17-jre-alpine AS runtime

# 安全加固: 创建非 root 用户
RUN addgroup -g 1000 vrd && adduser -u 1000 -G vrd -D vrd

WORKDIR /app

# 安装 curl (健康检查用)
RUN apk add --no-cache curl

# 复制 JAR 包
ARG SERVICE_NAME=service-dbc
COPY --from=builder /build/${SERVICE_NAME}/target/${SERVICE_NAME}-*.jar app.jar

# 创建日志目录
RUN mkdir -p /logs && chown -R vrd:vrd /app /logs

# 切换到非 root 用户
USER vrd

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/logs/heapdump.hprof \
  -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
