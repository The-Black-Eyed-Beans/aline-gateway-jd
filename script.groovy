def buildApp() {
  sh "mvn install"
}

def testApp() {
  if (params.IS_TESTING) {
    sh "mvn clean test"
  }
}

def postCleanup() {
  if (params.IS_CLEANWORKSPACE) {
    sh "rm -rf ./*"
    sh "docker context use default"
    sh "docker image prune -af"
  }
}

def upstreamToECR() {
  if (params.IS_DEPLOYING) {
    env.CURRENT_HASH = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
    sh "cp ./target/*.jar ."
    sh "docker context use default"
    sh 'aws ecr get-login-password --region $ECR_REGION --profile joshua | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$ECR_REGION.amazonaws.com'
    sh "docker build -t ${DOCKER_IMAGE} ."
    sh 'docker tag $DOCKER_IMAGE:latest $AWS_ACCOUNT_ID.dkr.ecr.$ECR_REGION.amazonaws.com/$DOCKER_IMAGE-jd:$CURRENT_HASH'
    sh 'docker tag $DOCKER_IMAGE:latest $AWS_ACCOUNT_ID.dkr.ecr.$ECR_REGION.amazonaws.com/$DOCKER_IMAGE-jd:latest'
    sh 'docker push $AWS_ACCOUNT_ID.dkr.ecr.$ECR_REGION.amazonaws.com/$DOCKER_IMAGE-jd:$CURRENT_HASH'
    sh 'docker push $AWS_ACCOUNT_ID.dkr.ecr.$ECR_REGION.amazonaws.com/$DOCKER_IMAGE-jd:latest'
  }
}

return this
