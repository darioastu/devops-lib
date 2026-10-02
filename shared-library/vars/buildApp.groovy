// vars/buildApp.groovy
def call(Map cfg = [:]) {
  dir(cfg.dir ?: '.') {
    sh 'npm install'
    sh 'npm run build'
    sh 'npm test'
  }
}
