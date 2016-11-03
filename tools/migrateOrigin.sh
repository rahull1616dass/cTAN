#!/bin/sh
#Get URL of current origin
REMOTE_LINE=$(git remote -v | grep origin | grep push)
REMOTE_LINE=$(sed 's/^.*\(git@.*\.git\).*$/\1/' <<< $REMOTE_LINE)
#echo $REMOTE_LINE
#Replace 'win9148.informatik.uni-wuerzburg.de' with gitlab2.informatik.uni-wuerzburg.de
#'.'s have to be escaped
REMOTE=$(sed 's/win9148\.informatik\.uni-wuerzburg\.de/gitlab2\.informatik\.uni-wuerzburg\.de/' <<< $REMOTE_LINE)
#echo $REMOTE
git remote set-url origin $REMOTE