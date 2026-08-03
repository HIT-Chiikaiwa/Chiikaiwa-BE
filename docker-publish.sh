#!/bin/bash

# =====================================
# Chiikaiwa-BE - Docker Image Build & Publish Script
# =====================================
# Usage:
#   ./docker-publish.sh              # Build and push with tag from .env
#   ./docker-publish.sh 1.0.0        # Build and push with version 1.0.0
#   ./docker-publish.sh latest       # Build and push as latest
# =====================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${BLUE}ℹ ${1}${NC}"
}

print_success() {
    echo -e "${GREEN}✓ ${1}${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ ${1}${NC}"
}

print_error() {
    echo -e "${RED}✗ ${1}${NC}"
}

echo ""
echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}Chiikaiwa-BE Docker Build & Publish${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running. Please start Docker."
    exit 1
fi

print_success "Docker is available"

# Load environment variables from .env file
if [ -f .env ]; then
    print_info "Loading configuration from .env file..."
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
    print_success "Configuration loaded"
else
    print_warning ".env file not found. Using default values."
fi

# Get Docker username
DOCKER_USERNAME=${DOCKER_USERNAME:-yourusername}
if [ "$DOCKER_USERNAME" = "yourusername" ]; then
    print_warning "DOCKER_USERNAME is not set in .env file"
    read -p "Enter your Docker Hub username: " DOCKER_USERNAME
    if [ -z "$DOCKER_USERNAME" ]; then
        print_error "Docker username is required"
        exit 1
    fi
fi

# Get image tag (from parameter or .env or default)
if [ -n "$1" ]; then
    IMAGE_TAG=$1
else
    IMAGE_TAG=${IMAGE_TAG:-latest}
fi

# Set image name
IMAGE_NAME="${DOCKER_USERNAME}/chiikaiwa-be"
FULL_IMAGE_TAG="${IMAGE_NAME}:${IMAGE_TAG}"

print_info "Image will be built as: ${FULL_IMAGE_TAG}"

# Check if user is logged in to Docker Hub
print_info "Checking Docker Hub authentication..."
if ! docker info | grep -q "Username"; then
    print_warning "Not logged in to Docker Hub"
    print_info "Logging in to Docker Hub..."
    docker login
    if [ $? -ne 0 ]; then
        print_error "Docker login failed"
        exit 1
    fi
fi
print_success "Authenticated with Docker Hub"

# Build the Docker image
print_info "Building Docker image..."
echo "----------------------------------------"
docker build -t "${FULL_IMAGE_TAG}" .
if [ $? -ne 0 ]; then
    print_error "Docker build failed"
    exit 1
fi
echo "----------------------------------------"
print_success "Image built successfully: ${FULL_IMAGE_TAG}"

# Tag as latest if building a version
if [ "$IMAGE_TAG" != "latest" ]; then
    print_info "Tagging as latest..."
    docker tag "${FULL_IMAGE_TAG}" "${IMAGE_NAME}:latest"
    print_success "Tagged as ${IMAGE_NAME}:latest"
fi

# Push to Docker Hub
print_info "Pushing ${FULL_IMAGE_TAG} to Docker Hub..."
echo "----------------------------------------"
docker push "${FULL_IMAGE_TAG}"
if [ $? -ne 0 ]; then
    print_error "Failed to push ${FULL_IMAGE_TAG}"
    exit 1
fi
echo "----------------------------------------"
print_success "Pushed ${FULL_IMAGE_TAG}"

# Push latest tag if applicable
if [ "$IMAGE_TAG" != "latest" ]; then
    print_info "Pushing ${IMAGE_NAME}:latest to Docker Hub..."
    docker push "${IMAGE_NAME}:latest"
    if [ $? -ne 0 ]; then
        print_error "Failed to push ${IMAGE_NAME}:latest"
        exit 1
    fi
    print_success "Pushed ${IMAGE_NAME}:latest"
fi

echo ""
echo "========================================"
print_success "Successfully published to Docker Hub!"
echo "========================================"
echo ""
print_info "Image: ${FULL_IMAGE_TAG}"
if [ "$IMAGE_TAG" != "latest" ]; then
    print_info "Also: ${IMAGE_NAME}:latest"
fi
echo ""
print_info "To use on server:"
echo "  1. Update .env with DOCKER_USERNAME=${DOCKER_USERNAME}"
echo "  2. Run: docker-compose pull"
echo "  3. Run: docker-compose up -d"
echo ""
print_info "To pull manually:"
echo "  docker pull ${FULL_IMAGE_TAG}"
echo ""

# Optional: Check if buildx is available for multi-platform builds
if docker buildx version &> /dev/null; then
    print_info "Docker Buildx is available for multi-platform builds"
    print_warning "To build for multiple platforms, use:"
    echo "  docker buildx build --platform linux/amd64,linux/arm64 -t ${FULL_IMAGE_TAG} --push ."
fi

exit 0
