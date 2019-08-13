// Copyright (c) 2019 FINE Design Group, Inc.
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

#!/usr/bin/env groovy

@NonCPS
def call(Map config) {
  if (env.OTHER_DEPLOY_ENVS) {
    for (target_environment in env.OTHER_DEPLOY_ENVS.split(',')){
      sh "${config.container} cap ${target_environment.trim()} deploy"
    }
  }
}
