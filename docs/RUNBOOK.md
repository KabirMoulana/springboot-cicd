# Operations Runbook

## Health Checks

| Endpoint | Expected | Notes |
|----------|----------|-------|
| `GET /actuator/health` | `{"status":"UP"}` | Liveness |
| `GET /actuator/health/liveness` | `{"status":"UP"}` | K8s liveness probe |
| `GET /actuator/health/readiness` | `{"status":"UP"}` | K8s readiness probe |
| `GET /actuator/prometheus` | Prometheus metrics | Scraped by Prometheus |

## Incident Response

### App returning 5xx errors
1. Check pod logs: `kubectl logs -l app=springboot-cicd -n springboot-cicd --tail=100`
2. Check health: `curl http://<host>/actuator/health`
3. Check DB connections: look for `hikaricp_connections_pending > 0` in Grafana
4. Rollback if needed: `helm rollback springboot-cicd-prod -n springboot-cicd`

### High memory / OOM
1. Check JVM heap in Grafana dashboard panel "JVM Heap Usage"
2. Check for memory leak: `curl /actuator/metrics/jvm.memory.used`
3. Restart pod: `kubectl rollout restart deployment/springboot-cicd -n springboot-cicd`
4. If recurring: increase `resources.limits.memory` in Helm values

### Database connection exhaustion
1. Alert: `DBConnectionPoolExhausted` fires when pending > 5
2. Check active connections: Grafana → "Active DB Connections (HikariCP)"
3. Kill long-running queries in PostgreSQL if needed
4. Tune `hikari.maximum-pool-size` in application-prod.yml

### Slow responses (p95 > 1s)
1. Alert: `SlowResponses` fires
2. Check: `curl /actuator/metrics/http.server.requests`
3. Look for slow DB queries in logs (Hibernate SQL debug)
4. Check Caffeine cache hit rate: `curl /actuator/metrics/cache.gets`

## Deployment Procedures

### Standard deploy (CI/CD)
Push to `main` → CD pipeline handles automatically.

### Manual deploy to staging
```bash
# Trigger workflow manually
gh workflow run k8s-deploy.yml \
  -f environment=staging \
  -f image_tag=sha-abc1234
```

### Emergency hotfix deploy
```bash
# Build and push hotfix image
docker build -t ghcr.io/kabirmoulana/springboot-cicd:hotfix-$(date +%Y%m%d) .
docker push ghcr.io/kabirmoulana/springboot-cicd:hotfix-$(date +%Y%m%d)

# Deploy directly
helm upgrade springboot-cicd-prod helm/springboot-cicd \
  -f helm/springboot-cicd/values-production.yaml \
  --set image.tag=hotfix-$(date +%Y%m%d) \
  --namespace springboot-cicd \
  --atomic --wait
```

### Rollback
```bash
# Helm rollback (last known good release)
helm rollback springboot-cicd-prod --namespace springboot-cicd

# Specific revision
helm history springboot-cicd-prod -n springboot-cicd
helm rollback springboot-cicd-prod <REVISION> -n springboot-cicd
```

## Useful Commands

```bash
# Watch pod status
kubectl get pods -n springboot-cicd -w

# Tail logs
kubectl logs -f deployment/springboot-cicd -n springboot-cicd

# Port-forward locally
kubectl port-forward svc/springboot-cicd 8080:80 -n springboot-cicd

# Run smoke tests
./scripts/smoke-test.sh http://localhost:8080

# Check HPA
kubectl get hpa -n springboot-cicd

# Describe deployment
kubectl describe deployment springboot-cicd -n springboot-cicd
```
