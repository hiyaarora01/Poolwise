@echo off
setlocal

echo ============================================================
echo   POOLWISE - Compiling Java sources...
echo ============================================================

set "OUT=out"
set "MAIN=src\main\java\com\poolwise\Main.java"

if not exist "%MAIN%" (
    echo ERROR: Main source file not found: %MAIN%
    echo Run this script from the POOLWISE project root.
    pause
    exit /b 1
)

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JAVAC=%JAVA_HOME%\bin\javac.exe"
    )
)

if not defined JAVAC set "JAVAC=javac"

where "%JAVAC%" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java compiler javac was not found.
    echo Install a JDK 21 and add its bin folder to PATH,
    echo or set JAVA_HOME to your JDK 21 installation folder.
    pause
    exit /b 1
)

if not exist "%OUT%" mkdir "%OUT%"

"%JAVAC%" --release 21 -encoding UTF-8 -sourcepath src\main\java -d "%OUT%" "%MAIN%"
if errorlevel 1 (
    echo.
    echo BUILD FAILED. Fix the compiler errors shown above.
    pause
    exit /b 1
)

echo.
echo BUILD SUCCESSFUL.
echo Run run.bat to launch POOLWISE.
echo.
pause
endlocal
