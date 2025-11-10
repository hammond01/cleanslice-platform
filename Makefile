# CleanSlice Platform - Makefile

.PHONY: help build package clean up down logs test

help: ## Show this help
	@echo "Available targets:"
	@echo "  build     - Compile the project"
	@echo "  package   - Build JAR files"
	@echo "  clean     - Clean build artifacts"
	@echo "  up        - Start all services with Docker Compose"
	@echo "  down      - Stop all services"
	@echo "  logs      - View logs from all services"
	@echo "  test      - Run tests"

build: ## Compile the project
	./mvnw clean compile -DskipTests

package: ## Build JAR files
	./mvnw clean package -DskipTests

clean: ## Clean build artifacts
	./mvnw clean

up: package ## Start all services
	cd deploy && docker-compose up -d

down: ## Stop all services
	cd deploy && docker-compose down

logs: ## View logs
	cd deploy && docker-compose logs -f

test: ## Run tests
	./mvnw test
