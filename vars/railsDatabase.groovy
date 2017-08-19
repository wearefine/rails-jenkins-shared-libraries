#!/usr/bin/env groovy

def call(Closure body){
  getDatabaseConnection(id: 'test_db', type: 'GLOBAL') {
    body()
  }
}
