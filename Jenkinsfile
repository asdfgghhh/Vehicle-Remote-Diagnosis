/**
 * Vehicle-Remote-Diagnosis CI/CD Pipeline
 *
 * 功能:
 *   1. 从 GitHub 拉取指定 BRANCH 代码（Git Parameter 实时列出远程分支供下拉选择）
 *   2. Docker 多阶段构建（Maven / npm 编译）
 *   3. 推送镜像到 Harbor (124.221.104.56:8211)
 *   4. 在服务器上按选择的服务重新部署（docker compose -p vrd）
 *
 * 参数:
 *   BRANCH  : 下拉选择要部署的代码分支。由 git-parameter 插件在打开构建页面时实时
 *             执行 git ls-remote 获取远程分支，新增分支立即可见，无需预刷新
 *   SERVICE : 要构建/部署的服务（all = 全部 9 个；或单服务；或 frontend）
 *   ACTION  : build-deploy(默认) / deploy-only / build-only
 *   TAG     : 镜像标签（默认 latest，AUTO_TAG=true 时按 <branch>-<sha7>-<yyyyMMddHHmm> 生成）
 *   AUTO_TAG: 为 true 时 TAG 参数被忽略
 *
 * 前提:
 *   - 已安装 git-parameter 插件（2026-09-04 安装，版本 462.463.v496a_59f698e5）
 *   - Job 的 SCM (Git) "Branches to build" 保持 main 即可，本 Jenkinsfile 内部
 *     会按 params.BRANCH 动态 checkout 到目标分支
 *   - Jenkins 凭据 harbor-credentials（用户名/密码）已配置
 *   - Jenkins 容器已挂载 Docker socket 与 cli-plugins（compose v2）
 */

