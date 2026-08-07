#!/usr/bin/env pwsh
# =====================================
# Chiikaiwa-BE - Docker Image Build & Publish Script (PowerShell)
# =====================================
# Usage:
#   .\docker-publish.ps1              # Build and push with tag from .env
#   .\docker-publish.ps1 1.0.0        # Build and push with version 1.0.0
#   .\docker-publish.ps1 latest       # Build and push as latest
# =====================================

param(
    [string]$ImageTag = ""
)

# Enable strict mode
$ErrorActionPreference = "Stop"

# Function to print colored messages
function Write-Info {
    param([string]$Message)
    Write-Host "ℹ $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "⚠ $Message" -ForegroundColor Yellow
}

function Write-Err {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Chiikaiwa-BE Docker Build & Publish" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is installed
try {
    $null = docker --version
    Write-Success "Docker is available"
} catch {
    Write-Err "Docker is not installed. Please install Docker Desktop first."
    exit 1
}

# Check if Docker daemon is running
try {
    $null = docker info 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Err "Docker daemon is not running. Please start Docker Desktop."
        exit 1
    }
} catch {
    Write-Err "Docker daemon is not running. Please start Docker Desktop."
    exit 1
}

Write-Host ""

# Load environment variables from .env file
if (Test-Path .env) {
    Write-Info "Loading configuration from .env file..."
    Get-Content .env | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            Set-Variable -Name $key -Value $value -Scope Script
        }
    }
    Write-Success "Configuration loaded"
} else {
    Write-Warn ".env file not found. Using default values."
}

Write-Host ""

# Get Docker username
if (-not $DOCKER_USERNAME -or $DOCKER_USERNAME -eq "yourusername") {
    Write-Warn "DOCKER_USERNAME is not set in .env file"
    $DOCKER_USERNAME = Read-Host "Enter your Docker Hub username"
    if ([string]::IsNullOrWhiteSpace($DOCKER_USERNAME)) {
        Write-Err "Docker username is required"
        exit 1
    }
}

# Get image tag (from parameter or .env or default)
if ($ImageTag) {
    $IMAGE_TAG = $ImageTag
} elseif (-not $IMAGE_TAG) {
    $IMAGE_TAG = "latest"
}

# Set image name
$IMAGE_NAME = "$DOCKER_USERNAME/chiikaiwa-be"
$FULL_IMAGE_TAG = "${IMAGE_NAME}:${IMAGE_TAG}"

Write-Info "Image will be built as: $FULL_IMAGE_TAG"
Write-Host ""

# Check Docker Hub authentication
Write-Info "Checking Docker Hub authentication..."
$dockerInfo = docker info 2>&1 | Out-String
if ($dockerInfo -notmatch "Username") {
    Write-Warn "Not logged in to Docker Hub"
    Write-Info "Logging in to Docker Hub..."
    docker login
    if ($LASTEXITCODE -ne 0) {
        Write-Err "Docker login failed"
        exit 1
    }
}
Write-Success "Authenticated with Docker Hub"
Write-Host ""

# Build the Docker image
Write-Info "Building Docker image..."
Write-Host "----------------------------------------" -ForegroundColor Gray
docker build -t $FULL_IMAGE_TAG .
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Err "Docker build failed"
    exit 1
}
Write-Host "----------------------------------------" -ForegroundColor Gray
Write-Success "Image built successfully: $FULL_IMAGE_TAG"
Write-Host ""

# Tag as latest if building a version
if ($IMAGE_TAG -ne "latest") {
    Write-Info "Tagging as latest..."
    docker tag $FULL_IMAGE_TAG "${IMAGE_NAME}:latest"
    Write-Success "Tagged as ${IMAGE_NAME}:latest"
    Write-Host ""
}

# Push to Docker Hub
Write-Info "Pushing $FULL_IMAGE_TAG to Docker Hub..."
Write-Host "----------------------------------------" -ForegroundColor Gray
docker push $FULL_IMAGE_TAG
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Err "Failed to push $FULL_IMAGE_TAG"
    exit 1
}
Write-Host "----------------------------------------" -ForegroundColor Gray
Write-Success "Pushed $FULL_IMAGE_TAG"
Write-Host ""

# Push latest tag if applicable
if ($IMAGE_TAG -ne "latest") {
    Write-Info "Pushing ${IMAGE_NAME}:latest to Docker Hub..."
    docker push "${IMAGE_NAME}:latest"
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Err "Failed to push ${IMAGE_NAME}:latest"
        exit 1
    }
    Write-Success "Pushed ${IMAGE_NAME}:latest"
    Write-Host ""
}

# Success message
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Successfully published to Docker Hub!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Info "Image: $FULL_IMAGE_TAG"
if ($IMAGE_TAG -ne "latest") {
    Write-Info "Also: ${IMAGE_NAME}:latest"
}
Write-Host ""
Write-Info "To use on server:"
Write-Host "  1. Update .env with DOCKER_USERNAME=$DOCKER_USERNAME"
Write-Host "  2. Run: docker-compose pull"
Write-Host "  3. Run: docker-compose up -d"
Write-Host ""
Write-Info "To pull manually:"
Write-Host "  docker pull $FULL_IMAGE_TAG"
Write-Host ""

# Check if buildx is available
try {
    $null = docker buildx version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Docker Buildx is available for multi-platform builds"
        Write-Warn "To build for multiple platforms, use:"
        Write-Host "  docker buildx build --platform linux/amd64,linux/arm64 -t $FULL_IMAGE_TAG --push ."
        Write-Host ""
    }
} catch {
    # Buildx not available, skip
}

exit 0
