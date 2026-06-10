#!/usr/bin/env bash
# Helper to generate K8s secret YAML from env vars (use with Sealed Secrets in prod)
set -euo pipefail

NAMESPACE="${1:-springboot-cicd}"
SECRET_NAME="app-secrets"

if [ -z "${DATABASE_URL:-}" ] || [ -z "${DATABASE_USER:-}" ] || [ -z "${DATABASE_PASSWORD:-}" ]; then
  echo "Set DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD before running this script"
  exit 1
fi

kubectl create secret generic "$SECRET_NAME" \
  --namespace "$NAMESPACE" \
  --from-literal=database-url="$DATABASE_URL" \
  --from-literal=database-user="$DATABASE_USER" \
  --from-literal=database-password="$DATABASE_PASSWORD" \
  --dry-run=client \
  -o yaml

echo ""
echo "# To apply: pipe the above output into: kubeseal --format yaml > sealed-secret.yaml"
