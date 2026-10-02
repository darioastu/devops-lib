package org.devops

// Encapsula el login y nombre de imagen de cada registry soportado
class Registry implements Serializable {
  def steps
  Registry(steps) { this.steps = steps }

  String fullImageName(Map cfg) {
    switch (cfg.registry) {
      case 'dockerhub': return cfg.image                               // usuario/app
      case 'ecr':       return "${cfg.registryHost}/${cfg.image}"      // <acct>.dkr.ecr.<region>.amazonaws.com
      case 'gar':       return "${cfg.registryHost}/${cfg.image}"      // <region>-docker.pkg.dev/<proyecto>/<repo>
      case 'acr':       return "${cfg.registryHost}/${cfg.image}"      // <nombre>.azurecr.io
      default: steps.error("Registry no soportado: ${cfg.registry}")
    }
  }

  void login(Map cfg) {
    switch (cfg.registry) {
      case 'dockerhub':
        steps.withCredentials([steps.usernamePassword(credentialsId: cfg.credentialsId,
            usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
          steps.sh 'echo "$REG_PASS" | docker login -u "$REG_USER" --password-stdin'
        }
        break
      case 'ecr':   // Requiere AWS CLI + plugin "Pipeline: AWS Steps"
        steps.withAWS(credentials: cfg.credentialsId, region: cfg.region) {
          steps.sh "aws ecr get-login-password --region ${cfg.region} | docker login --username AWS --password-stdin ${cfg.registryHost}"
        }
        break
      case 'gar':   // Credencial tipo "Secret file" con el JSON de la service account
        steps.withCredentials([steps.file(credentialsId: cfg.credentialsId, variable: 'GCP_KEY')]) {
          steps.sh "cat \$GCP_KEY | docker login -u _json_key --password-stdin https://${cfg.registryHost.split('/')[0]}"
        }
        break
      case 'acr':   // Service Principal con rol AcrPush
        steps.withCredentials([steps.usernamePassword(credentialsId: cfg.credentialsId,
            usernameVariable: 'SP_ID', passwordVariable: 'SP_SECRET')]) {
          steps.sh "echo \"\$SP_SECRET\" | docker login ${cfg.registryHost} -u \"\$SP_ID\" --password-stdin"
        }
        break
    }
  }
}
