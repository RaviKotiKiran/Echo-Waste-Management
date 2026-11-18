@echo off
setlocal
cd /d "%~dp0backend"

if not exist mvnw.cmd (
  echo ERROR: backend/mvnw.cmd was not found.
  echo This launcher should start inside the backend folder.
  pause
  exit /b 1
)

if not defined SPRING_PROFILES_ACTIVE set SPRING_PROFILES_ACTIVE=mysql
echo Starting Echo backend from: %CD%
echo Profile: %SPRING_PROFILES_ACTIVE%
if /I "%SPRING_PROFILES_ACTIVE%"=="mysql" (
  echo Using MySQL. Ensure the server and echo_waste database are available.
  echo MySQL credentials are read from DB_URL, DB_USER and DB_PASSWORD.
)

set "BACKEND_PORT=%PORT%"
if not defined BACKEND_PORT set "BACKEND_PORT=8081"
powershell -NoProfile -Command "$health = $null; try { $health = Invoke-RestMethod -Uri 'http://127.0.0.1:%BACKEND_PORT%/api/health' -TimeoutSec 2 } catch {}; if ($health.status -eq 'ok') { exit 0 }; $listeners = Get-NetTCPConnection -State Listen -LocalPort %BACKEND_PORT% -ErrorAction SilentlyContinue; if ($listeners) { exit 2 }; exit 1"
set "BACKEND_STATE=%ERRORLEVEL%"
if "%BACKEND_STATE%"=="0" (
  echo Echo backend is already healthy on port %BACKEND_PORT%. Leave it running; no second copy was started.
  exit /b 0
)
if "%BACKEND_STATE%"=="2" (
  echo.
  echo ERROR: Port %BACKEND_PORT% is occupied, but it did not respond as Echo.
  echo Identify the other program before stopping it, or configure a different PORT.
  pause
  exit /b 1
)

call mvnw.cmd spring-boot:run
if errorlevel 1 (
  echo ERROR: backend stopped. Read the first Caused by line above for the root cause.
  echo Check that MySQL is running and the echo_waste database and credentials are configured.
)
pause
