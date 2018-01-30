#!/usr/bin/env groovy

def call(Map config) {
  try {
    stage('Install Dependancies') {
      milestone label: 'Install Dependancies'
      retry(2) {
        sh "${config.container} bundle install --quiet --clean --jobs=4"
      }
      currentBuild.result = 'SUCCESS'
    }
  } catch(Exception e) {
    currentBuild.result = 'FAILURE'
    if (config.DEBUG == 'false') {
      railsSlack(config.SLACK_CHANNEL)
    }
    throw e
  }
}