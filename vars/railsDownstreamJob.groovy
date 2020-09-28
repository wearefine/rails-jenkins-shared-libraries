#!/usr/bin/env groovy
// Copyright (c) 2019 FINE Design Group, Inc.
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT


def call(Map config) {
  try {
    stage('Downstream Job') {
      milestone label: 'Downstream Job'
      if (config.DOWNSTREAM_JOB_PARAMS) {
        build job: config.DOWNSTREAM_JOB_NAME, parameters: config.DOWNSTREAM_JOB_PARAMS,
        propagate: false, wait: false
      } else {
        build job: config.DOWNSTREAM_JOB_NAME, propagate: false, wait: false
      
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
