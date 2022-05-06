pipeline {
    agent any

    
}
    
    stages {
        stage('Git Clone') {
            steps {
              echo 'git bağlantısı yapılıyor'
              git credentialsId: 'GIT_CREDENTIALS', url: 'https://github.com/emirhanaydindevops/portalpi.git' 
              echo 'git bağlantısı yapıldı'
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
