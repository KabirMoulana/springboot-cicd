#!/usr/bin/env bash
# Push ALL commits (batch 1 + batch 2 fixes) to GitHub
# Usage: GITHUB_TOKEN=ghp_xxx ./push-batch2.sh
set -euo pipefail

REPO="KabirMoulana/springboot-cicd"
TOKEN="${GITHUB_TOKEN:-}"

if [ -z "$TOKEN" ]; then
  echo "❌ Set GITHUB_TOKEN first: export GITHUB_TOKEN=ghp_xxx"
  exit 1
fi

echo "Total commits: $(git log --oneline | wc -l)"
echo "Pushing to https://github.com/${REPO}..."

git remote set-url origin "https://${TOKEN}@github.com/${REPO}.git"
git push origin main --force

echo ""
echo "✅ Done! https://github.com/${REPO}/commits/main"
echo ""
echo "CI pipeline will start automatically."
echo "Watch it at: https://github.com/${REPO}/actions"
