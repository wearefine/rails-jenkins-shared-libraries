// Copyright (c) 2019 FINE Design Group, Inc.
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

#!/usr/bin/env groovy

def call(Map config) {
  try {
    stage('Deploy') {
      milestone label: 'Deploy'
      if (config.DEPLOY_VARS) {
        withCredentials(config.DEPLOY_VARS) {
          if (config.CAP_VERSION == '3'){
            if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == config.MASTER_BRANCH) {
              sh "${config.container} cap prod deploy"
            }
            else if(env.BRANCH_NAME == 'stage' || env.BRANCH_NAME == config.STAGE_BRANCH) {
              sh "${config.container} cap stage deploy"
            }
            else if(env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == config.DEV_BRANCH) {
              sh "${config.container} cap dev deploy"
            }
            railsOtherBuildEnvsDocker(config)
          }
          if (config.CAP_VERSION == '2'){
            if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == config.MASTER_BRANCH) {
              sh "${config.container} cap deploy -S loc=prod"
            }
            else if(env.BRANCH_NAME == 'stage' || env.BRANCH_NAME == config.STAGE_BRANCH) {
              sh "${config.container} cap deploy -S loc=stage -S branch=stage"
            }
            else if(env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == config.DEV_BRANCH) {
              sh "${config.container} cap deploy -S loc=dev -S branch=dev"
            }
            railsOtherBuildEnvsDocker(config)
          }
        }
      }
      else {
        if (config.CAP_VERSION == '3'){
          if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == config.MASTER_BRANCH) {
            sh "${config.container} cap prod deploy"
          }
          else if(env.BRANCH_NAME == 'stage' || env.BRANCH_NAME == config.STAGE_BRANCH) {
            sh "${config.container} cap stage deploy"
          }
          else if(env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == config.DEV_BRANCH) {
            sh "${config.container} cap dev deploy"
          }
          railsOtherBuildEnvsDocker(config)
        }
        if (config.CAP_VERSION == '2'){
          if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == config.MASTER_BRANCH) {
            sh "${config.container} cap deploy -S loc=prod"
          }
          else if(env.BRANCH_NAME == 'stage' || env.BRANCH_NAME == config.STAGE_BRANCH) {
            sh "${config.container} cap deploy -S loc=stage -S branch=stage"
          }
          else if(env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == config.DEV_BRANCH) {
            sh "${config.container} cap deploy -S loc=dev -S branch=dev"
          }
          railsOtherBuildEnvsDocker(config)
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