pipeline {
    agent any

    environment {
        HARBOR_REGISTRY = '124.221.104.56:8211'
        ALL_SERVICES    = 'service-gateway service-auth service-vehicle service-ecu-log service-dbc service-signal service-access service-diagnosis frontend'
    }

    parameters {
        gitParameter(
            name: 'BRANCH',
            type: 'PT_BRANCH',
            defaultValue: 'main',
            description: '选择要部署的分支（实时从 GitHub 拉取，新分支无需预刷新；可输入关键字过滤）',
            branchFilter: '.*',
            selectedValue: 'DEFAULT',
            sortMode: 'ASCENDING_SMART',
            quickFilterEnabled: true
        )
        booleanParam(
            name: 'AUTO_TAG',
            defaultValue: true,
            description: '是否按 <分支>-<commitSHA7>-<时间戳> 自动生成镜像 TAG；为 true 时手动 TAG 输入被忽略'
        )
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
            description: '镜像标签（AUTO_TAG=true 时忽略）；示例：latest, v1.0'
        )
    }

    stages {

        stage('Resolve Branch') {
            steps {
                script {
                    echo "============================================================"
                    echo "  BRANCH       = ${params.BRANCH}"
                    echo "  SERVICE      = ${params.SERVICE}"
                    echo "  ACTION       = ${params.ACTION}"
                    echo "  AUTO_TAG     = ${params.AUTO_TAG}"
                    echo "  USER TAG     = ${params.TAG}"
                    echo "============================================================"

                    // 安全校验：禁止空分支
                    if (params.BRANCH == null || params.BRANCH.toString().trim().isEmpty()) {
                        error('BRANCH 参数不能为空')
                    }

                    // gitParameter 取自远程 ref，返回值可能带 origin/ 前缀，统一去掉
                    env.CI_BRANCH = params.BRANCH.toString()
                                        .replaceFirst('^origin/', '')
                                        .replaceFirst('^refs/heads/', '')
                                        .trim()
                    echo "  解析后的分支 = ${env.CI_BRANCH}"
                }
            }
        }

        stage('Checkout (按 BRANCH 切换)') {
            steps {
                script {
                    // 使用 Jenkins Git plugin 提供的 checkout 能力。
                    // scm.branches 若在 Job UI 固定为 "*/main"，会导致脚本无法切换分支。
                    // 这里用动态 checkout 明确按 env.CI_BRANCH 拉取，保证 BRANCH 参数生效。
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "refs/heads/${env.CI_BRANCH}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [
                            [$class: 'CloneOption',
                                depth: 0,
                                shallow: false,
                                noTags: false,
                                honorRefspec: false,
                                timeout: 15],
                            [$class: 'CheckoutOption', timeout: 15],
                            [$class: 'CleanBeforeCheckout']
                        ],
                        submoduleCfg: [],
                        userRemoteConfigs: scm.userRemoteConfigs
                    ])

                    // 若是 tag/sha 命中不到 refs/heads，尝试通用 checkout
                    sh '''
                        set +e
                        # 记录当前实际 HEAD
                        echo "Current branch:" && (git symbolic-ref --short -q HEAD || echo "(detached/sha or tag)")
                        echo "Commit SHA:   $(git rev-parse --short HEAD)"
                        echo "Commit Date:  $(git log -1 --format=%ci)"
                        echo "Commit Msg :  $(git log -1 --format=%s)"
                    '''

                    env.CI_SHORT_SHA = sh(returnStdout: true, script: 'git rev-parse --short=7 HEAD').trim()
                    env.CI_COMMIT_DATE = sh(returnStdout: true, script: 'git log -1 --format=%cd --date=format:%Y%m%d%H%M').trim()
                }
            }
        }

        stage('Resolve Image Tag') {
            steps {
                script {
                    if (params.AUTO_TAG) {
                        // 例如：feature-TLS_185e142_202609041132（分支名中 "/" 会替换为 "-"）
                        def safeBranch = env.CI_BRANCH.replaceAll('/', '-')
                        env.IMAGE_TAG = "${safeBranch}-${env.CI_SHORT_SHA}-${env.CI_COMMIT_DATE}"
                    } else {
                        env.IMAGE_TAG = (params.TAG == null || params.TAG.trim().isEmpty()) ? 'latest' : params.TAG.trim()
                    }
                    echo "最终镜像 TAG = ${env.IMAGE_TAG}"
                }
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
                        echo "========== Building ${svc} (${env.IMAGE_TAG}) =========="
                        if (svc == 'frontend') {
                            sh "docker build -t ${HARBOR_REGISTRY}/vrd/${svc}:${env.IMAGE_TAG} -t ${HARBOR_REGISTRY}/vrd/${svc}:latest -f frontend/Dockerfile frontend/"
                        } else {
                            sh "docker build --build-arg SERVICE_NAME=${svc} -t ${HARBOR_REGISTRY}/vrd/${svc}:${env.IMAGE_TAG} -t ${HARBOR_REGISTRY}/vrd/${svc}:latest -f backend/Dockerfile backend/"
                        }
                        echo "========== Pushing ${svc} =========="
                        sh "docker push ${HARBOR_REGISTRY}/vrd/${svc}:${env.IMAGE_TAG}"
                        sh "docker push ${HARBOR_REGISTRY}/vrd/${svc}:latest"
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

                    withEnv(["REGISTRY=${HARBOR_REGISTRY}", "TAG=${env.IMAGE_TAG}"]) {
                        echo "========== 部署信息 =========="
                        echo "  Branch : ${env.CI_BRANCH}"
                        echo "  SHA    : ${env.CI_SHORT_SHA}"
                        echo "  Tag    : ${env.IMAGE_TAG}"
                        echo "  Service: ${params.SERVICE}"
                        echo "==============================="
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
                        sh "docker ps --filter name=${cname} --format 'table {{.Names}}\\t{{.Image}}\\t{{.Status}}'"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "CI/CD succeeded! Branch=${env.CI_BRANCH} SHA=${env.CI_SHORT_SHA} Service=${params.SERVICE}, Action=${params.ACTION}, Tag=${env.IMAGE_TAG}"
            sh "docker image prune -f || true"
        }
        failure {
            echo "CI/CD FAILED! Branch=${env.CI_BRANCH} Service=${params.SERVICE}, Action=${params.ACTION}, Tag=${env.IMAGE_TAG}"
        }
        always {
            sh "docker logout ${HARBOR_REGISTRY} || true"
            cleanWs()
        }
    }
}
