# CleanSlice Platform - PowerShell Build Scripts

# Build project
function Build-Project {
    Write-Host "Building project..." -ForegroundColor Green
    .\mvnw.cmd clean compile -DskipTests
}

# Package JARs
function Package-Project {
    Write-Host "Packaging JARs..." -ForegroundColor Green
    .\mvnw.cmd clean package -DskipTests
}

# Start services
function Start-Services {
    Write-Host "Starting services..." -ForegroundColor Green
    Package-Project
    Set-Location deploy
    docker-compose up -d
    Set-Location ..
}

# Stop services
function Stop-Services {
    Write-Host "Stopping services..." -ForegroundColor Green
    Set-Location deploy
    docker-compose down
    Set-Location ..
}

# View logs
function Show-Logs {
    Set-Location deploy
    docker-compose logs -f
    Set-Location ..
}

# Run tests
function Run-Tests {
    Write-Host "Running tests..." -ForegroundColor Green
    .\mvnw.cmd test
}

# Clean
function Clean-Project {
    Write-Host "Cleaning project..." -ForegroundColor Green
    .\mvnw.cmd clean
}

# Export functions
Export-ModuleMember -Function Build-Project, Package-Project, Start-Services, Stop-Services, Show-Logs, Run-Tests, Clean-Project
