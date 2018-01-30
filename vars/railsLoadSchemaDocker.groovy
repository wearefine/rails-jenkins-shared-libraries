#!/usr/bin/env groovy

def call(Map config) {
  try {
    if (config.SKIP_MIGRATIONS == 'false') {
      stage('Load Schema') {
        milestone label: 'Load Schema'
        sh "${config.container} rake db:schema:load"
        currentBuild.result = 'SUCCESS'
      }
    }
  } catch(Exception e) {
    currentBuild.result = 'FAILURE'
    if (config.DEBUG == 'false') {
      railsSlack(config.SLACK_CHANNEL)
    }
    throw e
  }
}