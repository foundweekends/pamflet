#!/bin/bash -e

if [[ "${TRAVIS_PULL_REQUEST}" == "false" && "${TRAVIS_BRANCH}" == "0.7.0" && "${TRAVIS_REPO_SLUG}" == "foundweekends/pamflet" ]]; then
  echo -e "Host github.com\n\tStrictHostKeyChecking no\nIdentityFile ~/.ssh/deploy.key\n" >> ~/.ssh/config
  openssl aes-256-cbc -k "$SERVER_KEY" -in deploy_key.enc -d -a -out deploy.key
  cp deploy.key ~/.ssh/
  chmod 600 ~/.ssh/deploy.key
  git config --global user.email "6b656e6a69@gmail.com"
  git config --global user.name "xuwei-k"
  # https://github.com/sbt/sbt-ghpages/commit/cbedd8edb8
  export SBT_GHPAGES_COMMIT_MESSAGE="auto commit on travis https://github.com/foundweekends/pamflet/commit/${TRAVIS_COMMIT} ${TRAVIS_JOB_NUMBER}"
  sbt ghpagesPushSite
fi
