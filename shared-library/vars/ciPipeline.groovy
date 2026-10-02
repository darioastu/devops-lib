// vars/ciPipeline.groovy
// Pipeline completo reutilizable. Uso: ciPipeline(imageName: '...', registry: 'dockerhub', ...)
def call(Map cfg = [:]) {
  def appDir        = cfg.appDir        ?: '.'
  def imageName     = cfg.imageName     ?: error('imageName es obligatorio')
  def registry      = cfg.registry      ?: 'dockerhub'
  def credentialsId = cfg.credentialsId ?: 'dockerhub-creds'
  def sonarServer   = cfg.sonarServer   ?: 'sonarqube'

  pipeline {
    agent any
    triggers { githubPush() }                       // Webhook
    tools { nodejs 'node20' }
    options { timestamps(); buildDiscarder(logRotator(numToKeepStr: '10')) }
    environment { IMAGE_TAG = "${env.BUILD_NUMBER}" }

    stages {
      stage('Get Source') {
        steps { getSource() }
      }
      stage('Build & Sonar') {
        parallel {
          stage('Build') { steps { buildApp(dir: appDir) } }
          stage('SonarQube') { steps { sonarScan(server: sonarServer) } }
        }
      }
      stage('Docker Build') {
        steps { dockerBuild(image: imageName, tag: env.IMAGE_TAG) }
      }
      stage('Publish Image') {
        steps {
          dockerPublish(image: imageName, tag: env.IMAGE_TAG,
                        registry: registry, credentialsId: credentialsId,
                        region: cfg.region, registryHost: cfg.registryHost)
        }
      }
    }
    post {
      always  { sh "docker image prune -f || true" }
      success { echo "✅ Imagen publicada: ${imageName}:${env.IMAGE_TAG}" }
      failure { echo '❌ Pipeline falló' }
    }
  }
}
