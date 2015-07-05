#!/bin/bash

if [ "$TRAVIS_BRANCH" == "master" ]; then
  cd $HOME
  git clone --quiet https://${GH_TOKEN}@github.com/gsnewmark/munchkin-toolbox > /dev/null

  cd munchkin-toolbox
  boot build

  git checkout gh-pages
  cp target/js/munchkin-toolbox.js js/munchkin-toolbox.js
  cp target/css/munchkin-toolbox.css css/munchkin-toolbox.css
  git add js/munchkin-toolbox.js css/munchkin-toolbox.css
  git commit -m 'Deploy to Github Pages'

  git remote set-url origin https://${GH_TOKEN}@github.com/gsnewmark/munchkin-toolbox.git
  git push -fq origin gh-pages > /dev/null 2>&1
fi
