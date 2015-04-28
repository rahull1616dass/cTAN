#!/bin/sh

sha=`git log --pretty=format:%H | tail -1`
git checkout $sha -b clean-master
git merge --squash master
git commit -a
for i in `git diff --name-status $sha..HEAD | grep ^D | sed  -e 's/^D//'`; do 
    git filter-branch --force --index-filter "git rm --cached --ignore-unmatch $i" --prune-empty --tag-name-filter cat -- clean-master
done;
git branch -D master
myremote=`git remote -v show | sed s/\(.*\)//g | head -1`
git remote rm origin
git reflog expire --expire=now --all
git gc --prune=now --aggressive
git branch -m master
git remote add $myremote