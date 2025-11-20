# Reset Kafka topics to clean state
# Use this when you have deserialization errors or need a fresh start

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Resetting Kafka Topics" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Kafka is running
$kafkaRunning = docker ps --filter "name=deploy-kafka-1" --filter "status=running" --format "{{.Names}}"
if (-not $kafkaRunning) {
    Write-Host "ERROR: Kafka container is not running!" -ForegroundColor Red
    Write-Host "Please run: cd deploy && docker-compose up -d kafka zookeeper" -ForegroundColor Yellow
    exit 1
}

Write-Host "Deleting existing topics..." -ForegroundColor Yellow

# Delete topics
docker exec deploy-kafka-1 kafka-topics --bootstrap-server localhost:9092 --delete --topic products.events.v1 2>$null
docker exec deploy-kafka-1 kafka-topics --bootstrap-server localhost:9092 --delete --topic files.events.v1 2>$null

Write-Host "Waiting for topics to be deleted..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

Write-Host "Recreating topics..." -ForegroundColor Green

# Recreate topics
docker exec deploy-kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --topic products.events.v1 --partitions 1 --replication-factor 1
docker exec deploy-kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --topic files.events.v1 --partitions 1 --replication-factor 1

Write-Host ""
Write-Host "Kafka topics reset successfully!" -ForegroundColor Green
Write-Host "You can now restart your services." -ForegroundColor Yellow
