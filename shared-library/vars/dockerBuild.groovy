// vars/dockerBuild.groovy
def call(Map cfg = [:]) {
  def file    = cfg.dockerfile ?: 'Dockerfile'
  def context = cfg.context    ?: '.'
  sh """
    docker build -f ${file} \
      --label git.commit=${env.GIT_SHORT ?: 'na'} \
      --label build.number=${env.BUILD_NUMBER} \
      -t ${cfg.image}:${cfg.tag} -t ${cfg.image}:latest ${context}
  """
}
