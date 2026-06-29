#Requires -Version 5.1
<#
.SYNOPSIS
    Build backend JAR and frontend dist, then launch Docker Compose.

.PARAMETER SkipBackend
    Skip Maven build (use existing target/*.jar).

.PARAMETER SkipFrontend
    Skip Angular build (use existing dist/).

.PARAMETER NoDeploy
    Build artifacts only; do not run docker compose up.

.EXAMPLE
    .\build.ps1                   # Full build + docker compose up
    .\build.ps1 -SkipFrontend     # Skip frontend, rebuild backend only
    .\build.ps1 -NoDeploy         # Build both but don't start containers
#>
param(
    [switch]$SkipBackend,
    [switch]$SkipFrontend,
    [switch]$NoDeploy
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$Root = $PSScriptRoot

# ─── Java 21 override ───────────────────────────────────────────────────────
# Maven uses $env:JAVA_HOME; override here if the system default is not Java 21.
$Java21Home = "D:\AORS\Domain\Tool\graalvm-jdk-21.0.4+8.1"
if (Test-Path $Java21Home) {
    $env:JAVA_HOME = $Java21Home
    $env:PATH = "$Java21Home\bin;$env:PATH"
    Write-Host "[INFO] Using Java 21: $Java21Home" -ForegroundColor DarkGray
}

function Write-Step([string]$msg) {
    Write-Host "`n==> $msg" -ForegroundColor Cyan
}

function Assert-ExitCode([string]$step) {
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[FAIL] $step exited with code $LASTEXITCODE" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

# ─── 1. Backend (Maven) ─────────────────────────────────────────────────────
if (-not $SkipBackend) {
    Write-Step "Building backend (mvn package -DskipTests)"
    Push-Location "$Root\backend"
    mvn package -DskipTests -q
    Assert-ExitCode "mvn package"
    Pop-Location
    Write-Host "[OK] Backend JAR ready: backend/target/projecthub-backend-*.jar" -ForegroundColor Green
} else {
    Write-Host "[SKIP] Backend build skipped" -ForegroundColor Yellow
}

# ─── 2. Frontend (Angular) ──────────────────────────────────────────────────
if (-not $SkipFrontend) {
    Write-Step "Building frontend (npm run build:prod)"
    Push-Location "$Root\frontend"
    npm install --legacy-peer-deps --silent
    Assert-ExitCode "npm install"
    npm run build:prod
    Assert-ExitCode "ng build"
    Pop-Location
    Write-Host "[OK] Frontend dist ready: frontend/dist/projecthub-frontend/browser/" -ForegroundColor Green
} else {
    Write-Host "[SKIP] Frontend build skipped" -ForegroundColor Yellow
}

# ─── 3. Verify artifacts exist ──────────────────────────────────────────────
Write-Step "Verifying artifacts"
$jar = Get-Item "$Root\backend\target\projecthub-backend-*.jar" -ErrorAction SilentlyContinue
if (-not $jar) {
    Write-Host "[ERROR] No JAR found in backend/target/. Run without -SkipBackend." -ForegroundColor Red
    exit 1
}
$dist = "$Root\frontend\dist\projecthub-frontend\browser\index.html"
if (-not (Test-Path $dist)) {
    Write-Host "[ERROR] No frontend dist found. Run without -SkipFrontend." -ForegroundColor Red
    exit 1
}
Write-Host "[OK] JAR: $($jar.Name)" -ForegroundColor Green
Write-Host "[OK] Frontend dist: frontend/dist/projecthub-frontend/browser/" -ForegroundColor Green

# ─── 4. Docker Compose ──────────────────────────────────────────────────────
if (-not $NoDeploy) {
    Write-Step "Starting Docker Compose (docker compose up --build -d)"
    Push-Location $Root
    docker compose up --build -d
    Assert-ExitCode "docker compose up"
    Pop-Location

    Write-Host ""
    Write-Host "Services started:" -ForegroundColor Green
    Write-Host "  Frontend  : http://localhost" -ForegroundColor White
    Write-Host "  Backend   : http://localhost:8080/swagger-ui.html" -ForegroundColor White
    Write-Host "  Keycloak  : http://localhost:8180" -ForegroundColor White
    Write-Host "  MinIO     : http://localhost:9001" -ForegroundColor White
} else {
    Write-Host "`n[DONE] Artifacts built. Run 'docker compose up --build -d' when ready." -ForegroundColor Green
}
