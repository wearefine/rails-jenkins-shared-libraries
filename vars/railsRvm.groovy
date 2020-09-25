// Copyright (c) 2019 FINE Design Group, Inc.
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

#!/usr/bin/env groovy

def call(String commands) {
  if(env.BRANCH_NAME == config.MASTER_BRANCH){
    sh "bash -c \"source /usr/local/rvm/scripts/rvm && rvm use --install --create ${env.RUBY_VERSION}@${env.RUBY_GEMSET} && ${commands}\""
  }
  else if(env.BRANCH_NAME == config.DEV_BRANCH || env.BRANCH_NAME == config.STAGE_BRANCH){
    sh "bash -c \"source /usr/local/rvm/scripts/rvm && rvm use --install --create ${env.RUBY_VERSION}@${env.BRANCH_NAME}-${env.RUBY_GEMSET} && ${commands}\""
  }
  else if (env.BRANCH_NAME == /PR-.*/) {
    sh "bash -c \"source /usr/local/rvm/scripts/rvm && rvm use --install --create ${env.RUBY_VERSION}@pr-${env.RUBY_GEMSET} && ${commands}\""
  }
  if (env.DEBUG == 'true') {
    println "*************************"
    println "${env.RUBY_VERSION}"
    println "${env.RUBY_GEMSET}"
    println "${env.BRANCH_NAME}"
    println "RVM Commands: ${commands}"
    println "*************************"
  }
}
