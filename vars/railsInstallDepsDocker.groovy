// Copyright (c) 2019 FINE Design Group, Inc.
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

#!/usr/bin/env groovy

def call(Map config) {
  try {
    stage('Install Dependancies') {
      milestone label: 'Install Dependancies'
      retry(2) {
        sh "${config.container} bundle install --quiet --clean --force --jobs=4"
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
