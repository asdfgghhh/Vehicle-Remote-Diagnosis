/**
 * Vehicle-Remote-Diagnosis CI/CD Pipeline
 *
 * 功能:
 *   1. 从 GitHub 拉取代码
 *   2. Docker 多阶段构建（Maven / npm 编译）
 *   3. 推送镜像到 Harbor (124.221.104.56:8211)
 *   4. 在服务器上按选择的服务重新部署（docker compose -p vrd）
 *
 * 参数:
 *   SERVICE : 要构建/部署的服务（all = 全部 9 个；或单服务；或 frontend）
 *   ACTION  : build-deploy(默认) / deploy-only / build-only
 *   TAG     : 镜像标签（默认 latest）
 *
 * 前提:
 *   - Jenkins 凭据 harbor-credentials（用户名/密码）已配置
 *   - Jenkins 容器已挂载 Docker socket 与 cli-plugins（compose v2）
 */

pipeline {
    agent any

    environment {
        HARBOR_REGISTRY = '124.221.104.56:8211'
        IMAGE_TAG       = "${params.TAG ?: 'latest'}"
        ALL_SERVICES    = 'service-gateway service-auth service-vehicle service-ecu-log service-dbc service-signal service-access service-diagnosis frontend'
    }

    parameters {
        choice(
            name: 'SERVICE',
            choices: [
                'all',
                'service-gateway',
                'service-auth',
                'service-vehicle',
                'service-ecu-log',
                'service-dbc',
                'service-signal',
                'service-access',
                'service-diagnosis',
                'frontend'
            ],
            description: '选择要构建/部署的服务（all = 全部 9 个服务；可单选单服务重新部署）'
        )
        choice(
            name: 'ACTION',
            choices: ['build-deploy', 'deploy-only', 'build-only'],
            description: 'build-deploy=构建并部署(默认); deploy-only=仅重新部署已有镜像; build-only=仅构建推送不部署'
        )
        string(
            name: 'TAG',
            defaultValue: 'latest',
            description: '镜像标签（如 latest, v1.0, v1.2.1）'
        )
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Harbor Login') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'harbor-credentials',
                    passwordVariable: 'HARBOR_PWD',
                    usernameVariable: 'HARBOR_USER'
                )]) {
                    sh "echo '${HARBOR_PWD}' | docker login ${HARBOR_REGISTRY} -u ${HARBOR_USER} --password-stdin"
                }
            }
        }

        stage('Build & Push Images') {
            when { not { equals expected: 'deploy-only', actual: params.ACTION } }
            steps {
                script {
                    def allServices = env.ALL_SERVICES.split(' ')
                    def services = params.SERVICE == 'all' ? allServices : [params.SERVICE]

                    for (svc in services) {
                        echo "========== Building ${svc} (${IMAGE_TAG}) =========="
                        if (svc == 'frontend') {
                            sh "docker build -t ${HARBOR_REGISTRY}/vrd/${svc}:${IMAGE_TAG} -f frontend/Dockerfile frontend/"
                        } else {
                            sh "docker build --build-arg SERVICE_NAME=${svc} -t ${HARBOR_REGISTRY}/vrd/${svc}:${IMAGE_TAG} -f backend/Dockerfile backend/"
                        }
                        echo "========== Pushing ${svc} =========="
                        sh "docker push ${HARBOR_REGISTRY}/vrd/${svc}:${IMAGE_TAG}"
                    }
                }
            }
        }

        stage('Deploy') {
            when { not { equals expected: 'build-only', actual: params.ACTION } }
            steps {
                script {
                    def allServices = env.ALL_SERVICES.split(' ')
                    def services = params.SERVICE == 'all' ? allServices : [params.SERVICE]

                    // 项目名固定为 vrd，与正在运行的 vrd_vrd-network 保持一致
                    withEnv(["REGISTRY=${HARBOR_REGISTRY}", "TAG=${IMAGE_TAG}"]) {
                        for (svc in services) {
                            echo "========== Deploying ${svc} =========="
                            sh "docker compose -p vrd -f docker-compose.yml pull ${svc}"
                            sh "docker compose -p vrd -f docker-compose.yml up -d --no-deps ${svc}"
                        }
                    }
                }
            }
        }

        stage('Verify') {
            when { not { equals expected: 'build-only', actual: params.ACTION } }
            steps {
                script {
                    def allServices = env.ALL_SERVICES.split(' ')
                    def services = params.SERVICE == 'all' ? allServices : [params.SERVICE]

                    for (svc in services) {
                        def cname = (svc == 'frontend') ? 'vrd-frontend' : "vrd-${svc.replace('service-', '')}"
                        echo "========== Status of ${cname} =========="
                        sh "docker ps --filter name=${cname} --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}'"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "CI/CD succeeded! Service=${params.SERVICE}, Action=${params.ACTION}, Tag=${IMAGE_TAG}"
        }
        failure {
            echo "CI/CD FAILED! Service=${params.SERVICE}, Action=${params.ACTION}, Tag=${IMAGE_TAG}"
        }
        always {
            sh "docker logout ${HARBOR_REGISTRY} || true"
            cleanWs()
        }
        success {
            // 清理悬空镜像，避免磁盘被旧层占满
            sh "docker image prune -f || true"
        }
    }
}
