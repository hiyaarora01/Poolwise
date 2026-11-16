@echo off
setlocal

if not exist "out\com\poolwise\Main.class" (
    echo Compiled application not found. Compiling first...
    call compile.bat
    if errorlevel 1 exit /b 1
)

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA=%JAVA_HOME%\bin\java.exe"
    )
)

if not defined JAVA set "JAVA=java"

where "%JAVA%" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java runtime was not found.
    echo Install JDK 21 and add its bin folder to PATH,
    echo or set JAVA_HOME to your JDK 21 installation folder.
    pause
    exit /b 1
)

"%JAVA%" -version
if errorlevel 1 (
    echo ERROR: Java could not start.
    pause
    exit /b 1
)

"%JAVA%" -Xms128m -Xmx512m -cp "out" com.poolwise.Main
if errorlevel 1 (
    echo.
    echo POOLWISE exited with an error.
    pause
    exit /b 1
)

endlocal
