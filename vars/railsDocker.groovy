#!/usr/bin/env groovy
// Copyright (c) 2019 FINE Design Group, Inc.
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT


def call(Map config) {
  if (!config.DOCKER_REGISTRY_CREDS_ID) {
    error 'DOCKER_REGISTRY_CREDS_ID is required to use Docker builds'
  }
  if (!config.DOCKER_REGISTRY_URL){
    error 'DOCKER_REGISTRY_URL is required to use Docker builds'
  }
  if (!config.AWS_DEFAULT_REGION){
    env.AWS_DEFAULT_REGION = 'us-west-2'
  } else {
    env.AWS_DEFAULT_REGION = config.AWS_DEFAULT_REGION
  }

  env.BRANCH_NAME = env.BRANCH_NAME.split('-')[0].toLowerCase()
  echo "BRANCH_NAME: ${env.BRANCH_NAME}"
  def user_length = "${env.MYSQL_USER}_${env.BRANCH_NAME}".length()
  def user_name = "${env.BRANCH_NAME}_${env.MYSQL_USER}"
  def db_name = "${env.MYSQL_DATABASE}_${env.BRANCH_NAME}".toLowerCase()
  env.MYSQL_USER = user_name

  if(user_length >= 16) {
    def trimmed_username = user_name[0..15]
    env.MYSQL_USER = trimmed_username
  }
  env.MYSQL_DATABASE = db_name

  env.MYSQL_HOST = 'db'
  env.RUBY_VERSION_NUM = env.RUBY_VERSION.split('ruby-')[1]
  config.DOCKER_REGISTRY = config.DOCKER_REGISTRY_URL.split('https://')[1]
  env.REPO_NAME = env.JOB_NAME.split('/')[1]
  env.GEM_VOLUME = "${env.REPO_NAME}_${env.BRANCH_NAME}_gems"
  
  docker.withRegistry(config.DOCKER_REGISTRY_URL, "ecr:${env.AWS_DEFAULT_REGION}:${config.DOCKER_REGISTRY_CREDS_ID}") {

    docker.image('mysql:5.6').withRun("-v ${WORKSPACE}/mysql:/docker-entrypoint-initdb.d -e MYSQL_HOST=${env.MYSQL_HOST} -e MYSQL_DATABASE=${env.MYSQL_DATABASE} -e MYSQL_USER=${env.MYSQL_USER} -e MYSQL_PASSWORD=${env.MYSQL_PASSWORD} -e MYSQL_ALLOW_EMPTY_PASSWORD=true") { c ->

      docker.image('mysql:5.6').inside("--link ${c.id}:db") {
        sh "while ! mysqladmin ping -h${env.MYSQL_HOST} --silent; do sleep 5; done"
      }

      sshagent([config.SSH_AGENT_ID]) {
        config.container = "docker run -t --rm --name ${env.BUILD_TAG} -w /app -v ${env.SSH_AUTH_SOCK}:${env.SSH_AUTH_SOCK} -v ${env.JENKINS_HOME}/.ssh:/root/.ssh -v ${env.WORKSPACE}:/app -v ${env.GEM_VOLUME}:/gems -e RAILS_ENV=${env.RAILS_ENV} -e MYSQL_HOST=${env.MYSQL_HOST} -e MYSQL_DATABASE=${env.MYSQL_DATABASE} -e MYSQL_USER=${env.MYSQL_USER} -e MYSQL_PASSWORD=${env.MYSQL_PASSWORD} -e SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK} --link ${c.id}:db ${config.DOCKER_REGISTRY}:${env.RUBY_VERSION_NUM}"

        railsInstallDepsDocker(config)

        if (config.SKIP_TESTS == 'false') {
          railsLoadSchemaDocker(config)
        }

        if (config.SKIP_TESTS == 'false') {
          railsTestsDocker(config)
        }

        if (config.SKIP_DEPLOY == 'false') {
          railsDeployDocker(config)
        }

        sh "${config.container} chown -R ${config.JENKINS_UID}:${config.JENKINS_GID} /app"
      } // SSH agent
    } // MySQL container
  } // withRegistry
} // top level function
