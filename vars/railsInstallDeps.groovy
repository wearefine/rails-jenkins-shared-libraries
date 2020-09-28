#!/usr/bin/env groovy
// Copyright (c) 2019 FINE Design Group, Inc.
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT


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
    if (config.DEBUG == 'false') {
      railsSlack(config.SLACK_CHANNEL)
    }
    throw e
  }
}
