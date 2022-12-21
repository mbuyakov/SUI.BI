pipeline {
  options {
    buildDiscarder logRotator(numToKeepStr: '3')
    disableConcurrentBuilds()
  }

  parameters {
    booleanParam(defaultValue: false, description: 'Clean workspace', name: 'clean_ws')
  }

  environment {
    SUFFIX = "${env.BRANCH_NAME == "master" ? " " : ("-" + env.BRANCH_NAME)}"
  }

  agent any

  stages {
    stage('[Backend] JVM build') {
      agent {
        docker {
          image 'gradle:7.5.1-jdk17'
          reuseNode true
          args '-e HOME=$HOME'
        }
      }
      steps {
        sh """
          cd sui-bi-backend
          gradle --rerun-tasks build
        """
      }
    }

    stage('[Backend] Docker build') {
      steps {
        sh """
          docker build -t nexus.suilib.ru:10402/repository/docker-sui-bi/sui-bi-backend:${BUILD_NUMBER}${SUFFIX} sui-bi-backend
        """
      }
    }

    stage('[Frontend] Yarn build') {
      agent {
        docker {
          image 'node:18-alpine'
          reuseNode true
          args '-e HOME=$HOME'
        }
      }
      steps {
        sh """
          cd sui-bi-frontend
          yarn install --frozen-lockfile
          yarn build
        """
      }
    }

    stage('[Frontend] Docker build') {
      steps {
        sh """
          docker build -t nexus.suilib.ru:10402/repository/docker-sui-bi/sui-bi-frontend:${BUILD_NUMBER}${SUFFIX} sui-bi-frontend
        """
      }
    }

    stage('Publish') {
      environment {
        NEXUS = credentials('suilib-nexus')
      }
      steps {
        sh """
          docker login nexus.suilib.ru:10402/repository/docker-sui-bi/ --username ${NEXUS_USR} --password ${NEXUS_PSW}
          docker push nexus.suilib.ru:10402/repository/docker-sui-bi/sui-bi-backend:${BUILD_NUMBER}${SUFFIX}
          docker push nexus.suilib.ru:10402/repository/docker-sui-bi/sui-bi-frontend:${BUILD_NUMBER}${SUFFIX}
        """
      }
      post {
        always {
          sh """
            docker logout nexus.suilib.ru:10402/repository/docker-sui/
          """
        }
      }
    }

    stage('Clean workspace') {
      when {
        environment name: 'clean_ws', value: 'true'
      }
      steps {
        cleanWs()
      }
    }
  }
}
