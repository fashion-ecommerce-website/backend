#!/bin/bash

# Development Environment Setup Script

echo "🚀 Starting FIT Backend with PostgreSQL..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start PostgreSQL with Docker Compose
echo "📦 Starting PostgreSQL database..."
docker-compose up -d postgres

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 10

# Check if PostgreSQL is running
if ! docker-compose ps | grep -q "postgres.*Up"; then
    echo "❌ PostgreSQL failed to start. Check logs with: docker-compose logs postgres"
    exit 1
fi

echo "✅ PostgreSQL is running!"

# Run the application with development profile
echo "🏃 Starting Spring Boot application..."
mvn spring-boot:run -Dspring.profiles.active=dev

echo "🎉 Application started successfully!"
echo "📊 PgAdmin: http://localhost:8081 (admin@fit.com / admin123)"
echo "🔗 API: http://localhost:8080" 