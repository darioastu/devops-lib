// vars/getSource.groovy
def call() {
  checkout scm
  env.GIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
  echo "Commit: ${env.GIT_SHORT} | Rama: ${env.BRANCH_NAME ?: env.GIT_BRANCH}"
}
