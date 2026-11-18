@echo off
setlocal
cd /d "%~dp0frontend"

if not exist package.json (
  echo ERROR: frontend/package.json was not found.
  echo This launcher should start inside the frontend folder.
  pause
  exit /b 1
)

if not exist node_modules (
  echo Installing frontend dependencies...
  call npm install
  if errorlevel 1 (
    echo ERROR: npm install failed. Check your internet connection and Node.js installation.
    pause
    exit /b 1
  )
)

echo Starting Echo frontend from: %CD%
call npm run dev
if errorlevel 1 echo ERROR: frontend stopped with an error.
pause
