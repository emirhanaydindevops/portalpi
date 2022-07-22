node('linux') {
    stage('Prepare') {
        deleteDir()
        checkout scm
    }

    stage('Generate') {
        withEnv([
                "PATH+MVN=${tool 'Maven-3.3.9'}/bin",
                "JAVA_HOME=${tool 'jdk11'}",
                "PATH+JAVA=${tool 'jdk11'}/bin"
        ]) {
            sh 'mvn clean -U compile install -DskipTests'
        }
    }

    stage('Archive Test Report') {
        archive 'target/surefire-reports/*-output.txt'
    }
}
