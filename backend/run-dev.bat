@echo off
REM Development Environment Setup Script for Windows

echo 🚀 Starting FIT Backend with PostgreSQL...

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not running. Please start Docker first.
    pause
    exit /b 1
)

REM Start PostgreSQL with Docker Compose
echo 📦 Starting PostgreSQL database...
docker-compose up -d postgres

REM Wait for PostgreSQL to be ready
echo ⏳ Waiting for PostgreSQL to be ready...
timeout /t 10 /nobreak >nul

REM Check if PostgreSQL is running
docker-compose ps | findstr "postgres.*Up" >nul
if errorlevel 1 (
    echo ❌ PostgreSQL failed to start. Check logs with: docker-compose logs postgres
    pause
    exit /b 1
)

echo ✅ PostgreSQL is running!

REM Run the application with development profile
echo 🏃 Starting Spring Boot application...
mvn spring-boot:run -Dspring.profiles.active=dev

echo 🎉 Application started successfully!
echo 📊 PgAdmin: http://localhost:8081 (admin@fit.com / admin123)
echo 🔗 API: http://localhost:8080
pause 