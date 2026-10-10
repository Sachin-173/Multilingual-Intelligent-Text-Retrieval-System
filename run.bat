@echo off
title Multilingual Text Retrieval System - Web Server
cd /d "%~dp0"

echo ======================================================================
echo   MULTILINGUAL TEXT RETRIEVAL SYSTEM - Standalone Web Application
echo   Intelligent Multilingual Search with Automatic Algorithm Selection
echo ======================================================================
echo.

if not exist bin (
    mkdir bin
)

echo [1/2] Compiling Java backend with UTF-8 encoding...
javac -encoding UTF-8 -d bin src\*.java src\data\*.java src\datastructures\*.java src\graph\*.java src\model\*.java src\randomized\*.java src\ranking\*.java src\search\*.java src\server\*.java src\similarity\*.java src\sorting\*.java src\suffix\*.java src\test\*.java src\ui\*.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Compilation failed. Please make sure JDK 17+ is installed.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Launching Java REST Web Server and opening browser...
echo Website URL: http://localhost:8080
echo.
java "-Dfile.encoding=UTF-8" -cp bin Main
pause
