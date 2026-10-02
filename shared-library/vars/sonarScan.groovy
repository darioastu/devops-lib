// vars/sonarScan.groovy
def call(Map cfg = [:]) {
  def scannerHome = tool(cfg.scannerTool ?: 'sonar-scanner')
  withSonarQubeEnv(cfg.server ?: 'sonarqube') {
    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectVersion=${env.BUILD_NUMBER}"
  }
}
