# Secrets Management Guide

## Required GitHub Actions Secrets

Set these in: **Repository → Settings → Secrets and variables → Actions**

### Mandatory

| Secret | Description | Example format |
|--------|-------------|----------------|
| `NVD_API_KEY` | NVD API key for OWASP scan (avoids rate limits) | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| `SONAR_TOKEN` | SonarCloud project token | `sqp_xxxxxxxxxxxxx` |

### Staging deployment

| Secret | Description |
|--------|-------------|
| `STAGING_HOST` | IP or hostname of staging server |
| `STAGING_USER` | SSH username (e.g. `ubuntu`) |
| `STAGING_SSH_KEY` | Private SSH key (PEM format, entire key including headers) |
| `STAGING_DATABASE_URL` | `jdbc:postgresql://host:5432/dbname` |
| `STAGING_DATABASE_USER` | DB username |
| `STAGING_DATABASE_PASSWORD` | DB password |

### Production deployment

| Secret | Description |
|--------|-------------|
| `PROD_HOST` | Production hostname/IP |
| `PROD_USER` | SSH username |
| `PROD_SSH_KEY` | Private SSH key |
| `PROD_DATABASE_URL` | Production PostgreSQL JDBC URL |
| `PROD_DATABASE_USER` | DB username |
| `PROD_DATABASE_PASSWORD` | DB password |

### Kubernetes deployment

| Secret | Description |
|--------|-------------|
| `KUBE_CONFIG` | Base64-encoded kubeconfig: `cat ~/.kube/config \| base64` |

## Generating SSH Keys for Deployment

```bash
# Generate dedicated deploy key
ssh-keygen -t ed25519 -C "github-deploy-key" -f ~/.ssh/deploy_key -N ""

# Add public key to server
ssh-copy-id -i ~/.ssh/deploy_key.pub user@staging-host

# Add PRIVATE key as GitHub secret
cat ~/.ssh/deploy_key   # paste this as STAGING_SSH_KEY
```

## NVD API Key

Get a free key at: https://nvd.nist.gov/developers/request-an-api-key

Without a key, OWASP scans may hit rate limits and fail intermittently.
The pipeline uses `continue-on-error: true` so builds won't fail, but add the key for reliability.

## SonarCloud Setup

1. Go to https://sonarcloud.io → Login with GitHub
2. Import your `springboot-cicd` repository
3. Copy the project token → set as `SONAR_TOKEN`
4. The `sonar-project.properties` file is already configured

## Kubernetes Secret (Production)

Use Sealed Secrets to encrypt K8s secrets before committing:

```bash
# Install kubeseal
brew install kubeseal

# Generate and seal the secret
./scripts/generate-secrets.sh springboot-cicd | \
  kubeseal --format yaml --controller-name sealed-secrets \
  > k8s/overlays/production/sealed-secret.yaml

# Commit sealed-secret.yaml (safe to commit — encrypted)
git add k8s/overlays/production/sealed-secret.yaml
```
