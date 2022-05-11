pipeline {
    agent any

    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
                    git branch: 'main', credentialsId: 'GIT_HUB_CREDENTIALS', url: 'https://github.com/emirhanaydindevops/portalpi'
            }
        }
    }
}
