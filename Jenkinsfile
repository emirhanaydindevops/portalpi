pipeline {
    agent any
    tools{
        jdk 'java11'
    }

    stages {
        stage('Checkout Source') {
          steps {
            echo 'Pulling... ' + scm.branches[0].name
            git branch: scm.branches[0].name, credentialsId: 'jenkins-git', url: 'bitbucket.org:canesisdeployment/kargomatik_api.git'
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
                
                    // Set Artifactory repositories for dependencies resolution and artifacts deployment.
                    rtMaven.deployer releaseRepo:'m2-dev', server: server
                    //rtMaven.resolver releaseRepo:'libs-release', snapshotRepo:'libs-snapshot', server: server
                    
                    buildInfo = rtMaven.run pom: 'pom.xml', goals: 'clean spring-boot:build-info install'
                    
                    server.publishBuildInfo buildInfo
                
                    env.version =  version
                 
                }
            }
        }
    }
}
