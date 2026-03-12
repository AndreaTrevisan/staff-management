@echo off

set PID_FILE=app.pid

echo =======================================
echo   Staff Management - Application Stop
echo =======================================
echo.

if not exist "%PID_FILE%" (
    echo ERROR: No PID file found.
    echo Application may not be running.
    pause
    exit /b 1
)

set /p PID=<"%PID_FILE%"

echo Stopping application (PID %PID%)...

taskkill /PID %PID% >nul 2>nul

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Could not stop the process.
) else (
    echo Application stopped successfully.
)

del "%PID_FILE%" >nul 2>nul

echo.
pause