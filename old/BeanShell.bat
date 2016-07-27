@echo off
setlocal
set INSTALL_DIR=%~dp0..
set CLASSPATH=%INSTALL_DIR%\lib\bsh-2.1b5.jar;%INSTALL_DIR%\commands
java -cp "%CLASSPATH%" bsh.Interpreter %*
endlocal
pause