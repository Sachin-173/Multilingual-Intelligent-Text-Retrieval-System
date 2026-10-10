@echo off
title Multilingual Text Retrieval System - Algorithm Test Suite
cd /d "%~dp0"

echo ======================================================================
echo   Running Algorithm Test Suite (42 Unit Tests)
echo ======================================================================
echo.

if not exist bin (
    mkdir bin
    javac -encoding UTF-8 -d bin src\*.java src\data\*.java src\datastructures\*.java src\graph\*.java src\model\*.java src\randomized\*.java src\ranking\*.java src\search\*.java src\server\*.java src\similarity\*.java src\sorting\*.java src\suffix\*.java src\test\*.java src\ui\*.java
)

java "-Dfile.encoding=UTF-8" -cp bin Main --test
pause
