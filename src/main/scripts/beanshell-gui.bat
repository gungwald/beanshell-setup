@echo off

rem
rem     Runs the BeanShell interpreter inside the Java Virtual Machine.
rem

setlocal enabledelayedexpansion

set TOP=%~dp0..
set CLASSPATH=%USERPROFILE%\.beanshell\commands\java;%USERPROFILE%\.beanshell\commands\bsh
set CLASSPATH=%TOP%\commands\java;%TOP%\commands\bsh
set CLASSPATH=%CLASSPATH%;%TOP%\local\commands\java;%TOP%\local\commands\bsh
for %%j in ("%TOP%\lib\*.jar") do set CLASSPATH=!CLASSPATH!;%%j
for %%j in ("%TOP%\local\lib\*.jar") do set CLASSPATH=!CLASSPATH!;%%j

if not exist %USERPROFILE%\.beanshell mkdir %USERPROFILE%\.beanshell
echo %CLASSPATH% > "%USERPROFILE%\.beanshell\classpath-log.txt"

javaw bsh.Console %*

endlocal

rem
rem     If this script was started by a double-click then pause so the 
rem     window doesn't disappear.
rem
for /f "tokens=2" %%i in ("%CMDCMDLINE%") do if %%i equ /c pause
