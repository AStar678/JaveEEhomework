@echo off
set REDIS_VER=3.0.504
set REDIS_ZIP=Redis-x64-%REDIS_VER%.zip
set REDIS_URL=https://github.com/microsoftarchive/redis/releases/download/win-%REDIS_VER%/%REDIS_ZIP%

if exist redis-server.exe (
    echo Redis already installed.
) else (
    echo Downloading Redis %REDIS_VER%...
    powershell -Command "Invoke-WebRequest -Uri %REDIS_URL% -OutFile %REDIS_ZIP%"
    
    echo Extracting Redis...
    powershell -Command "Expand-Archive -Path %REDIS_ZIP% -DestinationPath . -Force"
    
    del %REDIS_ZIP%
    echo Redis installed successfully.
)

echo Starting Redis Server...
echo Please keep this window open.
redis-server.exe redis.windows.conf