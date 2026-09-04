/**
 * Vehicle-Remote-Diagnosis CI/CD Pipeline
 *
 * 功能:
 *   1. 从 GitHub 拉取指定 BRANCH 代码（默认 master）
 *   2. Docker 多阶段构建（Maven / npm 编译）
 *   3. 推送镜像到 Harbor (124.221.104.56:8211)
 *   4. 在服务器上按选择的服务重新部署（docker compose -p vrd）
 *
 * 参数:
 *   BRANCH          : 下拉选择要部署的代码分支。构建开始时自动从 GitHub 抓取全部远程分支，main 置顶
 *   REFRESH_BRANCHES: 勾选后仅刷新 BRANCH 下拉列表，不构建不部署（新增/删除分支后跑一次即可）
 *   SERVICE         : 要构建/部署的服务（all = 全部 9 个；或单服务；或 frontend）
 *   ACTION  : build-deploy(默认) / deploy-only / build-only
 *   TAG     : 镜像标签（默认 latest，也支持根据分支自动生成：{branch}-{shortCommit}-{yyyyMMddHHmm}）
 *   AUTO_TAG: 为 true 时 TAG 参数被忽略，按 <branch>-<sha7>-<yyyyMMddHHmm> 自动生成
 *
 * 前提:
 *   - Jenkins Job 的 SCM (Git) 配置项 "Branches to build" 需改为:
 *       方式 A（推荐）: 空着（或 ** 不填），让本 Jenkinsfile 内部按 params.BRANCH 手动 fetch + checkout
 *       方式 B        : 填 ${params.BRANCH}，在 Job 参数定义后使用（需按 Jenkins Git plugin 文档开启 "Lightweight checkout" 关闭）
 *   - Jenkins 凭据 harbor-credentials（用户名/密码）已配置
 *   - Jenkins 容器已挂载 Docker socket 与 cli-plugins（compose v2）
 */

// ============================================================================
// 动态获取 Git 远程分支列表（零插件方案）
// ----------------------------------------------------------------------------
// 说明：本 Jenkins 未安装 git-parameter / active-choices 插件，因此无法使用
//       gitParameter 等声明式动态参数。这里改为在执行 pipeline 之前先占用一个
//       executor 执行 `git ls-remote --heads`，把远端分支抓回来后动态生成
//       choice 参数。
// 注意：Jenkins 的参数定义来自「上一次构建」的 properties，所以新增分支后需要
//       先勾选 REFRESH_BRANCHES 跑一次（仅刷新列表、不构建），下拉里才会出现新分支。
// ============================================================================
def DEFAULT_BRANCH = 'main'
def branchChoices  = [DEFAULT_BRANCH]

node {
    // 优先复用 Job SCM 配置里的仓库地址（跟随 config.xml 中的 GitHub 代理地址），
    // 取不到时回退到硬编码地址。
    def repoUrl = 'https://gh-proxy.com/https://github.com/asdfgghhh/Vehicle-Remote-Diagnosis.git'
    try {
        def u = scm.userRemoteConfigs[0].url
        if (u != null && u.trim()) {
            repoUrl = u.trim()
        }
    } catch (err) {
        echo "WARN: 无法从 scm 读取仓库地址，使用内置默认地址。原因: ${err.getMessage()}"
    }
    echo "Fetching remote branches from: ${repoUrl}"

    try {
        timeout(time: 90, unit: 'SECONDS') {
            def remote = ''
            withEnv(["REPO_URL=${repoUrl}"]) {
                remote = sh(
                    script: '''git ls-remote --heads "$REPO_URL" | awk '{print $2}' | sed 's|refs/heads/||' | sort -u''',
                    returnStdout: true
                ).trim()
            }
            def list = remote ? remote.split('\\n').collect { it.trim() }.findAll { it } : []
            if (list) {
                // 默认分支置顶，其余按字母序
                list.removeAll { it == DEFAULT_BRANCH }
                branchChoices = [DEFAULT_BRANCH] + list
            }
            echo "已获取远程分支 ${branchChoices.size()} 个: ${branchChoices.join(', ')}"
        }
    } catch (err) {
        echo "WARN: 获取远程分支失败，回退为仅包含默认分支的下拉列表。原因: ${err.getMessage()}"
    }
}

pipeline {
    agent any

    environment {
        HARBOR_REGISTRY = '124.221.104.56:8211'
        ALL_SERVICES    = 'service-gateway service-auth service-vehicle service-ecu-log service-dbc service-signal service-access service-diagnosis frontend'
    }

    parameters {
        choice(
            name: 'BRANCH',
            choices: branchChoices,
            description: '选择要部署的分支（列表在每次构建开始时自动从 GitHub 抓取；如看不到新分支，请勾选 REFRESH_BRANCHES 先跑一次）'
        )
        booleanParam(
            name: 'REFRESH_BRANCHES',
            defaultValue: false,
            description: '仅刷新 BRANCH 下拉列表，不执行构建与部署（新增/删除分支后用一次即可）'
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
            description: '镜像标签（AUTO_TAG=true 时忽略）；示例：latest, v1.0, feature-TLS-20260901'
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
                    echo "  REFRESH      = ${params.REFRESH_BRANCHES}"
                    echo "============================================================"

                    // 是否跳过后续构建/部署（仅刷新分支下拉列表）
                    env.SKIP_MAIN = (params.REFRESH_BRANCHES ? 'true' : 'false')

                    if (env.SKIP_MAIN == 'true') {
                        currentBuild.description = "刷新分支列表（已同步 ${branchChoices.size()} 个）"
                        echo "已勾选 REFRESH_BRANCHES：本次仅刷新 BRANCH 下拉列表，不执行构建与部署。"
                        echo "当前可选分支(${branchChoices.size()}): ${branchChoices.join(', ')}"
                    }

                    // 安全校验：禁止空分支（仅刷新模式不校验）
                    if (env.SKIP_MAIN != 'true') {
                        if (params.BRANCH == null || params.BRANCH.trim().isEmpty()) {
                            error('BRANCH 参数不能为空')
                        }
                    }
                    env.CI_BRANCH = params.BRANCH ? params.BRANCH.trim() : ''
                }
            }
        }

        stage('Checkout (按 BRANCH 切换)') {
            when { expression { env.SKIP_MAIN != 'true' } }
            steps {
                script {
                    // 使用 Jenkins Git plugin 提供的 checkout 能力。
                    // scm.branches 若在 Job UI 固定为 "*/master"，会导致脚本无法切换分支。
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
            when { expression { env.SKIP_MAIN != 'true' } }
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
            when { expression { env.SKIP_MAIN != 'true' } }
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
            when {
                expression { env.SKIP_MAIN != 'true' }
                not { equals expected: 'deploy-only', actual: params.ACTION }
            }
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
            when {
                expression { env.SKIP_MAIN != 'true' }
                not { equals expected: 'build-only', actual: params.ACTION }
            }
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
            when {
                expression { env.SKIP_MAIN != 'true' }
                not { equals expected: 'build-only', actual: params.ACTION }
            }
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
