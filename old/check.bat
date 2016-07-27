@echo off

rem
rem     If this script was started by double-click then pause so the 
rem     window doesn't disappear.
rem
for /f "tokens=2" %%i in ("%CMDCMDLINE%") do if %%i equ /c pause


rem
rem This determines if our script was double-clicked from the UI or
rem if it was typed in from a Command Prompt. If it was double-clicked
rem then we need to add a "pause" command so that the window doesn't
rem just disappear when the script is done. The %CmdCmdLine% variable
rem will contain a /c switch if it was double-clicked, but not if it
rem was typed in from an Command Prompt.
rem

echo %cmdcmdline% | findstr " /c " > nul:

rem
rem An %errorLevel% of 0 indicates that the findstr command successfully
rem found the string it was looking for. An %errorlevel% of 1 would
rem indicate failure.
rem

if %errorlevel% equ 0 pause

rem Alternate method of check which requires one to know the name of
rem this script, which is undesirable because it would have to be
rem changed for every script it is used in.

rem if %0 neq check pause
rem

