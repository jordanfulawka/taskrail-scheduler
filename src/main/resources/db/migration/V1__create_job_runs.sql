CREATE TABLE job_runs (
    id BIGSERIAL PRIMARY KEY,
    job_type TEXT NOT NULL,
    payload JSONB,
    status TEXT NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at TIMESTAMPTZ
)