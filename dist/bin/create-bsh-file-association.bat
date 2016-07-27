@echo off

setlocal

set TOP=%~dp0..

java -cp "%TOP%\classes" ^
    org.beanshelldoubleclick.install.BeanShellFileAssociator %*

endlocal

rem
rem     If this script was started by a double-click then pause so the 
rem     window doesn't disappear.
rem
for /f "tokens=2" %%i in ("%CMDCMDLINE%") do if %%i equ /c pause
