@NonCPS
def call() {
  if (env.OTHER_DEPLOY_ENVS) {
    for (target_environment in env.OTHER_DEPLOY_ENVS.split(',')){
      rvm("cap ${target_environment.trim()} deploy")
    }
  }
}
