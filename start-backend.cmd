@echo off
setlocal
cd /d "%~dp0backend"

if not exist mvnw.cmd (
  echo ERROR: backend/mvnw.cmd was not found.
  pause
  exit /b 1
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
  echo ERROR: Port %BACKEND_PORT% is occupied by another process.
  echo Free port %BACKEND_PORT% or configure a different PORT.
  pause
  exit /b 1
)

echo Starting Echo backend on port %BACKEND_PORT%...
call mvnw.cmd spring-boot:run
if errorlevel 1 (
  echo ERROR: Backend stopped unexpectedly.
)
pause
