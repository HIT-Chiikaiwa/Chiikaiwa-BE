#!/bin/bash

# Chiikaiwa-BE - Docker Build & Run Script
# Usage: ./docker-run.sh [dev|prod|build|stop|logs|clean|status]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
fi

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[OK]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if Docker is installed
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
}

# Build Docker image
build() {
    print_info "Building Docker image..."
    docker build -t chiikaiwa-be:latest .
    print_success "Build completed successfully!"
}

# Run in development mode (Redis only for local Spring Boot)
dev() {
    print_info "Starting development environment..."
    print_info "Starting Redis for local development..."
    docker-compose up -d redis
    if [ -f docker-compose.dev.yml ]; then
        docker-compose -f docker-compose.dev.yml up -d
    fi
    echo ""
    print_success "Development environment started!"
    print_info "Redis:   localhost:6379"
    print_info "Run your application locally with: ./mvnw spring-boot:run"
    print_info "Swagger: http://localhost:8080/swagger-ui/index.html"
}

# Run in production mode (full stack)
prod() {
    print_info "Starting production environment..."

    # Check if .env file exists
    if [ ! -f .env ]; then
        print_error ".env file not found!"
        print_info "Creating .env from template..."
        cat > .env <<EOF
# Docker Configuration
DOCKER_USERNAME=chaosql
IMAGE_TAG=latest

# Database Configuration
DB_HOST=postgres
DB_NAME=chiikaiwa
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password_here

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT Security
JWT_SECRET=your_jwt_secret_here
TTL_MINUTES=30

# Cloudinary Media Storage
CLOUD_NAME=your_cloud_name
CLOUD_API_KEY=your_api_key
CLOUD_API_SECRET=your_api_secret

# Brevo Email SMTP
BREVO_NAME=Chiikaiwa
BREVO_SENDER_EMAIL=noreply@chiikaiwa.com
BREVO_API_KEY=your_brevo_api_key

# Firebase Notifications
FIREBASE_CREDENTIALS={}

# Application Configuration
SPRING_PROFILES_ACTIVE=prod
EOF
        print_warning "Please edit .env file with your configuration!"
        exit 1
    fi

    docker-compose pull
    docker-compose up -d
    echo ""
    print_success "Production environment started!"
    print_info "Application: http://localhost:${APP_PORT:-8080}"
    print_info "Swagger:     http://localhost:${APP_PORT:-8080}/swagger-ui/index.html"
    print_info "API Docs:    http://localhost:${APP_PORT:-8080}/v3/api-docs"
}

# Stop all containers
stop() {
    print_info "Stopping all containers..."
    docker-compose -f docker-compose.dev.yml down 2>/dev/null || true
    docker-compose down 2>/dev/null || true
    print_success "All containers stopped!"
}

# View logs
logs() {
    SERVICE=${2:-app}
    print_info "Showing logs for $SERVICE..."
    docker-compose logs -f $SERVICE
}

# Clean up everything
clean() {
    print_warning "This will remove all containers, volumes, and images!"
    read -p "Are you sure? (yes/no): " -r
    if [[ $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        print_info "Cleaning up..."
        docker-compose down -v
        docker-compose -f docker-compose.dev.yml down -v 2>/dev/null || true
        docker rmi chiikaiwa-be:latest 2>/dev/null || true
        print_success "Cleanup completed!"
    else
        print_info "Cleanup cancelled."
    fi
}

# Show status
status() {
    print_info "Container status:"
    docker ps -a | grep chiikaiwa || print_warning "No Chiikaiwa containers found"
    echo ""
    print_info "Volume status:"
    docker volume ls | grep chiikaiwa || print_warning "No Chiikaiwa volumes found"
}

# Main script
check_docker

case "$1" in
    build)
        build
        ;;
    dev)
        dev
        ;;
    prod)
        prod
        ;;
    stop)
        stop
        ;;
    logs)
        logs "$@"
        ;;
    clean)
        clean
        ;;
    status)
        status
        ;;
    *)
        echo ""
        echo "============================================="
        echo "Chiikaiwa-BE - Docker Management Script"
        echo "============================================="
        echo ""
        echo "Usage: $0 {build|dev|prod|stop|logs|clean|status}"
        echo ""
        echo "Commands:"
        echo "  build   - Build Docker image locally"
        echo "  dev     - Start development environment (Redis only)"
        echo "  prod    - Start production environment (full stack)"
        echo "  stop    - Stop all containers"
        echo "  logs    - View container logs (default: app, or specify service)"
        echo "  clean   - Remove all containers, volumes, and images"
        echo "  status  - Show container and volume status"
        echo ""
        echo "Examples:"
        echo "  $0 build          Build the Docker image"
        echo "  $0 dev            Start Redis for local development"
        echo "  $0 prod           Start full production stack"
        echo "  $0 logs app       View logs for app service"
        echo "  $0 logs redis     View logs for redis service"
        echo "  $0 stop           Stop all running containers"
        echo ""
        exit 1
        ;;
esac
