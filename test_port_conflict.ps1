# 测试端口冲突检测机制的PowerShell脚本

# 清理之前的服务实例
Write-Host "正在清理之前的服务实例..."
$processes = Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*viewer-service*" -or 
    $_.CommandLine -like "*finance-service*" -or
    $_.CommandLine -like "*analysis-service*" -or
    $_.CommandLine -like "*simulation-service*"
}

if ($processes.Count -gt 0) {
    $processes | Stop-Process -Force
    Write-Host "已清理 $($processes.Count) 个服务实例"
} else {
    Write-Host "没有发现运行中的服务实例"
}

# 等待一段时间让端口释放
Start-Sleep -Seconds 2

# 测试1：启动多个viewer-service实例
Write-Host "\n=== 测试1：启动多个viewer-service实例 ==="
$instances = 3
for ($i = 1; $i -le $instances; $i++) {
    Write-Host "启动第 $i 个viewer-service实例..."
    Start-Process -FilePath "java" -ArgumentList "-jar", ".\viewer-service\target\viewer-service-1.0.0-SNAPSHOT.jar" -NoNewWindow -WorkingDirectory "D:\OPPitems\HOMEWORK\课件\JavaEE架构\JaveEEhomework"
    Start-Sleep -Seconds 1
}

# 等待服务启动
Start-Sleep -Seconds 10

# 检查运行中的服务
Write-Host "\n=== 检查运行中的服务 ==="
$processes = Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*viewer-service*" -or 
    $_.CommandLine -like "*finance-service*" -or
    $_.CommandLine -like "*analysis-service*" -or
    $_.CommandLine -like "*simulation-service*"
}

if ($processes.Count -gt 0) {
    Write-Host "发现 $($processes.Count) 个运行中的服务实例："
    foreach ($process in $processes) {
        Write-Host "进程ID: $($process.Id), 命令行: $($process.CommandLine.Substring(0, [Math]::Min(150, $process.CommandLine.Length)))"
    }
} else {
    Write-Host "没有发现运行中的服务实例"
}

# 检查端口使用情况
Write-Host "\n=== 检查端口使用情况 ==="
$ports = 8081..8099 | ForEach-Object {
    try {
        $socket = New-Object System.Net.Sockets.TcpClient
        $socket.Connect("127.0.0.1", $_)
        $socket.Close()
        $_
    } catch {
        # 端口不可用，忽略
    }
}

if ($ports.Count -gt 0) {
    Write-Host "发现以下端口正在使用：$($ports -join ", ")"
} else {
    Write-Host "没有发现正在使用的端口（8081-8099）"
}

Write-Host "\n=== 测试完成 ==="
Write-Host "请手动检查服务日志以确认端口分配情况"
