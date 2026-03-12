@echo off
setlocal enabledelayedexpansion

set JAR_NAME=staff-management-0.0.1-SNAPSHOT.jar
set PID_FILE=app.pid

echo =======================================
echo   Staff Management - Application Start
echo =======================================
echo.

:: --------------------------------------------------
:: Prevent multiple instances
:: --------------------------------------------------
if exist "%PID_FILE%" (
    echo ERROR: Application already running.
    echo PID file found: %PID_FILE%
    pause
    exit /b 1
)

:: --------------------------------------------------
:: Java detection
:: --------------------------------------------------
echo Searching for Java runtime...

if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set JAVA_EXE=%JAVA_HOME%\bin\java.exe
        goto java_found
    )
)

where java >nul 2>nul
if %ERRORLEVEL%==0 (
    for /f "delims=" %%i in ('where java') do (
        set JAVA_EXE=%%i
        goto java_found
    )
)

set COUNT=0
for %%d in ("%USERPROFILE%\.jdks" "C:\Program Files\Java" "C:\Program Files (x86)\Java") do (
    if exist %%d (
        for /d %%j in (%%d\*) do (
            if exist "%%j\bin\java.exe" (
                set /a COUNT+=1
                set JDK!COUNT!=%%j
                echo !COUNT!^) %%j
            )
        )
    )
)

if %COUNT%==0 (
    echo.
    echo ERROR: No Java installation found.
    set /p USER_JAVA_HOME=Enter JDK path:
    set USER_JAVA_HOME=%USER_JAVA_HOME:"=%

    if not exist "%USER_JAVA_HOME%\bin\java.exe" (
        echo Invalid JDK path.
        pause
        exit /b 1
    )
    set JAVA_EXE=%USER_JAVA_HOME%\bin\java.exe
    goto java_found
)

if %COUNT%==1 (
    set JAVA_EXE=!JDK1!\bin\java.exe
    goto java_found
)

echo.
set /p CHOICE=Select JDK (1-%COUNT%):

if not defined JDK%CHOICE% (
    echo Invalid selection.
    pause
    exit /b 1
)

set JAVA_EXE=!JDK%CHOICE%!\bin\java.exe

:java_found
echo.
echo Using Java runtime:
echo %JAVA_EXE%
echo.

:: --------------------------------------------------
:: Verify JAR
:: --------------------------------------------------
if not exist "%JAR_NAME%" (
    echo ERROR: Application file not found: %JAR_NAME%
    pause
    exit /b 1
)

:: --------------------------------------------------
:: DB reset option
:: --------------------------------------------------
set /p RESET_DB=Do you want to reset the database? (y/N):

if /i "%RESET_DB%"=="y" (
    set PROFILES=local,firstrun
) else (
    set PROFILES=local
)

echo.
echo Starting application in background...
echo.

:: --------------------------------------------------
:: Start Java hidden using PowerShell (one-liner)
:: --------------------------------------------------
powershell -NoProfile -Command ^
"$proc = Start-Process -FilePath '%JAVA_EXE%' -ArgumentList '-jar','%JAR_NAME%','--spring.profiles.active=%PROFILES%' -WindowStyle Hidden -PassThru; ^
Set-Content -Path '%PID_FILE%' -Value $proc.Id"

:: --------------------------------------------------
:: Show PID
set /p PID=<"%PID_FILE%"
echo Application started successfully.
echo PID: %PID%
echo.

exit /b