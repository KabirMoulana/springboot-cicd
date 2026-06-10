-- Flyway migration V1: create tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    status      VARCHAR(20)  NOT NULL DEFAULT 'TODO',
    priority    VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_task_status  ON tasks (status);
CREATE INDEX IF NOT EXISTS idx_task_created ON tasks (created_at);
