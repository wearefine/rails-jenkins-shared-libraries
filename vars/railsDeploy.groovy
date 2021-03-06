#!/usr/bin/env groovy
// Copyright (c) 2019 FINE Design Group, Inc.
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT


def call(Map config) {
  try {
    stage('Deploy') {
      milestone label: 'Deploy'
      echo "ENV_BRANCH_NAME: ${env.BRANCH_NAME}"
      echo "CONFIG_BRANCH_NAME: ${config.MASTER_BRANCH}"
      sshagent([config.SSH_AGENT_ID]) {
        if (config.DEPLOY_VARS) {
          withCredentials(config.DEPLOY_VARS) {
            if (config.CAP_VERSION == '3'){
              if (env.BRANCH_NAME == config.MASTER_BRANCH) {
                railsRvm('cap prod deploy')
              }
              else if(env.BRANCH_NAME == config.STAGE_BRANCH) {
                railsRvm('cap stage deploy')
              }
              else if(env.BRANCH_NAME == config.DEV_BRANCH) {
                railsRvm('cap dev deploy')
              }
              railsOtherBuildEnvs()
            }
            if (config.CAP_VERSION == '2'){
              if (env.BRANCH_NAME == config.MASTER_BRANCH) {
                railsRvm('cap deploy -S loc=prod')
              }
              else if(env.BRANCH_NAME == config.STAGE_BRANCH) {
                railsRvm('cap deploy -S loc=stage -S branch=stage')
              }
              else if(env.BRANCH_NAME == config.DEV_BRANCH) {
                railsRvm('cap deploy -S loc=dev -S branch=dev')
              }
              railsOtherBuildEnvs()
            }
          }
        }
        else {
          if (config.CAP_VERSION == '3'){
            if (env.BRANCH_NAME == config.MASTER_BRANCH) {
              railsRvm('cap prod deploy')
            }
            else if(env.BRANCH_NAME == config.STAGE_BRANCH) {
              railsRvm('cap stage deploy')
            }
            else if(env.BRANCH_NAME == config.DEV_BRANCH) {
              railsRvm('cap dev deploy')
            }
            railsOtherBuildEnvs()
          }
          if (config.CAP_VERSION == '2'){
            if (env.BRANCH_NAME == config.MASTER_BRANCH) {
              railsRvm('cap deploy -S loc=prod')
            }
            else if(env.BRANCH_NAME == config.STAGE_BRANCH) {
              railsRvm('cap deploy -S loc=stage -S branch=stage')
            }
            else if(env.BRANCH_NAME == config.DEV_BRANCH) {
              railsRvm('cap deploy -S loc=dev -S branch=dev')
            }
            railsOtherBuildEnvs()
          }
        }
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
