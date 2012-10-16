
:: ----------------------------------------------------------------------------
:: Copyright (c) 2012, ABlogiX. All rights reserved.
:: 
:: This file is part of APIWATCH and published under the BSD license.
::
:: Redistribution and use in source and binary forms, with or without modification, 
:: are permitted provided that the following conditions are met:
:: 
:: * Redistributions of source code must retain the above copyright notice, 
::   this list of conditions and the following disclaimer.
:: * Redistributions in binary form must reproduce the above copyright notice, 
::   this list of conditions and the following disclaimer in the documentation 
::   and/or other materials provided with the distribution.
:: 
:: THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
:: ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
:: WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
:: IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
:: INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
:: BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
:: DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
:: LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
:: OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
:: OF THE POSSIBILITY OF SUCH DAMAGE.
:: ----------------------------------------------------------------------------

:: ----------------------------------------------------------------------------
:: APIWATCH scan Start Up Batch script
:: ----------------------------------------------------------------------------

@echo off

set ERROR_CODE=0

:: set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

:: ==== START VALIDATION ====
if defined JAVACMD goto checkJavaCmd
if not "%JAVA_HOME%"=="" goto checkJavaHome
for %%X in (java.exe) do (set FOUND=%%~$PATH:X)
if defined FOUND (
    set JAVACMD="%FOUND%"
    goto checkJavaCmd
)

echo.
echo ERROR: java.exe was not found in the system PATH.
echo.
goto error


:checkJavaHome
if exist "%JAVA_HOME%\bin\java.exe" (
    set JAVACMD="%JAVA_HOME%\bin\java.exe"
    goto checkApiwatchHome
)

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error


:checkJavaCmd
if exist "%JAVACMD%" goto runApiwatch

echo.
echo ERROR: JAVACMD environment variable is set to an invalid path.
echo JAVACMD = "%JAVACMD%"
echo Please set the JAVACMD variable in your environment to match the
echo location of your java.exe executable.
echo.
goto error




:checkApiwatchHome
if not "%APIWATCH_HOME%"=="" goto validateHome

if "%OS%"=="Windows_NT" SET "APIWATCH_HOME=%~dp0.."
if "%OS%"=="WINNT" SET "APIWATCH_HOME=%~dp0.."
if not "%APIWATCH_HOME%"=="" goto validateHome

echo.
echo ERROR: APIWATCH_HOME not found in your environment.
echo Please set the APIWATCH_HOME variable in your environment to match the
echo location of the APIWatch installation
echo.
goto error

:validateHome

:stripHome
if not "_%APIWATCH_HOME:~-1%"=="_\" goto checkApiwatchCmd
set "APIWATCH_HOME=%APIWATCH_HOME:~0,-1%"
goto stripHome

:checkApiwatchCmd
if exist "%APIWATCH_HOME%\bin\%~n0.cmd" goto init

echo.
echo ERROR: APIWATCH_HOME is set to an invalid directory.
echo APIWATCH_HOME = "%APIWATCH_HOME%"
echo Please set the APIWATCH_HOME variable in your environment to match the
echo location of the Maven installation
echo.
goto error
:: ==== END VALIDATION ====

:init
:: Decide how to startup depending on the version of windows

:: -- Windows NT with Novell Login
if "%OS%"=="WINNT" goto WinNTNovell

:: -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

:WinNTNovell

:: -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTArgs

:: -- Regular WinNT shell
set APIWATCH_CMD_LINE_ARGS=%*
goto endInit

:: The 4NT Shell from jp software
:4NTArgs
set APIWATCH_CMD_LINE_ARGS=%$
goto endInit

:Win9xArg
:: Slurp the command line arguments.  This loop allows for an unlimited number
:: of agruments (up to the command line limit, anyway).
set APIWATCH_CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto endInit
set APIWATCH_CMD_LINE_ARGS=%APIWATCH_CMD_LINE_ARGS% %1
shift
goto Win9xApp

:: Reaching here means variables are defined and arguments have been captured
:endInit
SET APIWATCH_JAVA_EXE="%JAVA_HOME%\bin\java.exe"

:: -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTCWJars

REM needed to overcome weird loop behavior
REM in conjunction with variable expansion
SETLOCAL enabledelayedexpansion

set CLASSPATH=.

:: -- Regular WinNT shell
for %%i in ("%APIWATCH_HOME%"\lib\*) do (
    set CLASSPATH=!CLASSPATH!;%%i%
)
goto runApiwatch

:: The 4NT Shell from jp software
:4NTCWJars
for %%i in ("%APIWATCH_HOME%\lib\*") do (
    set CLASSPATH=!CLASSPATH!;%%i%
)
goto runApiwatch



:runApiwatch
set MAINCLASS=org.apiwatch.cli.APIWatch
:: Choose main class
if "%0" == "apiscan.cmd" (
    set MAINCLASS=org.apiwatch.cli.APIScan
)
if "%0" == "apidiff.cmd" (
    set MAINCLASS=org.apiwatch.cli.APIDiff
)
if "%0" == "apiwatch.cmd" (
    set MAINCLASS=org.apiwatch.cli.APIWatch
)

:: Start APIWATCH2
%APIWATCH_JAVA_EXE% %APIWATCH_OPTS% -classpath "%CLASSPATH%" %MAINCLASS% %APIWATCH_CMD_LINE_ARGS%
if NOT ERRORLEVEL 0 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
set ERROR_CODE=1

:end
:: set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT
if "%OS%"=="WINNT" goto endNT

:: For old DOS remove the set variables from ENV - we assume they were not set
:: before we started - at least we don't leave any baggage around
set APIWATCH_JAVA_EXE=
set APIWATCH_CMD_LINE_ARGS=
goto postExec

:endNT
@endlocal & set ERROR_CODE=%ERROR_CODE%

:postExec
cmd /C exit /B %ERROR_CODE%

