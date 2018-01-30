#!/usr/bin/env groovy

def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  if (!config.RAILS_ENV) {
    env.RAILS_ENV = "test"
  }
  if (!config.RUBY_VERSION){
    error 'RUBY_VERSION is required'
  } else {
    env.RUBY_VERSION = config.RUBY_VERSION
  }
  if (!config.MYSQL_HOST) {
    env.MYSQL_HOST = "localhost"
  }
  if (!config.MYSQL_DATABASE){
    error 'MYSQL_DATABASE is required'
  } else {
    env.MYSQL_DATABASE = config.MYSQL_DATABASE
  }
  if (!config.MYSQL_USER){
    error 'MYSQL_USER is required'
  } else {
    env.MYSQL_USER = config.MYSQL_USER
  }
  if (!config.MYSQL_PASSWORD){
    error 'MYSQL_PASSWORD is required'
  } else {
    env.MYSQL_PASSWORD = config.MYSQL_PASSWORD
  }
  if (!config.CAP_VERSION) {
    config.CAP_VERSION = '3'
  }
  if (!config.OTHER_DEPLOY_ENVS) {
    config.OTHER_DEPLOY_ENVS = ''
  } else {
    env.OTHER_DEPLOY_ENVS = config.OTHER_DEPLOY_ENVS
  }
  if (!config.SLACK_CHANNEL) {
    config.SLACK_CHANNEL = '#deploys'
  }
  if (!config.DEBUG) {
    config.DEBUG = 'false'
    env.DEBUG = 'false'
  }
  else {
    env.DEBUG = 'true'
  }
  if (!config.SSH_AGENT_ID) {
    error 'SSH_AGENT_ID is required'
  }
  if (!config.SKIP_TESTS){
    config.SKIP_TESTS = 'false'
  } else {
    env.SKIP_TESTS = config.SKIP_TESTS
  }
  if (!config.SKIP_MIGRATIONS){
    config.SKIP_MIGRATIONS = 'false'
  } else {
    env.SKIP_MIGRATIONS = config.SKIP_MIGRATIONS
  }

  node {
    timestamps {
      if (config.DEBUG == 'false') {
        railsSlack(config.SLACK_CHANNEL)
      }

      try {
        stage('Checkout') {
          checkout scm
          currentBuild.result = 'SUCCESS'
        }
      } catch(Exception e) {
        currentBuild.result = 'FAILURE'
        if (config.DEBUG == 'false') {
          railsSlack(config.SLACK_CHANNEL)
        }
        throw e
      }

      def dockerBuild = fileExists 'Dockerfile'
      if (dockerBuild) {
        railsDocker(config)
        if (config.DEBUG == 'false') {
          railsSlack(config.SLACK_CHANNEL)
        }
      } else {

        if (!config.RUBY_GEMSET){
          error 'RUBY_GEMSET is required'
        } else {
          env.RUBY_GEMSET = config.RUBY_GEMSET
        }
        if (!config.NODE_INSTALL_NAME) {
          error 'NODE_INSTALL_NAME is required'
        }

        if (config.SKIP_TESTS == 'false') {
          getDatabaseConnection(id: 'test_db', type: 'GLOBAL') {
            nodejs(nodeJSInstallationName: config.NODE_INSTALL_NAME) {
              if (config.DEBUG == 'true') {
                echo "PATH: ${env.PATH}"
                echo "BRANCH_NAME: ${env.BRANCH_NAME}"
              }

              try {
                stage('Setup Environment') {
                  milestone label: 'Setup Environment'
                  
                  if (config.SKIP_MIGRATIONS == 'false') {
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

                    sql connection: 'test_db', sql: "DROP DATABASE IF EXISTS ${env.MYSQL_DATABASE};"

                    sql connection: 'test_db', sql: "CREATE DATABASE IF NOT EXISTS ${env.MYSQL_DATABASE};"
                    echo "SQL: CREATE DATABASE IF NOT EXISTS ${env.MYSQL_DATABASE};"
                    sql connection: 'test_db', sql: "GRANT ALL ON ${env.MYSQL_DATABASE}.* TO \'${env.MYSQL_USER}\'@\'%\' IDENTIFIED BY \'${env.MYSQL_PASSWORD}\';"
                    echo "SQL: GRANT ALL ON ${env.MYSQL_DATABASE}.* TO \'${env.MYSQL_USER}\'@\'%\' IDENTIFIED BY \'**************\';"
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

              railsInstallDeps(config)
              
              if (config.SKIP_MIGRATIONS == 'false') {
                try {
                  stage('Load Schema') {
                    milestone label: 'Load Schema'
                    retry(2) {
                      railsRvm('rake db:schema:load')
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
                      if (config.DEBUG == 'false') {
                        railsSlack(config.SLACK_CHANNEL)
                      }
                  }
                  else {
                    railsRvm("rake ${test_framework}" )
                  }
                  junit allowEmptyResults: true, keepLongStdio: true, testResults: 'testresults/*.xml'
                  currentBuild.result = 'SUCCESS'
                }
              } catch(Exception e) {
                junit allowEmptyResults: true, keepLongStdio: true, testResults: 'testresults/*.xml'
                currentBuild.result = 'FAILURE'
                if (config.DEBUG == 'false') {
                  railsSlack(config.SLACK_CHANNEL)
                }
                throw e
              }

              railsDeploy(config)

              try {
                stage('Clean Up') {
                  milestone label: 'Clean Up'
                  if (config.SKIP_MIGRATIONS == 'false') {
                    sql connection: 'test_db', sql: "DROP DATABASE IF EXISTS ${env.MYSQL_DATABASE};"
                    echo "SQL: DROP DATABASE IF EXISTS ${env.MYSQL_DATABASE};"
                    sql connection: 'test_db', sql: "REVOKE ALL PRIVILEGES, GRANT OPTION FROM ${env.MYSQL_USER}@'%';"
                    echo "SQL: REVOKE ALL PRIVILEGES, GRANT OPTION FROM ${env.MYSQL_USER}@'%';"
                    sql connection: 'test_db', sql: "DROP USER ${env.MYSQL_USER}@'%';"
                    echo "SQL: DROP USER ${env.MYSQL_USER}@'%';"
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
            } // railsNodejs
          } // railsDatabase
        } // SKIP_TESTS
        else {
          railsInstallDeps(config)
          railsDeploy(config)
        }
        if (config.DEBUG == 'false') {
          railsSlack(config.SLACK_CHANNEL)
        }
      }
      cleanWs notFailBuild: true
    } // timestamps
  } // node
}
