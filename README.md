# springboot-cicd 🚀

[![CI](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/ci.yml/badge.svg)](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/ci.yml)
[![CD](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/cd.yml/badge.svg)](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/cd.yml)
[![Security Scan](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/scheduled-security.yml/badge.svg)](https://github.com/KabirMoulana/springboot-cicd/actions/workflows/scheduled-security.yml)
[![codecov](https://codecov.io/gh/KabirMoulana/springboot-cicd/branch/main/graph/badge.svg)](https://codecov.io/gh/KabirMoulana/springboot-cicd)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A **production-ready Java 21 / Spring Boot 3.3** REST API with an enterprise-grade CI/CD pipeline using GitHub Actions.

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                     GitHub Actions CI/CD                      │
│                                                              │
│  Push/PR → CI (test+sonar+owasp+lint) → CD (build+deploy)   │
│                  ↓ weekly cron                               │
│           Security (Trivy + CodeQL)                          │
└──────────────────────────────────────────────────────────────┘
               ↓ GHCR image
┌──────────────────────────────────────┐
│           Kubernetes / Docker        │
│  ┌──────────┐    ┌────────────────┐  │
│  │  App Pod │    │   PostgreSQL   │  │
│  │ (x2 min) │────│   (via K8s)    │  │
│  └──────────┘    └────────────────┘  │
│       │                              │
│  ┌────┴────────────────────────────┐ │
│  │  Prometheus → Grafana           │ │
│  └─────────────────────────────────┘ │
└──────────────────────────────────────┘
```

## 📁 Project Structure

```
springboot-cicd/
├── .github/
│   ├── workflows/
│   │   ├── ci.yml                  # Tests, SonarCloud, OWASP, Lint
│   │   ├── cd.yml                  # Docker build/push, staging, blue/green prod
│   │   ├── pr-checks.yml           # PR validation, coverage comment, Hadolint
│   │   ├── release.yml             # Tag-triggered releases
│   │   ├── scheduled-security.yml  # Weekly Trivy + CodeQL
│   │   ├── performance-test.yml    # k6 load tests (manual trigger)
│   │   └── dependency-update.yml   # Weekly outdated deps check
│   ├── ISSUE_TEMPLATE/
│   └── dependabot.yml
├── src/
│   ├── main/java/com/devops/app/
│   │   ├── Application.java
│   │   ├── config/          # Security, Cache, Metrics, OpenAPI, Actuator
│   │   ├── controller/      # TaskController, InfoController
│   │   ├── dto/             # TaskRequest, TaskResponse, PagedResponse
│   │   ├── exception/       # GlobalExceptionHandler (RFC 9457 ProblemDetail)
│   │   ├── model/           # Task (JPA entity)
│   │   ├── repository/      # TaskRepository (custom JPQL queries)
│   │   └── service/         # TaskService (caching, pagination)
│   ├── main/resources/
│   │   ├── application.yml          # Base config
│   │   ├── application-prod.yml     # PostgreSQL + Flyway
│   │   ├── application-staging.yml
│   │   └── db/migration/            # Flyway SQL migrations
│   └── test/
│       ├── controller/  # WebMvcTest slice tests
│       ├── service/     # Mockito unit tests
│       ├── repository/  # DataJpaTest slice tests
│       └── integration/ # Full SpringBootTest
├── k8s/
│   ├── base/        # Deployment, Service, Ingress, HPA, PDB, RBAC
│   └── overlays/    # staging / production Kustomize overlays
├── performance/     # k6 load test scripts
├── monitoring/      # Prometheus + Grafana configs
├── scripts/         # local-dev.sh, smoke-test.sh
├── Dockerfile       # Multi-stage, layered JAR, non-root
├── docker-compose.yml
└── pom.xml
```

## ⚙️ CI/CD Pipeline

```
Push to main
     │
     ▼
┌──────────────┐   ┌──────────────┐   ┌───────────────────┐
│  CI Pipeline │   │  CD Pipeline │   │  Weekly Scheduled │
│              │   │              │   │                   │
│ • Unit tests │──▶│ • Build JAR  │   │ • Trivy scan      │
│ • JaCoCo     │   │ • Docker     │   │ • CodeQL SAST     │
│ • SonarCloud │   │   multi-arch │   │ • Dep updates     │
│ • OWASP scan │   │ • Push GHCR  │   └───────────────────┘
│ • Lint       │   │ • Staging    │
└──────────────┘   │   + smoke    │
                   │ • Prod       │
                   │   blue/green │
                   └──────────────┘
```

## 🚀 Quick Start

### Run locally

```bash
./scripts/local-dev.sh start
```

### Run tests

```bash
./mvnw verify
# Coverage: target/site/jacoco/index.html
```

### Build Docker image

```bash
docker build -t springboot-cicd:local .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=default springboot-cicd:local
```

### Smoke test

```bash
./scripts/smoke-test.sh http://localhost:8080
```

## 📡 API Endpoints

| Method   | Endpoint              | Description              |
|----------|-----------------------|--------------------------|
| `GET`    | `/api/`               | App info                 |
| `GET`    | `/api/tasks`          | List tasks (paginated)   |
| `GET`    | `/api/tasks?title=X`  | Search tasks             |
| `GET`    | `/api/tasks/{id}`     | Get task by ID           |
| `POST`   | `/api/tasks`          | Create task              |
| `PUT`    | `/api/tasks/{id}`     | Update task              |
| `DELETE` | `/api/tasks/{id}`     | Delete task              |
| `GET`    | `/api/tasks/stats`    | Status summary           |
| `GET`    | `/actuator/health`    | Health (liveness)        |
| `GET`    | `/actuator/prometheus`| Prometheus metrics       |
| `GET`    | `/swagger-ui.html`    | Swagger UI               |

## 🔧 GitHub Actions Secrets Required

| Secret                   | Description                         |
|--------------------------|-------------------------------------|
| `STAGING_SSH_KEY`        | Private SSH key for staging server  |
| `STAGING_HOST`           | Staging server hostname/IP          |
| `STAGING_USER`           | SSH username                        |
| `STAGING_DATABASE_URL`   | Staging PostgreSQL JDBC URL         |
| `STAGING_DATABASE_USER`  | DB username                         |
| `STAGING_DATABASE_PASSWORD` | DB password                      |
| `PROD_SSH_KEY`           | Private SSH key for production      |
| `PROD_HOST`              | Production hostname/IP              |
| `PROD_USER`              | SSH username                        |
| `PROD_DATABASE_URL`      | Production PostgreSQL JDBC URL      |
| `PROD_DATABASE_USER`     | DB username                         |
| `PROD_DATABASE_PASSWORD` | DB password                         |
| `NVD_API_KEY`            | NVD API key for OWASP scan          |
| `SONAR_TOKEN`            | SonarCloud token                    |

> `GITHUB_TOKEN` is provided automatically.

## 🛡️ Security

- Non-root Docker container user
- Spring Security stateless filter chain
- OWASP Dependency-Check (fails on CVSS ≥ 9)
- Trivy container vulnerability scan (weekly)
- CodeQL SAST analysis (weekly)
- Secrets via GitHub Actions — never hardcoded
- K8s `readOnlyRootFilesystem`, dropped capabilities

## 📦 Deploy to Kubernetes

```bash
# Staging
kubectl apply -k k8s/overlays/staging

# Production
kubectl apply -k k8s/overlays/production
```

## 📄 License

MIT © [Kabir Moulana](https://github.com/KabirMoulana)
