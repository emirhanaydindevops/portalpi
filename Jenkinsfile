pipeline {
    agent any
    tools{
        jdk 'java11'
    }

    stages {
        stage('Checkout Source') {
          steps {
            git branch: 'main', credentialsId: 'mygithub', url: 'https://github.com/emirhanaydindevops/portalpi.git'
            sh 'mvn -B -DskipTests clean package' 
          }
        }
    }
}
