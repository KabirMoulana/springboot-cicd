-- Seed data for dev/test (H2 only; suppressed in prod)
INSERT INTO tasks (title, description, status, priority, created_at, updated_at)
VALUES
  ('Set up CI/CD pipeline',     'Configure GitHub Actions for automated builds',    'DONE',        'HIGH',     NOW(), NOW()),
  ('Write unit tests',          'Add JUnit 5 tests for all service methods',        'IN_PROGRESS', 'HIGH',     NOW(), NOW()),
  ('Dockerize application',     'Create multi-stage Dockerfile',                    'DONE',        'MEDIUM',   NOW(), NOW()),
  ('Add Kubernetes manifests',  'Create Deployment, Service, Ingress YAMLs',        'TODO',        'MEDIUM',   NOW(), NOW()),
  ('Configure Prometheus',      'Set up metrics scraping and alerting rules',       'TODO',        'LOW',      NOW(), NOW()),
  ('Implement rate limiting',   'Add bucket4j rate limiting per IP/user',           'TODO',        'LOW',      NOW(), NOW()),
  ('Add OpenAPI docs',          'Integrate springdoc-openapi for Swagger UI',       'TODO',        'MEDIUM',   NOW(), NOW()),
  ('Security hardening',        'OWASP Top 10 mitigations review',                 'IN_PROGRESS', 'CRITICAL', NOW(), NOW());
