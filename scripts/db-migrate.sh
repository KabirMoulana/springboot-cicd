#!/usr/bin/env bash
# Run Flyway migrations against a target database
set -euo pipefail

COMMAND="${1:-info}"
ENV="${2:-staging}"

log() { echo "[$(date '+%H:%M:%S')] $*"; }

case "$ENV" in
  staging)
    DB_URL="${STAGING_DATABASE_URL:?STAGING_DATABASE_URL not set}"
    DB_USER="${STAGING_DATABASE_USER:?STAGING_DATABASE_USER not set}"
    DB_PASS="${STAGING_DATABASE_PASSWORD:?STAGING_DATABASE_PASSWORD not set}"
    ;;
  production)
    DB_URL="${PROD_DATABASE_URL:?PROD_DATABASE_URL not set}"
    DB_USER="${PROD_DATABASE_USER:?PROD_DATABASE_USER not set}"
    DB_PASS="${PROD_DATABASE_PASSWORD:?PROD_DATABASE_PASSWORD not set}"
    ;;
  *)
    echo "Unknown environment: $ENV (use staging|production)"
    exit 1
    ;;
esac

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

log "Running Flyway $COMMAND against $ENV"
./mvnw flyway:"$COMMAND" -B \
  -Dflyway.url="$DB_URL" \
  -Dflyway.user="$DB_USER" \
  -Dflyway.password="$DB_PASS" \
  -Dflyway.locations=filesystem:src/main/resources/db/migration

log "Done."
