# rails-jenkins-shared-libraries

Testing your Rails project on every change should be a smooth process. With Jenkins pipelines you can describe the entire process through code. We did the hard work to make our rails testing and deployment library open source so you can benefit. It gives you a drop in pipeline shared library with a configurable Jenkinsfile.

## Prerequisites

### Rails Project

This pipeline job assumes your project has the following:
- Capistrano based deployments
- Uses a single database the is cleaned up after each build
- Testing is done on a single node without docker

### Jenkins

If you're new to Jenkins pipelines you should go read the [documentation](https://jenkins.io/doc/book/pipeline/) before proceeding to get a sense for what to expect using this code. The rest of the setup process will assume you have basic knowledge of Jenkins or CI/CD jobs in general.

#### OS
  - rvm installed in the jenkins user
  - git
  - build-essential

#### Jenkins
  - Version: > 2.7.3 - tested on (2.19.4 LTS)
  
#### Plugins
  - slack
  - pipeline (workflow-aggregator)
  - git
  - timestamper
  - credentials
  - sshagent
  - junit

#### Scripts Approval
When the job runs the first time you will need to work through allowing certain functions to execute in the groovy sandbox. This is normal as not all high use groovy functions are in the default safelist but more are added all the time.

### Manage with Puppet
The following modules work great to manage a Jenkins instance.

- maestrodev/rvm
- puppetlabs/apache
- rtyler/jenkins

## Jenkinsfile

``` groovy
rails {
  MYSQL_DATABASE = 'db_name'
  MYSQL_USER = 'db_user'
  MYSQL_PASSWORD = 'db_password'
  MYSQL_HOST = 'localhost'
  RUBY_VERSION = 'ruby-x.x.x'
  RUBY_GEMSET = 'gemset_name'
  NODE_INSTALL_NAME = 'lts/boron'
  RAILS_ENV = 'test'
  CAP_VERSION = '3'
  OTHER_DEPLOY_ENVS = 'stage2, qa'
  DEPLOY_VARS = [string(credentialsId: 'secret-text', variable: 'secret-text'), usernameColonPassword(credentialsId: 'git_access', variable: 'git-login-creds')]
  SLACK_CHANNEL = '#deploys'
  DEBUG = 'false'
}
```

### Required Parameters

- **MYSQL_DATABASE:** Name of the test database
- **MYSQL_USER:** Username for the test database
- **MYSQL_PASSWORD:** Password for the test database
- **RUBY_VERSION:** Ruby version to use. [String]
- **RUBY_GEMSET**: Name of the gemset to create. [String]
- **NODE_INSTALL_NAME:** Nodejs plugin uses names to identify installs in Jenkins, enter that value here [String]
- **SSH_AGENT_ID:** Capistrano uses SSH to deploy code so you need to add the sshagent plugin and give the credentialsId here. [String]

### Optional Parameters

- **MYSQL_HOST:** Set the mysql hostname. [String] Default: localhost
- **RAILS_ENV:** Set the rails env. [String] Default: test
- **OTHER_DEPLOY_ENVS:** Other deployment environments you want code pushed to. This should match up the a Capistrano deploy environment. [String (comma delimited)]
- **CAP_VERSION:** Set the version of Capistrano you are using. [String]
- **DEPLOY_VARS:** Credential strings you wish to have set during deployment only. See [Deploy Vars Configuration](#Deploy Vars Configuration) for more details. [List]
- **SLACK_CHANNEL:** Specify the Slack channel to use for notifications. [String] Default: #puppet
- **DEBUG:** Turn off Slack notifications and turn on more console output. [String] Default: false

## Testing Framework Support

Testing supports rspec or minitest based on the test directory name. If it is unknown it will throw an error `==== Unsupported testing framework! ====`

|Dir Name | Test framework | Command |
----------|----------------|---------|
|spec     |rspec           |rake spec|
|test     |minitest        |rake test|

## Deploy Vars Configuration

If you need secure credentials in your deployment steps there is an input parameter that allows for this. 

```groovy
DEPLOY_VARS = [
  string(credentialsId: 'secret-text', variable: 'secret-text'),
  <credential-type>(credentialsId: <cred-id>, variable: <var-name>)
]
```
I went ahead and left the first item in the list the same as above but changed the second to include explanations of the values. You can find these in the pipeline syntax section of a pipeline job. Once there select the `withCredentials` step. Then add the credentials needed for the deploy step and click generate code. It will generate code that looks like the below example.

```groovy
withCredentials([string(credentialsId: 'secret-test-stuff', variable: 'testing'), usernameColonPassword(credentialsId: 'git_basic_access', variable: 'login-creds')]) {
    // some block
}
```

You will notice that inside the `withCredentials` method there is a list of values. This is the list that you need to copy and paste (as is) into the `DEPLOY_VARS` parameter. 


## Test Results
All test results are assumed to be in JUnit format and placed in a single directory named `testresults`.

## [Changelog](CHANGELOG.md)

## [MIT License](LICENSE)

