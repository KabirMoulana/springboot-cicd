#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────
# Push Batch 1 (50 commits) to KabirMoulana/springboot-cicd
# Usage: GITHUB_TOKEN=ghp_xxx ./push-to-github.sh
# ─────────────────────────────────────────────────────────────
set -euo pipefail

REPO="KabirMoulana/springboot-cicd"
TOKEN="${GITHUB_TOKEN:-}"

if [ -z "$TOKEN" ]; then
  echo "❌ ERROR: GITHUB_TOKEN is not set"
  echo "Usage: GITHUB_TOKEN=ghp_xxx ./push-to-github.sh"
  exit 1
fi

echo "🔧 Configuring remote..."
git remote remove origin 2>/dev/null || true
git remote add origin "https://${TOKEN}@github.com/${REPO}.git"

echo "🚀 Pushing 50 commits to main (force-push to replace existing history)..."
git push origin main --force

echo ""
echo "✅ Batch 1 done! 50 commits pushed."
echo "   → https://github.com/${REPO}/commits/main"
echo ""
echo "📌 Next steps:"
echo "   1. Add GitHub Secrets (see README for full list)"
echo "   2. Enable GitHub Actions if not already on"
echo "   3. Come back in ~5 hours for Batch 2 (Helm charts, ArgoCD, SonarQube)"
