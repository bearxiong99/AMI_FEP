#!/bin/bash

APP_HOME=$(pwd)  #get current dir name
JAVA_HOME="/usr/java/jdk1.6.0_31"
CLASSPATH=.:$JAVA_HOME/lib/tools.jar:  
libs=$(ls $(pwd)/libs)  #get all lib
for i in $libs
do
	CLASSPATH=$CLASSPATH$APP_HOME/libs/$i:   #loop every lib,add to classpath
done
CLASSPATH=$CLASSPATH$APP_HOME/fep-dr.jar   #add fep-dr.jar to classpath

_RUNJAVA=$JAVA_HOME/bin/java

MAINCLASS=cn.hexing.reread.Application
JAVA_OPTS=-Xms256m -Xmx512m -Dfile.encoding=UTF8 -Duser.timezone=Asia/Shanghai

$_RUNJAVA -version
$_RUNJAVA $JAVA_OPTS -classpath $CLASSPATH $MAINCLASS 

:end

pause