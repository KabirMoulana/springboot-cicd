.PHONY: help test build docker-build docker-up docker-down smoke-test k8s-staging k8s-prod

SHELL := /bin/bash
IMAGE_TAG ?= local

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

test: ## Run full test suite with coverage
	./mvnw verify -B

build: ## Build JAR (skip tests)
	./mvnw package -B -DskipTests

docker-build: ## Build Docker image
	docker build -t springboot-cicd:$(IMAGE_TAG) .

docker-up: ## Start full local stack
	./scripts/local-dev.sh start

docker-down: ## Stop local stack
	./scripts/local-dev.sh stop

smoke-test: ## Run smoke tests against localhost
	./scripts/smoke-test.sh http://localhost:8080

k8s-staging: ## Deploy to staging via Helm
	helm upgrade springboot-cicd-staging helm/springboot-cicd \
		-f helm/springboot-cicd/values.yaml \
		-f helm/springboot-cicd/values-staging.yaml \
		--namespace springboot-cicd-staging \
		--create-namespace --wait --atomic

k8s-prod: ## Deploy to production via Helm (requires confirmation)
	@read -p "Deploy to PRODUCTION? (yes/no): " confirm && [ "$$confirm" = "yes" ] || exit 1
	helm upgrade springboot-cicd-prod helm/springboot-cicd \
		-f helm/springboot-cicd/values.yaml \
		-f helm/springboot-cicd/values-production.yaml \
		--namespace springboot-cicd \
		--create-namespace --wait --atomic
