#!/usr/bin/env groovy

def call(Closure body){
  nodejs(nodeJSInstallationName: config.NODE_INSTALL_NAME) {
      body()
    }
  }
}
