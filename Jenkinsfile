def gv

pipeline {
  agent any

  tools {
    maven 'Maven'
  }

  parameters {
    booleanParam(name: "IS_CLEANWORKSPACE", defaultValue: "true", description: "Set to false to disable folder cleanup, default true.")
    booleanParam(name: "IS_DEPLOYING", defaultValue: "true", description: "Set to false to skip deployment, default true.")
    booleanParam(name: "IS_TESTING", defaultValue: "false", description: "Set to false to skip testing, default false.")
  }

  environment {
    AWS_ACCOUNT_ID = credentials("AWS-ACCOUNT-ID")
    DOCKER_IMAGE = "gateway"
    ECR_REGION = "us-east-1"
  }

  stages {
    stage("init") {
      steps {
          script {
          gv = load "init.groovy"
        }
      }
    }
    stage("build") {
      steps {
        script {
          gv.buildApp()
        }
      }
    }
    stage("test") {
      steps {
        script {
          gv.testApp()
        }
      } 
    }
    stage('archive') {
      steps {
        archiveArtifacts artifacts: '**/target/*.jar', followSymlinks: false
      }
    }
    stage("deploy") {
      steps {
        script {
          gv.deployToECR()
        }
      }
    }
  }
  post {
    always {
      script {
          gv.postAlways()
        }
    }
  }
}
