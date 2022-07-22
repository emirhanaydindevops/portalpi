pipeline {

  environment {
	def scannerHome = tool 'SonarQubeScanner-4.7'
  }

  agent any

  stages {
        stage('SonarQube Code Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=emor -Dsonar.projectName=emor -Dsonar.projectVersion=1.0"
                }
            }
        }
    }
}
