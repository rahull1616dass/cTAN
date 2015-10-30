#!/bin/bash

#path,gitLab_project_name
MODULES=(
core,core 
components/ontology/generating,generating
components/editor,editor
components/sound/lwjgl-sound,lwjgl-sound
components/physics/jbullet,jbullet
components/renderer/jvr,jvr
components/io/vrpn,vrpn
applications/examples/basic,examples-basic
)

simxsource=$1

echo Close SmartGit
read -p "Press any key to continue ..."

for m in ${MODULES[@]} ; do 
	IFS=","; set $m; 
 	echo Processing $1 
  	cd $1
 	git checkout github-release
 	echo Removing $1
 	rm -rf ./*
 	echo Copying from $simxsource/$1/
 	cp -R $simxsource/$1/* .
 	git add .
	cd -
done

# for m in ${MODULES[@]} ; do 
# 	IFS=","; set $m; 
#  	echo Processing $1 
#   	cd $1
#  	git checkout github-release
# 	git remote add hci git@win9148.informatik.uni-wuerzburg.de:simx/$2.git
# 	git fetch hci master
# 	git merge --squash hci/master 	
# 	git checkout --theirs -- .
# 	conflicts=`git diff --name-only --diff-filter=U`
# 	#Split git result by newline
# 	while read -r c; do
#    		git add --force -- $c
# 	done <<< $conflicts
#  	git remote rm hci
# 	cd -
# done

cp $simxsource/components/io/InputOutput.owl components/renderer/InputOutput.owl
cp $simxsource/components/physics/SimxPhysics.owl components/physics/SimxPhysics.owl
cp $simxsource/components/renderer/SimxRenderer.owl components/renderer/SimxRenderer.owl
cp $simxsource/components/sound/SimxSound.owl components/sound/SimxSound.owl

cp $simxsource/project/build.properties project/build.properties
cp $simxsource/project/plugins.sbt project/plugins.sbt
cp $simxsource/project/SimXBuildBase.scala project/SimXBuildBase.scala
cp $simxsource/project/SimXSettings.scala project/SimXSettings.scala

rm -rf doc
cp -R $simxsource/doc .

echo "###################################################"
echo "Auto-update done, manual steps required:"
echo "----------------------------------------"
echo "Resolve versions section in project/SimXProductionBuild.scala manually"
echo "Check files (large files, licences, ...)"
echo "Check if running (e.g. examples-basic dependencies, ontology files)"
echo "Push"
