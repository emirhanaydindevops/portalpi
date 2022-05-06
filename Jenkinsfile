pipeline {
    agent any

    
    
    stage("Git Clone"){
        echo 'git bağlantısı yapılıyor'
        git credentialsId: 'GIT_CREDENTIALS', url: 'https://github.com/emirhanaydindevops/portalpi.git' 
        echo 'git bağlantısı yapıldı'
    }

    
    
    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
            }
        }
        stage('Buid') {
            steps {
                echo 'Building'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing'
            }
        }
        stage('Release') {
            steps {
                echo 'Releasing'
            }
        }
    }
}
