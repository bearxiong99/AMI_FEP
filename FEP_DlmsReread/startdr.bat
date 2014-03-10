title br
@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Start script for the FAS Gate Server
rem ---------------------------------------------------------------------------

set JAVA_HOME=D:\jdk1.6.0_17

set CURRENT_DIR=%cd%
set APP_HOME=%CURRENT_DIR%

set LIBS_HOME=%APP_HOME%\libs
:gotJavaHome
set CLASSPATH=.;%JAVA_HOME%\lib\tools.jar

setlocal enabledelayedexpansion
for %%j in (%LIBS_HOME%\*.jar) do (
	set CLASSPATH=!CLASSPATH!;%%j
)
set CLASSPATH=%CLASSPATH%;%APP_HOME%\fep-dr.jar

set _RUNJAVA="%JAVA_HOME%\bin\java"

echo Using APP_HOME=%CURRENT_DIR%
echo Using JAVA_HOME:   %JAVA_HOME%
echo CLASSPATH:   %CLASSPATH%

set MAINCLASS=cn.hexing.reread.Application
set JAVA_OPTS=-Xms256m -Xmx512m  

%_RUNJAVA% -version
%_RUNJAVA% %JAVA_OPTS% -classpath %CLASSPATH% %MAINCLASS% 
endlocal
:end

pause