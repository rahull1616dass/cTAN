#!/bin/sh

for dir in  `git submodule`; do 
    if [ -e $dir ];then 
	cd $dir
	echo "in `pwd` executing git $1"  
	git $1
	cd - 
    fi  
done
