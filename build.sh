#!/usr/bin/env bash
# Usage: ./build.sh [--skip-backend] [--skip-frontend] [--no-deploy]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
SKIP_BACKEND=false
SKIP_FRONTEND=false
NO_DEPLOY=false

for arg in "$@"; do
  case "$arg" in
    --skip-backend)  SKIP_BACKEND=true ;;
    --skip-frontend) SKIP_FRONTEND=true ;;
    --no-deploy)     NO_DEPLOY=true ;;
  esac
done

step() { echo -e "\n\033[36m==> $1\033[0m"; }
ok()   { echo -e "\033[32m[OK] $1\033[0m"; }
fail() { echo -e "\033[31m[ERROR] $1\033[0m" >&2; exit 1; }

# ─── 1. Backend ─────────────────────────────────────────────────────────────
if [ "$SKIP_BACKEND" = false ]; then
  step "Building backend (mvn package -DskipTests)"
  (cd "$ROOT/backend" && mvn package -DskipTests -q)
  ok "Backend JAR ready: backend/target/projecthub-backend-*.jar"
else
  echo "[SKIP] Backend build skipped"
fi

# ─── 2. Frontend ─────────────────────────────────────────────────────────────
if [ "$SKIP_FRONTEND" = false ]; then
  step "Building frontend (npm run build:prod)"
  (cd "$ROOT/frontend" && npm install --legacy-peer-deps --silent && npm run build:prod)
  ok "Frontend dist ready: frontend/dist/projecthub-frontend/browser/"
else
  echo "[SKIP] Frontend build skipped"
fi

# ─── 3. Verify ───────────────────────────────────────────────────────────────
step "Verifying artifacts"
JAR=$(ls "$ROOT/backend/target/projecthub-backend-"*.jar 2>/dev/null | head -1)
[ -z "$JAR" ] && fail "No JAR found in backend/target/. Run without --skip-backend."
[ ! -f "$ROOT/frontend/dist/projecthub-frontend/browser/index.html" ] && \
  fail "No frontend dist found. Run without --skip-frontend."
ok "JAR: $(basename "$JAR")"
ok "Frontend dist: frontend/dist/projecthub-frontend/browser/"

# ─── 4. Docker Compose ───────────────────────────────────────────────────────
if [ "$NO_DEPLOY" = false ]; then
  step "Starting Docker Compose (docker compose up --build -d)"
  (cd "$ROOT" && docker compose up --build -d)
  echo ""
  ok "Services started:"
  echo "  Frontend  : http://localhost"
  echo "  Backend   : http://localhost:8080/swagger-ui.html"
  echo "  Keycloak  : http://localhost:8180"
  echo "  MinIO     : http://localhost:9001"
else
  echo -e "\n\033[32m[DONE] Artifacts built. Run 'docker compose up --build -d' when ready.\033[0m"
fi
