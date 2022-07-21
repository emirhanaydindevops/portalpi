node {
  stage('Clone the Git') {
    git branch: 'main', credentialsId: 'github', url: 'https://github.com/emirhanaydindevops/portalpi'
  }
  stage('SonarQube analysis') {
    def scannerHome = tool 'SonarQubeScanner-4.7';
    withSonarQubeEnv('sonarqube') {
      sh "${scannerHome}/bin/sonar-scanner \
      -D sonar.login=admin \
      -D sonar.password=sonar \
      -D sonar.projectKey=sonarqubetest \
      -D sonar.exclusions=vendor/**,resources/**,**/*.java \
      -D sonar.host.url=http://192.168.1.39:9000/"
    }
  }
}
