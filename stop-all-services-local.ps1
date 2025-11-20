# Stop all local Java services running on ports 8081, 8083, 8084

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Stopping All Local Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Function to kill process on a specific port
function Stop-ServiceOnPort {
    param([int]$port, [string]$serviceName)
    
    try {
        $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction Stop
        if ($connections) {
            foreach ($connection in $connections) {
                $processId = $connection.OwningProcess
                try {
                    Stop-Process -Id $processId -Force -ErrorAction Stop
                    Write-Host "✓ Stopped $serviceName (PID: $processId, Port: $port)" -ForegroundColor Green
                } catch {
                    Write-Host "✗ Failed to stop $serviceName (PID: $processId): $($_.Exception.Message)" -ForegroundColor Red
                }
            }
        } else {
            Write-Host "- $serviceName not running (Port: $port)" -ForegroundColor Gray
        }
    } catch {
        Write-Host "Error checking port $port : $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Stop services
Stop-ServiceOnPort -port 8081 -serviceName "Gateway Service"
Stop-ServiceOnPort -port 8083 -serviceName "Audit Service"
Stop-ServiceOnPort -port 8084 -serviceName "Product Service"
Stop-ServiceOnPort -port 8082 -serviceName "Files Service"

Write-Host ""
Write-Host "Done! All local services stopped." -ForegroundColor Cyan
