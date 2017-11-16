#!/usr/bin/env groovy

def call(Map config) {
  try {
    stage('Install Dependancies') {
      milestone label: 'Install Dependancies'
      retry(2) {
        railsRvm('bundle install')
      }
      currentBuild.result = 'SUCCESS'
    }
  } catch(Exception e) {
    currentBuild.result = 'FAILURE'
    sql connection: 'test_db', sql: "DROP DATABASE IF EXISTS ${env.MYSQL_DATABASE};"
    if (config.DEBUG == 'false') {
      railsSlack(config.SLACK_CHANNEL)
    }
    throw e
  }
}