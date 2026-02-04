pipeline {
    agent any

    environment {
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk'
        MAVEN_OPTS = '-Xmx1024m -Xms512m'
        SONAR_HOST_URL = credentials('sonar_host_url')
        SONAR_LOGIN = credentials('sonar_login_token')
        DOCKER_REGISTRY = credentials('docker_registry_url')
        DOCKER_CREDENTIALS = credentials('docker_credentials')
    }

    options {
        timestamps()
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '30', daysToKeepStr: '30'))
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Cloning repository...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Building project...'
                sh 'mvn clean package -DskipTests -Dmaven.test.skip=true'
            }
        }

        stage('Unit Tests') {
            steps {
                echo '🧪 Running unit tests...'
                sh 'mvn test'
            }
        }

        stage('Integration Tests') {
            steps {
                echo '🔗 Running integration tests...'
                sh 'mvn verify -Pintegration'
            }
        }

        stage('Code Analysis - PMD, SpotBugs, Checkstyle') {
            steps {
                echo '🔍 Running static code analysis...'
                sh 'mvn -Panalysis pmd:pmd spotbugs:spotbugs checkstyle:check'
                
                // Archive reports
                archiveArtifacts artifacts: '**/target/pmd.xml,**/target/spotbugsXml.xml,**/target/checkstyle-result.xml',
                    allowEmptyArchive: true
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo '📊 Analyzing code with SonarQube...'
                sh '''
                    mvn sonar:sonar \
                        -Dsonar.projectKey=${JOB_NAME} \
                        -Dsonar.projectName="${JOB_NAME}" \
                        -Dsonar.sources=. \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONAR_LOGIN} \
                        -Dsonar.exclusions=node_modules/**,**/target/**,**/dist/**
                '''
            }
        }

        stage('Build Docker Images') {
            when {
                branch 'develop'
            }
            steps {
                echo '🐳 Building Docker images...'
                sh '''
                    docker build -t ${DOCKER_REGISTRY}/buy-02-user-service:${BUILD_NUMBER} ./user-service
                    docker build -t ${DOCKER_REGISTRY}/buy-02-product-service:${BUILD_NUMBER} ./product-service
                    docker build -t ${DOCKER_REGISTRY}/buy-02-media-service:${BUILD_NUMBER} ./media-service
                    docker build -t ${DOCKER_REGISTRY}/buy-02-frontend:${BUILD_NUMBER} ./frontend-angular
                '''
            }
        }

        stage('Push Docker Images') {
            when {
                branch 'develop'
            }
            steps {
                echo '📤 Pushing Docker images to registry...'
                sh '''
                    echo ${DOCKER_CREDENTIALS_PSW} | docker login -u ${DOCKER_CREDENTIALS_USR} --password-stdin ${DOCKER_REGISTRY}
                    docker push ${DOCKER_REGISTRY}/buy-02-user-service:${BUILD_NUMBER}
                    docker push ${DOCKER_REGISTRY}/buy-02-product-service:${BUILD_NUMBER}
                    docker push ${DOCKER_REGISTRY}/buy-02-media-service:${BUILD_NUMBER}
                    docker push ${DOCKER_REGISTRY}/buy-02-frontend:${BUILD_NUMBER}
                '''
            }
        }

        stage('Deploy to Dev') {
            when {
                branch 'develop'
            }
            steps {
                echo '🚀 Deploying to development environment...'
                sh '''
                    docker compose -f docker-compose.dev.yml down
                    docker compose -f docker-compose.dev.yml up -d --pull always
                '''
            }
        }

        stage('Smoke Tests') {
            when {
                branch 'develop'
            }
            steps {
                echo '🔥 Running smoke tests...'
                sh 'bash scripts/ci-smoke.sh'
            }
        }
    }

    post {
        always {
            echo '🧹 Cleaning up...'
            // Archive test reports
            junit(testResults: '**/target/surefire-reports/TEST-*.xml', allowEmptyResults: true)
            
            // Publish code coverage
            publishHTML([
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'Code Coverage Report',
                keepAll: true
            ])
        }

        success {
            echo '✅ Build successful!'
            // Send success notification
        }

        failure {
            echo '❌ Build failed!'
            // Send failure notification
        }

        unstable {
            echo '⚠️ Build unstable!'
        }

        cleanup {
            deleteDir()
        }
    }
}
