#!/bin/bash

# CampusWorks Backend - Build All Services Script
# This script builds all microservices Docker images

set -e

echo "🚀 Building CampusWorks Backend Services..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Build services in order
services=("eureka-server" "auth-service" "task-service" "bidding-service" "profile-service" "api-gateway")

for service in "${services[@]}"; do
    print_status "Building $service..."
    
    if [ -d "$service" ]; then
        cd "$service"
        
        # Build Docker image
        if docker build -t "campusworks-$service:latest" .; then
            print_status "✅ Successfully built $service"
        else
            print_error "❌ Failed to build $service"
            exit 1
        fi
        
        cd ..
    else
        print_warning "⚠️  Directory $service not found, skipping..."
    fi
done

print_status "🎉 All services built successfully!"
print_status "You can now run 'docker-compose up' to start all services locally."

# List built images
echo ""
print_status "Built Docker images:"
docker images | grep campusworks
