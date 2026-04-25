@echo off
REM ============================================================
REM  compile.bat — Compiles the Interactive Quiz System
REM ============================================================
echo.
echo  Compiling Interactive Quiz System...
echo.

REM Create output directory
if not exist "out" mkdir out

REM Find all Java source files
dir /s /b src\*.java > sources.txt
REM Convert backslashes to forward slashes to handle paths with spaces
powershell -Command "(Get-Content sources.txt) -replace '\\', '/' | Set-Content sources.txt"

REM Compile (add sqlite-jdbc JAR to classpath if present)
if exist "lib\sqlite-jdbc.jar" (
    javac -cp "lib\sqlite-jdbc.jar" -d out @sources.txt
) else (
    javac -d out @sources.txt
)

REM Clean up
del sources.txt

if %ERRORLEVEL% == 0 (
    echo  ✓ Compilation successful!
    echo  Run with: run.bat
) else (
    echo  ✗ Compilation failed. Check errors above.
)
echo.
