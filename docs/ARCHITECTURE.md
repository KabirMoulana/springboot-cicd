# Architecture Overview

## Application Layers

```
HTTP Request
     │
     ▼
┌──────────────────────────────────────────────┐
│               Spring Security                 │  CORS, stateless, rate limit
└──────────────────────────────────────────────┘
     │
     ▼
┌──────────────────────────────────────────────┐
│           Request Logging Filter              │  MDC (requestId), X-Request-ID
└──────────────────────────────────────────────┘
     │
     ▼
┌──────────────────────────────────────────────┐
│          REST Controllers                     │  TaskController, AuditController
│   (validation, mapping, HTTP semantics)      │
└──────────────────────────────────────────────┘
     │
     ▼
┌──────────────────────────────────────────────┐
│           Service Layer                       │  TaskService
│  (business logic, caching, transactions)     │  + TaskEventPublisher (metrics)
└──────────────────────────────────────────────┘
     │                      │
     ▼                      ▼ (async)
┌─────────────┐      ┌─────────────────┐
│  Repository │      │  AuditService   │
│  (JPA/SQL)  │      │  (AuditLog DB)  │
└─────────────┘      └─────────────────┘
     │
     ▼
┌────────────────────┐
│  H2 (dev) /        │
│  PostgreSQL (prod) │
└────────────────────┘
```

## CI/CD Pipeline Flow

```
Developer Push
      │
      ├─ feature/** or PR ──► ci.yml
      │                          ├─ Unit Tests (JUnit5 + Mockito)
      │                          ├─ JaCoCo (≥70% line coverage)
      │                          ├─ SonarCloud SAST
      │                          ├─ OWASP Dependency-Check
      │                          └─ Hadolint (PR only)
      │
      └─ main ──────────────► ci.yml + cd.yml
                                  ├─ All CI checks
                                  ├─ Docker multi-arch build
                                  ├─ Push to GHCR
                                  ├─ Deploy staging (SSH)
                                  ├─ Smoke test staging
                                  └─ Blue/Green deploy production

Tags (v*.*.*) ──────────────► release.yml
                                  ├─ Full test suite
                                  ├─ Docker tag + latest
                                  └─ GitHub Release + JAR attachment

Weekly ─────────────────────► scheduled-security.yml
                                  ├─ Trivy container scan
                                  └─ CodeQL Java SAST
```

## Key Design Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Cache | Caffeine | In-process, fast, production-grade |
| DB migrations | Flyway | Version-controlled, reproducible |
| Error format | RFC 9457 ProblemDetail | Standard, parseable by clients |
| Audit writes | @Async + separate TX | Never blocks main transaction |
| Docker | Layered JAR | Optimal build cache |
| K8s deploy | Helm + Kustomize | Flexible env overrides |
| GitOps | ArgoCD | Declarative, auditable deployments |
