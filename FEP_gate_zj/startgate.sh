#!/bin/bash

APP_HOME=$(pwd)  #get current dir name
JAVA_HOME="/usr/java/jdk1.6.0_31"
CLASSPATH=.:$JAVA_HOME/lib/tools.jar:  
libs=$(ls $(pwd)/libs)  #get  all lib
for i in $libs
do
	CLASSPATH=$CLASSPATH$APP_HOME/libs/$i:   #loop every lib,add to classpath
done
CLASSPATH=$CLASSPATH$APP_HOME/fep-gate.jar   #add fep-gate.jar to classpath

_RUNJAVA=$JAVA_HOME/bin/java-3090

MAINCLASS=cn.hexing.fk.gate.Application
JAVA_OPT="-Xms256m -Xmx960m"

$_RUNJAVA -version
$_RUNJAVA $JAVA_OPTS -classpath $CLASSPATH $MAINCLASS