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
        sh 'aws s3 cp s3://beb-bucket-jd/terraform/vpc-output.json vpc-output.json --quiet --profile $AWS_PROFILE'
        sh 'aws s3 cp s3://beb-bucket-jd/terraform/ecs-output.json ecs-output.json --quiet --profile $AWS_PROFILE'
        sh """cat ecs-output.json | jq '.["outputs"]' > ecs.json"""
        sh """cat ecs.json| jq '.["security_groups"]["value"]' | jq 'map({(.name): .id}) | add' > sg.json"""
        sh """cat ecs.json | jq '.["service_secrets"]["value"]' | jq 'map({(.name): .arn}) | add' > secrets.json"""
      }
    }
    stage("Deploy to ECS"){
      environment {
        APP_SERVICE_HOST = credentials("APP_SERVICE_HOST")
        CLUSTER = "${sh(script: """cat ecs.json | jq -r '.["cluster"]["value"]'""", returnStdout: true).trim()}"
        LOAD_BALANCER = "${sh(script: """cat ecs.json | jq -r '.["load_balancer"]["value"]'""", returnStdout: true).trim()}"
        SG_PRIVATE = "${sh(script: """cat sg.json | jq -r '.["private"]'""", returnStdout: true).trim()}"
        SG_PUBLIC = "${sh(script: """cat sg.json | jq -r '.["public"]'""", returnStdout: true).trim()}"
        SSL_CERT = "${sh(script: """cat ecs.json | jq -r '.["ssl_cert"]["value"]'""", returnStdout: true).trim()}"
        SUBNET_ONE = "${sh(script: """cat vpc-output.json | jq -r '.["outputs"]["private_subnets"]["value"][0]'""", returnStdout: true).trim()}"
        SUBNET_TWO = "${sh(script: """cat vpc-output.json | jq -r '.["outputs"]["private_subnets"]["value"][1]'""", returnStdout: true).trim()}"
        VPC = "${sh(script: """cat vpc-output.json | jq -r '.["outputs"]["vpc_id"]["value"]'""", returnStdout: true).trim()}"
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