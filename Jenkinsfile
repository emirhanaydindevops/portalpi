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
    stages {

        stage('SonarQube Code Analysis') {

            steps {

                withSonarQubeEnv('sonarqube') {

                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=test -Dsonar.projectName=test -Dsonar.projectVersion=1.0"

                }

            }

        }

    }

}  
        
       
        stage("Commit Stage"){
            steps{
                script{
                    def pom = readMavenPom file: 'pom.xml'
                    
                    def version=  "${pom.version}.${currentBuild.number}"
                                   
                    descriptor.version =version
                    
                    descriptor.transform()
    
                    rtMaven.tool = "Maven-3.3.9"
                
                    //rtMaven.resolver releaseRepo:'libs-release', snapshotRepo:'libs-snapshot', server: server
                    
                    buildInfo = rtMaven.run pom: 'pom.xml', goals: 'clean spring-boot:build-info install'
                    
                    server.publishBuildInfo buildInfo
                
                    env.version =  version
                 
                }
            }
        }
    }
}
