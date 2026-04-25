@echo off
REM ============================================================
REM  run.bat — Runs the Interactive Quiz System
REM ============================================================
REM  Usage:
REM    run.bat              (Console mode, TXT file)
REM    run.bat --gui        (Swing GUI)
REM    run.bat --csv        (Console mode, CSV file)
REM    run.bat --gui --csv  (GUI with CSV file)
REM    run.bat --db         (Console mode, SQLite database)
REM ============================================================

if exist "lib\sqlite-jdbc.jar" (
    java -cp "out;lib\sqlite-jdbc.jar" com.quizsystem.Main %*
) else (
    java -cp "out" com.quizsystem.Main %*
)
