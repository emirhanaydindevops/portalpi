pipeline {
    agent any
    tools{
        jdk 'java11'
    }

    stages {
        stage('Checkout Source') {
          steps {
            git branch: 'main', credentialsId: 'mygithub', url: 'https://github.com/emirhanaydindevops/portalpi.git'
          }
        }
    
       pipeline {
    agent {
        docker {
            image 'maven:3.8.1-adoptopenjdk-11' 
            args '-v /root/.m2:/root/.m2' 
        }
    }
    stages {
        stage('Build') { 
            steps {
                sh 'mvn -B -DskipTests clean package' 
            }
        }
    }
}
