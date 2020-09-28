#!/usr/bin/env groovy
// Copyright (c) 2019 FINE Design Group, Inc.
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT


def call(Map config) {
  try {
    stage('Test') {
      milestone label: 'Test'
      def test_framework = sh returnStdout: true, script: '''if [ -d "test" ]; then
          echo \'test\'
          elif [ -d "spec" ]; then
          echo \'spec\'
          else
          echo \'idk\'
          fi'''
      if(test_framework == 'idk') {
          error '==== Unsupported testing framework! ===='
          currentBuild.result = 'FAILURE'
          
      }
      else {
        sh "${config.container} rake ${test_framework}"
      }
      junit allowEmptyResults: true, keepLongStdio: true, testResults: 'testresults/*.xml'
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
