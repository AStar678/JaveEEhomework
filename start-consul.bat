@echo off
set CONSUL_VER=1.15.4
set CONSUL_ZIP=consul_%CONSUL_VER%_windows_amd64.zip
set CONSUL_URL=https://releases.hashicorp.com/consul/%CONSUL_VER%/%CONSUL_ZIP%

if exist consul.exe (
    echo Consul already installed.
) else (
    echo Downloading Consul %CONSUL_VER%...
    powershell -Command "Invoke-WebRequest -Uri %CONSUL_URL% -OutFile %CONSUL_ZIP%"
    
    echo Extracting Consul...
    powershell -Command "Expand-Archive -Path %CONSUL_ZIP% -DestinationPath . -Force"
    
    del %CONSUL_ZIP%
    echo Consul installed successfully.
)

echo Starting Consul in development mode...
echo Please keep this window open.
consul agent -dev