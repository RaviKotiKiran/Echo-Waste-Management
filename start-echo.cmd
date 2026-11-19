@echo off
setlocal EnableExtensions EnableDelayedExpansion
set "PROJECT_ROOT=%~dp0"

if not exist "%PROJECT_ROOT%backend\mvnw.cmd" (
  echo ERROR: backend\mvnw.cmd was not found.
  pause
  exit /b 1
)
if not exist "%PROJECT_ROOT%frontend\package.json" (
  echo ERROR: frontend\package.json was not found.
  pause
  exit /b 1
)

if not exist "%PROJECT_ROOT%frontend\node_modules" (
  echo Installing frontend dependencies...
  pushd "%PROJECT_ROOT%frontend"
  call npm install
  if errorlevel 1 (
    popd
    echo ERROR: Could not install frontend dependencies.
    pause
    exit /b 1
  )
  popd
)

echo Starting Echo backend and frontend...
echo Checking local database availability...
powershell -NoProfile -Command "if (Test-NetConnection 127.0.0.1 -Port 3306 -InformationLevel Quiet -WarningAction SilentlyContinue) { exit 0 } else { exit 1 }"
if errorlevel 1 (
  echo [INFO] Local MySQL service is not running on port 3306.
  echo [INFO] Using built-in embedded database - zero setup required.
) else (
  echo [INFO] Local MySQL service detected on port 3306.
  if not defined SPRING_PROFILES_ACTIVE set "SPRING_PROFILES_ACTIVE=mysql"
  if not defined DB_URL set "DB_URL=jdbc:mysql://localhost:3306/echo_waste?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  if not defined DB_USER set "DB_USER=echo_user"
  if not defined DB_PASSWORD set "DB_PASSWORD=echo_password"
)

set "BACKEND_PORT="
powershell -NoProfile -Command "$health = $null; try { $health = Invoke-RestMethod -Uri 'http://127.0.0.1:8081/api/health' -TimeoutSec 2 } catch {}; if ($health.status -eq 'ok') { exit 0 } else { exit 1 }"
if errorlevel 1 (
  for /f "delims=" %%P in ('powershell -NoProfile -Command "foreach ($p in 8081..8090) { $listener = $null; try { $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $p); $listener.Start(); $listener.Stop(); Write-Output $p; exit 0 } catch { if ($listener) { $listener.Stop() } } }; exit 1"') do set "BACKEND_PORT=%%P"
  if not defined BACKEND_PORT (
    echo ERROR: Ports 8081 through 8090 are all in use.
    echo Free one of those ports and retry.
    pause
    exit /b 1
  )
  set "PORT=!BACKEND_PORT!"
  set "ECHO_BACKEND_URL=http://127.0.0.1:!BACKEND_PORT!"
  echo Starting Echo backend on port !BACKEND_PORT!...
  start "Echo Backend" /D "%PROJECT_ROOT%backend" cmd /k "mvnw.cmd spring-boot:run"
) else (
  set "BACKEND_PORT=8081"
  set "ECHO_BACKEND_URL=http://127.0.0.1:8081"
  echo Echo backend is already healthy on port 8081; reusing it.
)

start "Echo Frontend" /D "%PROJECT_ROOT%frontend" cmd /k "npm run dev"
echo.
echo Keep both Echo terminal windows open while using the application.
echo Open the frontend URL shown by Vite, normally http://localhost:5173.
echo Echo API health: http://localhost:%BACKEND_PORT%/api/health
