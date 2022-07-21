pipeline {
    agent any

    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
                    git branch: 'main', credentialsId: 'github', url: 'https://github.com/emirhanaydindevops/portalpi'
            }
        }
    }
}
