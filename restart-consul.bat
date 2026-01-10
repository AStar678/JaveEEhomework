@echo off
echo Stopping any running Consul instances...
taskkill /F /IM consul.exe >nul 2>&1
echo Consul stopped.

echo Starting Consul...
start start-consul.bat
echo Consul is restarting in a new window.
pause