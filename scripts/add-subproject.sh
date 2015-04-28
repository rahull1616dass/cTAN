#!/bin/sh

servername=trinity.informatik.uni-wuerzburg.de
modulesdir=/var/git/projects/siris-modules/
rootdir=$PWD

if [[ ! $0 == \./scripts/* ]]; then
    echo "error: execute this script from the root directory"
    exit 1
fi

destination=ssh://$servername$modulesdir$1
echo "creating git repository in $destination"
ssh trinity.informatik.uni-wuerzburg.de "mkdir -p $modulesdir$1 && git init --bare --shared=group $modulesdir$1"

echo "adding submodule in $rootdir/$1"
git submodule add $destination $1

echo "adding welcome file and pushing"
cd $1
echo "$1" > modulename
git add modulename
git commit -m "initial commit"

echo "adding $rootdir/$1 to git"
cd $rootdir
git add $1