#!/usr/bin/env groovy

def call(String commands) {
  def String gemset
  if(env.BRANCH_NAME == 'master'){
    sh "bash -c \"source /usr/local/rvm/scripts/rvm && rvm use --install --create ${env.RUBY_VERSION}@${env.RUBY_GEMSET} && ${commands}\""
  }
  else if(env.BRANCH_NAME == 'stage' | env.BRANCH_NAME == 'dev'){
    sh "bash -c \"source /usr/local/rvm/scripts/rvm && rvm use --install --create ${env.RUBY_VERSION}@${env.BRANCH_NAME}-${env.RUBY_GEMSET} && ${commands}\""
  }
  else if(env.BRANCH_NAME == /PR-.*/) {
    sh "bash -c \"source /usr/local/rvm/scripts/rvm && rvm use --install --create ${env.RUBY_VERSION}@pr-${env.RUBY_GEMSET} && ${commands}\""
  }
  if (env.DEBUG == 'true') {
    println "*************************"
    println "${env.RUBY_VERSION}"
    println "${env.RUBY_GEMSET}"
    println "${env.BRANCH_NAME}"
    println "Ruby Gemset: ${gemset}"
    println "RVM Commands: ${commands}"
    println "*************************"
  }
}
