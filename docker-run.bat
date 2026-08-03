@echo off
REM Chiikaiwa-BE - Docker Build & Run Script for Windows
REM Usage: docker-run.bat [dev|prod|build|stop|logs|clean|status]

setlocal enabledelayedexpansion

REM Colors (using ANSI escape codes - works in Windows 10+)
set "BLUE=[94m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "NC=[0m"

REM Check if Docker is installed
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo %RED%[ERROR] Docker is not installed. Please install Docker Desktop first.%NC%
    exit /b 1
)

where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    echo %RED%[ERROR] Docker Compose is not installed. Please install Docker Compose first.%NC%
    exit /b 1
)

if "%1"=="" goto usage
if "%1"=="build" goto build
if "%1"=="dev" goto dev
if "%1"=="prod" goto prod
if "%1"=="stop" goto stop
if "%1"=="logs" goto logs
if "%1"=="clean" goto clean
if "%1"=="status" goto status
goto usage

:build
echo %BLUE%[INFO] Building Docker image...%NC%
docker build -t chiikaiwa-be:latest .
if %errorlevel% equ 0 (
    echo %GREEN%[OK] Build completed successfully!%NC%
) else (
    echo %RED%[ERROR] Build failed!%NC%
    exit /b 1
)
goto end

:dev
echo %BLUE%[INFO] Starting development environment...%NC%
echo %BLUE%[INFO] Starting Redis for local development...%NC%
docker-compose up -d redis
if exist docker-compose.dev.yml (
    docker-compose -f docker-compose.dev.yml up -d
)
echo.
echo %GREEN%[OK] Development environment started!%NC%
echo %BLUE%[INFO] Redis:   localhost:6379%NC%
echo %BLUE%[INFO] Run your application locally with: mvnw spring-boot:run%NC%
echo %BLUE%[INFO] Swagger: http://localhost:8080/swagger-ui/index.html%NC%
goto end

:prod
echo %BLUE%[INFO] Starting production environment...%NC%

if not exist .env (
    echo %RED%[ERROR] .env file not found!%NC%
    echo %BLUE%[INFO] Creating .env from template...%NC%
    (
        echo # Docker Configuration
        echo DOCKER_USERNAME=chaosql
        echo IMAGE_TAG=latest
        echo.
        echo # Database Configuration
        echo DB_HOST=postgres
        echo DB_NAME=chiikaiwa
        echo DB_USERNAME=postgres
        echo DB_PASSWORD=your_secure_password_here
        echo.
        echo # Redis Configuration
        echo REDIS_HOST=redis
        echo REDIS_PORT=6379
        echo REDIS_PASSWORD=
        echo.
        echo # JWT Security
        echo JWT_SECRET=your_jwt_secret_here
        echo TTL_MINUTES=30
        echo.
        echo # Cloudinary Media Storage
        echo CLOUD_NAME=your_cloud_name
        echo CLOUD_API_KEY=your_api_key
        echo CLOUD_API_SECRET=your_api_secret
        echo.
        echo # Brevo Email SMTP
        echo BREVO_NAME=Chiikaiwa
        echo BREVO_SENDER_EMAIL=noreply@chiikaiwa.com
        echo BREVO_API_KEY=your_brevo_api_key
        echo.
        echo # Firebase Notifications
        echo FIREBASE_CREDENTIALS={}
        echo.
        echo # Application Configuration
        echo SPRING_PROFILES_ACTIVE=prod
    ) > .env
    echo %YELLOW%[WARNING] Please edit .env file with your configuration!%NC%
    exit /b 1
)

docker-compose pull
docker-compose up -d
echo.
echo %GREEN%[OK] Production environment started!%NC%
echo %BLUE%[INFO] Application: http://localhost:8080%NC%
echo %BLUE%[INFO] Swagger:     http://localhost:8080/swagger-ui/index.html%NC%
echo %BLUE%[INFO] API Docs:    http://localhost:8080/v3/api-docs%NC%
goto end

:stop
echo %BLUE%[INFO] Stopping all containers...%NC%
if exist docker-compose.dev.yml (
    docker-compose -f docker-compose.dev.yml down 2>nul
)
docker-compose down 2>nul
echo %GREEN%[OK] All containers stopped!%NC%
goto end

:logs
set SERVICE=app
if not "%2"=="" set SERVICE=%2
echo %BLUE%[INFO] Showing logs for %SERVICE%...%NC%
docker-compose logs -f %SERVICE%
goto end

:clean
echo %YELLOW%[WARNING] This will remove all containers, volumes, and images!%NC%
set /p CONFIRM="Are you sure? (yes/no): "
if /i "%CONFIRM%"=="yes" (
    echo %BLUE%[INFO] Cleaning up...%NC%
    docker-compose down -v
    if exist docker-compose.dev.yml (
        docker-compose -f docker-compose.dev.yml down -v
    )
    docker rmi chiikaiwa-be:latest 2>nul
    echo %GREEN%[OK] Cleanup completed!%NC%
) else (
    echo %BLUE%[INFO] Cleanup cancelled.%NC%
)
goto end

:status
echo %BLUE%[INFO] Container status:%NC%
docker ps -a | findstr chiikaiwa
if %errorlevel% neq 0 (
    echo %YELLOW%[WARNING] No Chiikaiwa containers found%NC%
)
echo.
echo %BLUE%[INFO] Volume status:%NC%
docker volume ls | findstr chiikaiwa
if %errorlevel% neq 0 (
    echo %YELLOW%[WARNING] No Chiikaiwa volumes found%NC%
)
goto end

:usage
echo.
echo =============================================
echo Chiikaiwa-BE - Docker Management Script
echo =============================================
echo.
echo Usage: %0 {build^|dev^|prod^|stop^|logs^|clean^|status}
echo.
echo Commands:
echo   build   - Build Docker image locally
echo   dev     - Start development environment (Redis only)
echo   prod    - Start production environment (full stack)
echo   stop    - Stop all containers
echo   logs    - View container logs (default: app, or specify service)
echo   clean   - Remove all containers, volumes, and images
echo   status  - Show container and volume status
echo.
echo Examples:
echo   %0 build          Build the Docker image
echo   %0 dev            Start Redis for local development
echo   %0 prod           Start full production stack
echo   %0 logs app       View logs for app service
echo   %0 logs redis     View logs for redis service
echo   %0 stop           Stop all running containers
echo.
exit /b 1

:end
endlocal
