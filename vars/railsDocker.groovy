#!/usr/bin/env groovy

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

  env.MYSQL_HOST = 'db'
  env.RUBY_VERSION_NUM = env.RUBY_VERSION.split('-')[1]
  config.DOCKER_REGISTRY = config.DOCKER_REGISTRY_URL.split('https://')[1]
  env.REPO_NAME = env.JOB_NAME.split('/')[1]

  docker.withRegistry(config.DOCKER_REGISTRY_URL, "ecr:${env.AWS_DEFAULT_REGION}:${config.DOCKER_REGISTRY_CREDS_ID}") {

    docker.image('mysql:5.6').withRun("-v ${WORKSPACE}/mysql:/docker-entrypoint-initdb.d -e MYSQL_HOST=${env.MYSQL_HOST} -e MYSQL_DATABASE=${env.MYSQL_DATABASE} -e MYSQL_USER=${env.MYSQL_USER} -e MYSQL_PASSWORD=${env.MYSQL_PASSWORD} -e MYSQL_ALLOW_EMPTY_PASSWORD=true") { c ->

      docker.image('mysql:5.6').inside("--link ${c.id}:db") {
        sh "while ! mysqladmin ping -h${env.MYSQL_HOST} --silent; do sleep 5; done"
      }

      sshagent([config.SSH_AGENT_ID]) {
        config.container = "docker run -t --rm --name ${env.BUILD_TAG} -w /app -v ${env.SSH_AUTH_SOCK}:${env.SSH_AUTH_SOCK} -v ${env.JEKNINS_HOME}/.ssh:/root/.ssh -v ${env.WORKSPACE}:/app -v ${env.REPO_NAME}_${env.BRANCH_NAME}-gems:/gems -e RAILS_ENV=${env.RAILS_ENV} -e MYSQL_HOST=${env.MYSQL_HOST} -e MYSQL_DATABASE=${env.MYSQL_DATABASE} -e MYSQL_USER=${env.MYSQL_USER} -e MYSQL_PASSWORD=${env.MYSQL_PASSWORD} -e SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK} --link ${c.id}:db ${config.DOCKER_REGISTRY}:${env.RUBY_VERSION_NUM}"

        railsInstallDepsDocker(config)

        if (config.SKIP_TESTS == 'false') {
          railsLoadSchemaDocker(config)
        }

        if (config.SKIP_TESTS == 'false') {
          railsTestsDocker(config)
        }

        railsDeployDocker(config)
      } // SSH agent
    } // MySQL container
  } // withRegistry
} // top level function