 pipeline {

    agent any

    tools {
        maven "Maven"
    }

    options {
        timestamps()
        ansiColor("xterm")
    }

    stages {

        stage("Verify") {
            steps {
                sh "env"
                sh "mvn clean verify"
            }
        }

    }

    post {
        always {
            deleteDir()
        }
    }
  }
