#!/usr/bin/env groovy
// Copyright (c) 2019 FINE Design Group, Inc.
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT


def call(Map config) {
  try {
    if (config.SKIP_MIGRATIONS == 'false') {
      stage('Load Schema') {
        milestone label: 'Load Schema'
        sh "${config.container} rake db:create db:schema:load"
        currentBuild.result = 'SUCCESS'
      }
    }
  } catch(Exception e) {
    sh "${config.container} chown -R ${config.JENKINS_UID}:${config.JENKINS_GID} /app"
    currentBuild.result = 'FAILURE'
    if (config.DEBUG == 'false') {
      railsSlack(config.SLACK_CHANNEL)
    }
    throw e
  }
}
