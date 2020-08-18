 pipeline {

    agent {
      docker { 
        image 'maven:3.6.3-openjdk-14-slim'
        args '-v $HOME/.m2:/root/.m2'
      }
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
