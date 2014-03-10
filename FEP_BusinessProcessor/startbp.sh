#!/bin/bash

APP_HOME=$(pwd)  #get current dir name
JAVA_HOME="/usr/java/jdk1.6.0_31"
CLASSPATH=.:$JAVA_HOME/lib/tools.jar:  
libs=$(ls $(pwd)/libs)  #get all libs 
for i in $libs
do
	CLASSPATH=$CLASSPATH$APP_HOME/libs/$i:   #loop every lib,add to classpath
done
CLASSPATH=$CLASSPATH$APP_HOME/fep-bp.jar   #add fep-bp.jar to classpath

_RUNJAVA=$JAVA_HOME/bin/java

MAINCLASS=cn.hexing.fk.bp.Application
JAVA_OPTS="-Xms256m -Xmx960m"

$_RUNJAVA -version
$_RUNJAVA $JAVA_OPTS -classpath $CLASSPATH $MAINCLASS