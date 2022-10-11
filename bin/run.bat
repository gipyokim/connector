@echo off

rem %~dp0 is expanded pathname of the current script under NT
set APP_HOME=%~dp0
set APP_HOME=%APP_HOME:\bin\=%

set JAVA_OPTS=-Xms256m -Xmx1024m
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto runIt

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo.

:runIt

set _LIBJARS=%APP_HOME%\lib\*;.
"%_JAVACMD%" -classpath %_LIBJARS% %JAVA_OPTS% "-Dlog4j.configurationFile=%APP_HOME%\conf\log4j2.xml" com.konantech.connector.Main %*
