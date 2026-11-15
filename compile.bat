@echo off
echo ============================================================
echo   POOLWISE — Compiling all sources...
echo ============================================================

set JAVAC="C:\Program Files\Java\jdk-27.0.1.0.3\bin\javac.exe"
set OUT=out
set SRC=src\main\java

if not exist %OUT% mkdir %OUT%

%JAVAC% -d %OUT% ^
  %SRC%\com\poolwise\exception\*.java ^
  %SRC%\com\poolwise\model\*.java ^
  %SRC%\com\poolwise\algorithm\*.java ^
  %SRC%\com\poolwise\service\*.java ^
  %SRC%\com\poolwise\data\*.java ^
  %SRC%\com\poolwise\util\*.java ^
  %SRC%\com\poolwise\ui\*.java ^
  %SRC%\com\poolwise\Main.java

if %ERRORLEVEL% == 0 (
    echo.
    echo   BUILD SUCCESSFUL!  Run  run.bat  to launch the app.
    echo.
) else (
    echo.
    echo   BUILD FAILED — see errors above.
)
pause
