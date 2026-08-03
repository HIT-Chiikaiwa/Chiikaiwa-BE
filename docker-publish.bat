@echo off
REM =====================================
REM Chiikaiwa-BE - Docker Image Build & Publish Script
REM =====================================
REM Usage:
REM   docker-publish.bat              # Build and push with tag from .env
REM   docker-publish.bat 1.0.0        # Build and push with version 1.0.0
REM   docker-publish.bat latest       # Build and push as latest
REM =====================================

setlocal enabledelayedexpansion

REM Colors (using ANSI escape codes - works in Windows 10+)
set "BLUE=[94m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "NC=[0m"

echo.
echo =====================================
echo Chiikaiwa-BE Docker Build ^& Publish
echo =====================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo %RED%[ERROR] Docker is not installed. Please install Docker Desktop first.%NC%
    exit /b 1
)

REM Check if Docker daemon is running
docker info >nul 2>&1
if errorlevel 1 (
    echo %RED%[ERROR] Docker daemon is not running. Please start Docker Desktop.%NC%
    exit /b 1
)

echo %GREEN%[OK] Docker is available%NC%
echo.

REM Load environment variables from .env file
if exist .env (
    echo %BLUE%[INFO] Loading configuration from .env file...%NC%
    for /f "usebackq tokens=1,2 delims==" %%a in (.env) do (
        set "line=%%a"
        if not "!line:~0,1!"=="#" (
            if not "%%a"=="" (
                set "%%a=%%b"
            )
        )
    )
    echo %GREEN%[OK] Configuration loaded%NC%
) else (
    echo %YELLOW%[WARNING] .env file not found. Using default values.%NC%
)
echo.

REM Get Docker username
if "%DOCKER_USERNAME%"=="" set DOCKER_USERNAME=yourusername
if "%DOCKER_USERNAME%"=="yourusername" (
    echo %YELLOW%[WARNING] DOCKER_USERNAME is not set in .env file%NC%
    set /p DOCKER_USERNAME="Enter your Docker Hub username: "
    if "!DOCKER_USERNAME!"=="" (
        echo %RED%[ERROR] Docker username is required%NC%
        exit /b 1
    )
)

REM Get image tag (from parameter or .env or default)
if not "%~1"=="" (
    set IMAGE_TAG=%~1
) else (
    if "%IMAGE_TAG%"=="" set IMAGE_TAG=latest
)

REM Set image name
set IMAGE_NAME=%DOCKER_USERNAME%/chiikaiwa-be
set FULL_IMAGE_TAG=%IMAGE_NAME%:%IMAGE_TAG%

echo %BLUE%[INFO] Image will be built as: %FULL_IMAGE_TAG%%NC%
echo.

REM Check Docker Hub authentication
echo %BLUE%[INFO] Checking Docker Hub authentication...%NC%
docker info | findstr /C:"Username" >nul 2>&1
if errorlevel 1 (
    echo %YELLOW%[WARNING] Not logged in to Docker Hub%NC%
    echo %BLUE%[INFO] Logging in to Docker Hub...%NC%
    docker login
    if errorlevel 1 (
        echo %RED%[ERROR] Docker login failed%NC%
        exit /b 1
    )
)
echo %GREEN%[OK] Authenticated with Docker Hub%NC%
echo.

REM Build the Docker image
echo %BLUE%[INFO] Building Docker image...%NC%
echo ----------------------------------------
docker build -t "%FULL_IMAGE_TAG%" .
if errorlevel 1 (
    echo.
    echo %RED%[ERROR] Docker build failed%NC%
    exit /b 1
)
echo ----------------------------------------
echo %GREEN%[OK] Image built successfully: %FULL_IMAGE_TAG%%NC%
echo.

REM Tag as latest if building a version
if not "%IMAGE_TAG%"=="latest" (
    echo %BLUE%[INFO] Tagging as latest...%NC%
    docker tag "%FULL_IMAGE_TAG%" "%IMAGE_NAME%:latest"
    echo %GREEN%[OK] Tagged as %IMAGE_NAME%:latest%NC%
    echo.
)

REM Push to Docker Hub
echo %BLUE%[INFO] Pushing %FULL_IMAGE_TAG% to Docker Hub...%NC%
echo ----------------------------------------
docker push "%FULL_IMAGE_TAG%"
if errorlevel 1 (
    echo.
    echo %RED%[ERROR] Failed to push %FULL_IMAGE_TAG%%NC%
    exit /b 1
)
echo ----------------------------------------
echo %GREEN%[OK] Pushed %FULL_IMAGE_TAG%%NC%
echo.

REM Push latest tag if applicable
if not "%IMAGE_TAG%"=="latest" (
    echo %BLUE%[INFO] Pushing %IMAGE_NAME%:latest to Docker Hub...%NC%
    docker push "%IMAGE_NAME%:latest"
    if errorlevel 1 (
        echo.
        echo %RED%[ERROR] Failed to push %IMAGE_NAME%:latest%NC%
        exit /b 1
    )
    echo %GREEN%[OK] Pushed %IMAGE_NAME%:latest%NC%
    echo.
)

echo.
echo ========================================
echo %GREEN%Successfully published to Docker Hub!%NC%
echo ========================================
echo.
echo %BLUE%[INFO] Image: %FULL_IMAGE_TAG%%NC%
if not "%IMAGE_TAG%"=="latest" (
    echo %BLUE%[INFO] Also: %IMAGE_NAME%:latest%NC%
)
echo.
echo %BLUE%[INFO] To use on server:%NC%
echo   1. Update .env with DOCKER_USERNAME=%DOCKER_USERNAME%
echo   2. Run: docker-compose pull
echo   3. Run: docker-compose up -d
echo.
echo %BLUE%[INFO] To pull manually:%NC%
echo   docker pull %FULL_IMAGE_TAG%
echo.

REM Check if buildx is available
docker buildx version >nul 2>&1
if not errorlevel 1 (
    echo %BLUE%[INFO] Docker Buildx is available for multi-platform builds%NC%
    echo %YELLOW%[TIP] To build for multiple platforms, use:%NC%
    echo   docker buildx build --platform linux/amd64,linux/arm64 -t %FULL_IMAGE_TAG% --push .
    echo.
)

endlocal
exit /b 0
