#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

log() { echo "[$(date '+%H:%M:%S')] $*"; }

case "${1:-help}" in
  start)
    log "Starting full local stack (app + postgres + prometheus + grafana)..."
    cd "$ROOT_DIR"
    docker compose up -d
    log "Waiting for app health check..."
    for i in $(seq 1 20); do
      STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null || echo "000")
      [ "$STATUS" = "200" ] && { log "✅ App ready at http://localhost:8080"; break; }
      [ "$i" -eq 20 ] && { log "❌ App did not start in time"; exit 1; }
      sleep 5
    done
    log "Grafana:    http://localhost:3000 (admin/admin)"
    log "Prometheus: http://localhost:9090"
    log "H2 Console: http://localhost:8080/h2-console"
    ;;
  stop)
    cd "$ROOT_DIR"
    docker compose down
    log "Stack stopped"
    ;;
  test)
    log "Running tests..."
    cd "$ROOT_DIR"
    ./mvnw verify -B
    log "Tests complete. Coverage: target/site/jacoco/index.html"
    ;;
  build)
    log "Building Docker image..."
    cd "$ROOT_DIR"
    docker build -t springboot-cicd:local .
    log "Image built: springboot-cicd:local"
    ;;
  logs)
    docker compose logs -f app
    ;;
  help|*)
    echo "Usage: $0 {start|stop|test|build|logs}"
    ;;
esac
