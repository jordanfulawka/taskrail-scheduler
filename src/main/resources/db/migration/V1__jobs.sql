CREATE TABLE jobs (
    id BIGSERIAL PRIMARY KEY,
    type TEXT NOT NULL,
    payload JSONB NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    run_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    attempts INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 3,
    locked_by TEXT,
    locked_until TIMESTAMPTZ,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX jobs_due_idx ON jobs (run_at) WHERE status = 'PENDING';

CREATE TABLE job_runs_log (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT REFERENCES jobs(id),
    attempt SMALLINT,
    worker_name TEXT,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at TIMESTAMPTZ,
    outcome TEXT,
    error_message TEXT,
    error_type TEXT
);