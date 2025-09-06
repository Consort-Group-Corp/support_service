pipeline {
  agent any
  options { timestamps() }

  tools {
    jdk 'jdk-21'
    gradle 'gradle-8'
  }

  triggers {
    githubPush()
  }

  environment {
    SERVICE_NAME   = 'support-service'
    CONTAINER_NAME = 'consort-support-service'
    DOCKER_NETWORK = 'consort-infra_consort-network'
    LOGS_DIR       = '/app/logs/support-service'
    ENV_FILE       = '/var/jenkins_home/.env'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        script {
          env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
          env.IMAGE_TAG    = "${env.SERVICE_NAME}:${env.GIT_COMMIT_SHORT}"
          env.IMAGE_LATEST = "${env.SERVICE_NAME}:latest"
        }
      }
    }

    stage('Load .env') {
      steps {
        script {
          def rd = { k -> sh(script: "grep -E '^${k}=' ${env.ENV_FILE} | head -n1 | cut -d= -f2- || true", returnStdout: true).trim() }
          env.POSTGRES_DB       = rd('POSTGRES_DB')       ?: 'consort_group'
          env.POSTGRES_USER     = rd('POSTGRES_USER')     ?: 'consort'
          env.POSTGRES_PASSWORD = rd('POSTGRES_PASSWORD') ?: ''
          env.DB_USERNAME       = rd('DB_USERNAME')       ?: env.POSTGRES_USER
          env.DB_PASSWORD       = rd('DB_PASSWORD')       ?: env.POSTGRES_PASSWORD
          env.SECURITY_TOKEN    = rd('SECURITY_TOKEN')    ?: ''
        }
      }
    }

    stage('Build core_api_dto to local maven') {
      steps {
        dir("${env.WORKSPACE}@core-dto") {
          sh '''
            set -e
            find . -mindepth 1 -maxdepth 1 -exec rm -rf {} + || true
            git clone https://github.com/Consort-Group-Corp/core_api_dto.git .
            chmod +x gradlew
            ./gradlew --no-daemon publishToMavenLocal
          '''
        }
      }
    }

    stage('Gradle build') {
      steps {
        sh '''
          set -e
          chmod +x gradlew
          ./gradlew --no-daemon clean build -x test
        '''
      }
    }

    stage('Archive JAR') {
      steps {
        archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true, allowEmptyArchive: true
      }
    }

    stage('Docker build') {
      steps {
        sh '''
          set -e
          docker build -t ${IMAGE_TAG} -t ${IMAGE_LATEST} .
        '''
      }
    }

    stage('Deploy') {
      steps {
        sh '''
          set -e

          APP_PORT=8089
          HEALTH_URL="http://${CONTAINER_NAME}:${APP_PORT}/actuator/health"

          mkdir -p ${LOGS_DIR} || true

          docker stop ${CONTAINER_NAME} || true
          docker rm ${CONTAINER_NAME} || true

          docker run -d \
            --name ${CONTAINER_NAME} \
            --network ${DOCKER_NETWORK} \
            --restart unless-stopped \
            -p ${APP_PORT}:${APP_PORT} \
            -v ${LOGS_DIR}:/app/logs/support-service \
            --env-file ${ENV_FILE} \
            -e TZ=Asia/Tashkent \
            -e SPRING_PROFILES_ACTIVE=dev \
            -e SERVER_PORT=${APP_PORT} \
            -e SPRING_DATASOURCE_URL=jdbc:postgresql://consort-postgres:5432/${POSTGRES_DB} \
            -e SPRING_DATASOURCE_USERNAME=${POSTGRES_USER} \
            -e SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD} \
            -e SPRING_DATA_REDIS_HOST=consort-redis-support-service \
            -e SPRING_DATA_REDIS_PORT=6379 \
            -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://consort-eureka-service:8762/eureka/ \
            -e SECURITY_TOKEN=${SECURITY_TOKEN} \
            ${IMAGE_TAG}

          echo "⌛ Ожидаю readiness ${HEALTH_URL} ..."
          timeout=120
          until curl -sf "${HEALTH_URL}" | grep -q '"status" *: *"UP"'; do
            sleep 3
            timeout=$((timeout-3))
            if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
              echo "❌ Контейнер ${CONTAINER_NAME} не запущен"
              docker logs --tail=200 ${CONTAINER_NAME} || true
              exit 1
            fi
            if [ $timeout -le 0 ]; then
              echo "❌ Healthcheck не дождались UP за 120с"
              curl -s "${HEALTH_URL}" || true
              docker logs --tail=300 ${CONTAINER_NAME} || true
              exit 1
            fi
          done

          echo "✅ Deployed & healthy: ${CONTAINER_NAME} (${IMAGE_TAG})"
        '''
      }
    }
  }

  post {
    success {
      echo "✅ Build & deploy success: ${env.IMAGE_TAG}"
    }
    failure {
      echo '❌ Pipeline failed — cleaning up...'
      sh '''
        docker logs --tail=200 ${CONTAINER_NAME} || true
        docker stop ${CONTAINER_NAME} || true
        docker rm ${CONTAINER_NAME} || true
        docker rmi ${IMAGE_TAG} || true
      '''
    }
    cleanup {
      cleanWs(deleteDirs: true, disableDeferredWipeout: true, notFailBuild: true)
    }
  }
}
