// vars/dockerPublish.groovy
// Publica la imagen en Docker Hub, ECR, GAR o ACR según cfg.registry
import org.devops.Registry

def call(Map cfg = [:]) {
  def reg      = new Registry(this)
  def fullName = reg.fullImageName(cfg)          // agrega el host del registry si aplica

  sh "docker tag ${cfg.image}:${cfg.tag} ${fullName}:${cfg.tag}"
  sh "docker tag ${cfg.image}:${cfg.tag} ${fullName}:latest"

  reg.login(cfg)
  sh "docker push ${fullName}:${cfg.tag}"
  sh "docker push ${fullName}:latest"
  sh "docker logout ${cfg.registryHost ?: ''} || true"
}
