## springboot-cicd 🚀

A production-ready **Java 21 / Spring Boot 3** REST API with a full **5-stage GitHub Actions CI/CD pipeline** including testing, OWASP security scanning, Docker build, staging deploy, and blue/green production deploy.

---

## 📁 Project Structure

```
springboot-cicd/
├── src/
│   ├── main/java/com/devops/app/
│   │   ├── Application.java
│   │   ├── controller/TaskController.java   # REST endpoints
│   │   ├── service/TaskService.java         # Business logic
│   │   └── model/Task.java
│   └── test/java/com/devops/app/
│       └── TaskControllerTest.java          # MockMvc tests
├── Dockerfile                               # Multi-stage, layered build
├── pom.xml                                  # Maven + JaCoCo coverage
└── .github/workflows/ci-cd.yml             # 5-stage CI/CD pipeline
```

---

## ⚙️ CI/CD Pipeline (5 Stages)

```
Push to main
     │
     ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────────┐   ┌─────────────────┐   ┌────────────────────┐
│  Test &      │──▶│   Security   │──▶│  Build & Push    │──▶│  Deploy Staging │──▶│ Deploy Production  │
│  Coverage    │   │  OWASP Scan  │   │  (GHCR Docker)   │   │  + Smoke Test   │   │  (Blue/Green)      │
└──────────────┘   └──────────────┘   └──────────────────┘   └─────────────────┘   └────────────────────┘
```

### Stages:
1. **Test & Analyse** — Maven tests + JaCoCo coverage report
2. **Security** — OWASP Dependency-Check (fails on CVSS ≥ 9)
3. **Build** — Multi-stage Docker build, pushed to GHCR with SHA + `latest` tags
4. **Deploy Staging** — SSH deploy + automated health check smoke test
5. **Deploy Production** — Blue/green deploy with zero-downtime swap

---

## 🚀 Getting Started

### Run locally

```bash
./mvnw spring-boot:run
```

App runs at `http://localhost:8080`

### Run tests with coverage

```bash
./mvnw clean verify
open target/site/jacoco/index.html
```

### Build Docker image

```bash
docker build -t springboot-cicd .
docker run -p 8080:8080 springboot-cicd
```

---

## 📡 API Endpoints

| Method   | Endpoint          | Description         |
|----------|-------------------|---------------------|
| `GET`    | `/api/`           | App info            |
| `GET`    | `/api/tasks`      | List all tasks      |
| `GET`    | `/api/tasks/{id}` | Get task by ID      |
| `POST`   | `/api/tasks`      | Create a task       |
| `PUT`    | `/api/tasks/{id}` | Update a task       |
| `DELETE` | `/api/tasks/{id}` | Delete a task       |
| `GET`    | `/actuator/health`| Health check        |
| `GET`    | `/actuator/info`  | App info            |

---

## 🔧 GitHub Actions Secrets

Add these under `Settings → Secrets and variables → Actions`:

| Secret              | Description                         |
|---------------------|-------------------------------------|
| `STAGING_SSH_KEY`   | Private SSH key for staging server  |
| `STAGING_HOST`      | Staging server hostname/IP          |
| `STAGING_USER`      | SSH username for staging            |
| `PROD_SSH_KEY`      | Private SSH key for production      |
| `PROD_HOST`         | Production server hostname/IP       |
| `PROD_USER`         | SSH username for production         |

> `GITHUB_TOKEN` is provided automatically by GitHub Actions.

---

## 🛡️ Security Highlights

- Non-root user in Docker container
- Spring Boot layered JAR for minimal image size
- OWASP Dependency-Check in CI pipeline
- Secrets managed via GitHub Actions — never hardcoded
- Blue/green deploy with health check before traffic swap
