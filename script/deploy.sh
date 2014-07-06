#!/bin/bash

if [ "$TRAVIS_BRANCH" == "master" ]; then
  cd $HOME
  git clone --quiet https://${GH_TOKEN}@github.com/gsnewmark/munchkin-toolbox > /dev/null

  cd munchkin-toolbox
  lein2 with-profile prod do clean, compile

  git checkout gh-pages
  cp target/cljsbuild/public/js/munchkin-toolbox.js js/munchkin-toolbox.js
  git add js/munchkin-toolbox.js
  git commit -m 'Deploy to Github Pages'

  git remote set-url origin https://${GH_TOKEN}@github.com/gsnewmark/munchkin-toolbox.git
  git push -fq origin gh-pages > /dev/null 2>&1
fi
