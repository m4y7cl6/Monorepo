#Requires -Version 5.1
<#
.SYNOPSIS
    Build backend JAR locally, then launch Docker Compose.
    Frontend is built inside Docker (node:20-alpine) to avoid local Node version constraints.

.PARAMETER SkipBackend
    Skip Maven build (use existing target/*.jar).

.PARAMETER NoDeploy
    Build JAR only; do not run docker compose up.

.EXAMPLE
    .\build.ps1              # Build backend JAR + docker compose up (frontend built in Docker)
    .\build.ps1 -SkipBackend # Skip Maven, use existing JAR, then docker compose up
    .\build.ps1 -NoDeploy    # Build JAR only
#>
param(
    [switch]$SkipBackend,
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

# ─── 2. Frontend ─────────────────────────────────────────────────────────────
Write-Host "[INFO] Frontend will be built inside Docker (node:20-alpine) — no local Node required." -ForegroundColor DarkGray

# ─── 3. Verify backend JAR exists ────────────────────────────────────────────
Write-Step "Verifying backend artifact"
$jar = Get-Item "$Root\backend\target\projecthub-backend-*.jar" -ErrorAction SilentlyContinue
if (-not $jar) {
    Write-Host "[ERROR] No JAR found in backend/target/. Run without -SkipBackend." -ForegroundColor Red
    exit 1
}
Write-Host "[OK] JAR: $($jar.Name)" -ForegroundColor Green

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
