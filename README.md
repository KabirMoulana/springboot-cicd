# springboot-cicd 🚀

[![CI](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/ci.yml/badge.svg)](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/ci.yml)
[![CD](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/cd.yml/badge.svg)](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/cd.yml)
[![Integration Tests](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/integration-tests.yml/badge.svg)](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/integration-tests.yml)
[![Security Scan](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/scheduled-security.yml/badge.svg)](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/scheduled-security.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A **production-ready Java 21 / Spring Boot 3.3.5** Task Management REST API with an enterprise-grade CI/CD pipeline — 100 commits of real, working infrastructure.

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    GitHub Actions (9 workflows)               │
│  ci.yml · cd.yml · pr-checks · release · integration-tests  │
│  scheduled-security · performance · helm-ci · gitops-update │
└──────────────────────────────────────────────────────────────┘
               ↓ GHCR multi-arch image (amd64/arm64)
┌──────────────────────────────────────────────────┐
│       Kubernetes (Helm + Kustomize + ArgoCD)      │
│  ┌─────────────────┐   ┌────────────────────────┐ │
│  │  App Deployment  │   │  PostgreSQL (external) │ │
│  │  HPA: 2–10 pods  │───│  Flyway migrations     │ │
│  │  PDB: minAvail=1 │   └────────────────────────┘ │
│  └─────────────────┘                               │
│  NetworkPolicy · RBAC · Sealed Secrets            │
└──────────────────────────────────────────────────┘
               ↓ Prometheus scrape
┌─────────────────────────────────┐
│  Prometheus → Grafana           │
│  Custom metrics · Alert rules   │
└─────────────────────────────────┘
```

## 📁 Project Structure

```
springboot-cicd/
├── .github/
│   ├── workflows/          # 9 GitHub Actions workflows
│   ├── ISSUE_TEMPLATE/     # Bug + feature templates
│   ├── pull_request_template.md
│   └── dependabot.yml
├── src/
│   ├── main/java/com/devops/app/
│   │   ├── config/         # Security, Cache, Metrics, OpenAPI, Async, JPA, Logging
│   │   ├── controller/     # Task, Info, Audit controllers
│   │   ├── dto/            # Request/Response/Paged/Error records
│   │   ├── exception/      # RFC 9457 ProblemDetail handler
│   │   ├── model/          # Task, AuditLog JPA entities
│   │   ├── repository/     # TaskRepository, AuditLogRepository
│   │   └── service/        # TaskService, AuditService, TaskEventPublisher
│   ├── main/resources/
│   │   ├── application*.yml           # Base + prod + staging profiles
│   │   ├── logback-spring.xml         # JSON (prod) + human (dev) logging
│   │   └── db/migration/              # Flyway V1–V3 SQL migrations
│   └── test/               # Unit, slice, integration, cache, audit tests
├── helm/springboot-cicd/   # Full Helm chart with env values files
├── k8s/                    # Kustomize base + staging/production overlays
├── argocd/                 # ArgoCD Application + AppProject manifests
├── performance/            # k6 load test script
├── monitoring/             # Prometheus rules + Grafana dashboard JSON
├── scripts/                # local-dev, smoke-test, db-migrate, generate-secrets
├── docs/                   # ARCHITECTURE.md, RUNBOOK.md, SECRETS.md
├── Dockerfile              # Multi-stage layered JAR, non-root
├── docker-compose.yml      # Full local stack
└── Makefile                # Developer shortcuts
```

## 🚀 Quick Start

```bash
# Full local stack (app + postgres + prometheus + grafana)
make docker-up

# Run all tests
make test

# Smoke test
make smoke-test
```

## 📡 API Endpoints

| Method   | Endpoint                    | Description            |
|----------|-----------------------------|------------------------|
| `GET`    | `/api/`                     | App info               |
| `GET`    | `/api/tasks`                | List (paginated)       |
| `GET`    | `/api/tasks?title=X&status=TODO` | Search/filter     |
| `GET`    | `/api/tasks/{id}`           | Get by ID              |
| `POST`   | `/api/tasks`                | Create task            |
| `PUT`    | `/api/tasks/{id}`           | Full update            |
| `PATCH`  | `/api/tasks/{id}/status`    | Status-only update     |
| `DELETE` | `/api/tasks/{id}`           | Delete task            |
| `GET`    | `/api/tasks/stats`          | Status summary         |
| `GET`    | `/api/audit`                | Audit log              |
| `GET`    | `/api/audit/tasks/{id}`     | Task audit trail       |
| `GET`    | `/actuator/health`          | Health (liveness)      |
| `GET`    | `/actuator/prometheus`      | Prometheus metrics     |
| `GET`    | `/swagger-ui.html`          | Swagger UI             |

## 🔧 CI/CD Workflows

| Workflow | Trigger | What it does |
|----------|---------|--------------|
| `ci.yml` | push/PR | Tests, JaCoCo, SonarCloud, OWASP, lint |
| `cd.yml` | push main | Build multi-arch image → staging → prod blue/green |
| `pr-checks.yml` | PR | Conventional Commits, coverage, Hadolint |
| `release.yml` | `v*.*.*` tag | GitHub Release + JAR attachment |
| `integration-tests.yml` | push/nightly | Tests against real PostgreSQL service |
| `scheduled-security.yml` | weekly | Trivy + CodeQL SAST |
| `performance-test.yml` | manual | k6 load test |
| `helm-ci.yml` | helm/** | Helm lint + template render + chart-testing |
| `gitops-update.yml` | CD success | Auto-update staging tag, PR for prod |

## 📄 Docs

- [Architecture](docs/ARCHITECTURE.md)
- [Runbook](docs/RUNBOOK.md)
- [Secrets Setup](docs/SECRETS.md)
- [Contributing](CONTRIBUTING.md)

## 📄 License

MIT © [Kabir Moulana](https://github.com/KabirMoulana)
