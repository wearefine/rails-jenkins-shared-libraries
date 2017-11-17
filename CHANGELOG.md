# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased] - TBD

## [v1.1.1] - 11-17-2017
### Fixed
- MySQL query was trying to remove a user that wasn't there during the setup process

## [v1.1.0] - 11-16-2017
### Added
- SKIP_TESTS parameter so you can just deploy code
- More debug output in railsRvm function

### Fixed
- ruby_string being undeclared
- DEBUG not setting the env var when passing in true

### Updated
- railsinstallDeps is now its own function
- railsDeploy is now its own function
- SQL commands now clean old DBs, and Users before adding new ones at the start of the build

## [v1.0.0]

- EVERYTHING :tada:
