# Contributing Guide

## Getting Started

```bash
git clone https://github.com/KabirMoulana/springboot-cicd.git
cd springboot-cicd
./mvnw verify            # Run full test suite
./scripts/local-dev.sh start  # Start full local stack
```

## Branch Naming

| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feature/<short-desc>` | `feature/add-jwt-auth` |
| Bug fix | `fix/<issue-or-desc>` | `fix/cache-eviction-bug` |
| CI/CD | `ci/<desc>` | `ci/add-trivy-scan` |
| Docs | `docs/<desc>` | `docs/update-runbook` |

## Commit Messages (Conventional Commits)

Format: `<type>(<scope>): <description>`

| Type | When to use |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `test` | Tests only |
| `ci` | CI/CD changes |
| `docs` | Documentation |
| `refactor` | No behaviour change |
| `perf` | Performance improvement |
| `chore` | Maintenance |

Examples:
```
feat(api): add bulk task creation endpoint
fix(cache): correct cache key for status updates
test(integration): add Postgres IT for search filters
ci: pin OWASP plugin to 10.0.4 to fix rate limit issue
```

## Pull Request Process

1. Create branch from `main`
2. Implement change with tests (≥70% coverage on changed files)
3. Run `./mvnw verify` — all tests must pass
4. Run `docker build .` — image must build cleanly
5. Open PR with the template filled out
6. PR checks must pass (CI + Hadolint + Conventional Commits)
7. One approval required before merge

## Code Standards

- Java 21 features encouraged (records, sealed classes, pattern matching)
- No `@SuppressWarnings` without a comment explaining why
- No hardcoded secrets — use `@Value` or environment variables
- All public service methods need a unit test
- All new endpoints need an integration test
- RFC 9457 `ProblemDetail` for all error responses
