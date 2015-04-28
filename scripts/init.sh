#!/bin/sh

git submodule init
git submodule update
./scripts/gitOnSubs.sh "checkout master"