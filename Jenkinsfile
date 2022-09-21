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

/*
  stages {
    stage("JVM") {
      agent {
        docker {
          image 'gradle:6.7-jdk8'
          reuseNode true
          args '-e HOME=$HOME'
        }
      }
      stages {
        stage("[JVM] Build") {
          steps {
            sh """
              gradle build
            """
          }
        }
        stage("[JVM] Publish") {
          environment {
            NEXUS_PASS = credentials('suilib-nexus-pass')
          }
          steps {
            sh """
              sh ./upload.sh suibi-structured-query-to-sql-converter
            """
          }
        }
      }
    }
*/

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
