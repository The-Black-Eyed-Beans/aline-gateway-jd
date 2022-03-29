def gv

pipeline {
  agent {
    node {
      label "worker-one"
    }
  }

  tools {
    maven 'Maven'
  }

  parameters {
    booleanParam(name: "IS_CLEANWORKSPACE", defaultValue: "true", description: "Set to false to disable folder cleanup, default true.")
    booleanParam(name: "IS_DEPLOYING", defaultValue: "true", description: "Set to false to skip deployment, default true.")
    booleanParam(name: "IS_TESTING", defaultValue: "true", description: "Set to false to disable testing, default true.")
  }

  environment {
    AWS_ACCOUNT_ID = credentials("AWS-ACCOUNT-ID")
    AWS_PROFILE = credentials("AWS_PROFILE")
    DOCKER_IMAGE = "gateway"
    ECR_REGION = credentials("AWS_REGION")
  }

  stages {
    stage("init") {
      steps {
        script {
          gv = load "script.groovy"
        }
      }
    }
    stage("Test") {
      steps {
        script {
          gv.testApp()
        }
      } 
    }   
    stage("Build") {
      steps {
        script {
          gv.buildApp()
        }
      }
    } 
    stage("Upstream Artifact to ECR") {
      steps {
        script {
          gv.upstreamToECR()
        }
      }
    }
    stage("Fetch Environment Variables"){
      steps {
        sh "aws lambda invoke --function-name getGatewayEnv data.json --profile $AWS_PROFILE"
      }
    }
    stage("Deploy to ECS"){
      environment {
        APP_SERVICE_HOST = "${sh(script: """cat data.json | jq -r '.["body"]["APP_SERVICE_HOST"]'""", returnStdout: true).trim()}"
        CLUSTER = "${sh(script: """cat data.json | jq -r '.["body"]["CLUSTER"]'""", returnStdout: true).trim()}"
        LOAD_BALANCER = "${sh(script: """cat data.json | jq -r '.["body"]["LOAD_BALANCER"]'""", returnStdout: true).trim()}"
        SG_PUBLIC = "${sh(script: """cat data.json | jq -r '.["body"]["SG_PUBLIC"]'""", returnStdout: true).trim()}"
        SSL_CERT = "${sh(script: """cat data.json | jq -r '.["body"]["SSL_CERT"]'""", returnStdout: true).trim()}"
        SUBNET_ONE = "${sh(script: """cat data.json | jq -r '.["body"]["SUBNET_ONE"]'""", returnStdout: true).trim()}"
        SUBNET_TWO = "${sh(script: """cat data.json | jq -r '.["body"]["SUBNET_TWO"]'""", returnStdout: true).trim()}"
        VPC = "${sh(script: """cat data.json | jq -r '.["body"]["VPC"]'""", returnStdout: true).trim()}"
      }
      steps {
        sh "docker context use prod-jd"
        sh "docker compose -p $DOCKER_IMAGE-jd up -d"
      }
    }
  }
  post {
    cleanup {
      script {
          gv.postCleanup()
        }
    }
  }
}