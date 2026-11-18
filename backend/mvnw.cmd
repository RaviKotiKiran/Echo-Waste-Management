@echo off
setlocal
set "MAVEN_VERSION=3.9.11"
set "MAVEN_ROOT=%~dp0.mvn-runtime"
set "MAVEN_HOME=%MAVEN_ROOT%\apache-maven-%MAVEN_VERSION%"
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  if not exist "%MAVEN_ROOT%" mkdir "%MAVEN_ROOT%"
  echo Downloading Apache Maven %MAVEN_VERSION% for this project...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$zip = Join-Path $env:TEMP 'apache-maven-%MAVEN_VERSION%-bin.zip'; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile $zip; Expand-Archive -LiteralPath $zip -DestinationPath '%MAVEN_ROOT%' -Force; Remove-Item -LiteralPath $zip -Force"
  if errorlevel 1 exit /b 1
)
  call "%MAVEN_HOME%\bin\mvn.cmd" "-Dmaven.repo.local=%~dp0.mvn-repository" %*
exit /b %ERRORLEVEL%
