INSERT INTO jobs (type, payload)
SELECT
    (ARRAY['sleep', 'flaky'])[1 + floor(random() * 2)::int],
    jsonb_build_object('n', i)
FROM generate_series(1, 50) AS i;


-- Get-Content scripts\seed-jobs.sql | docker compose exec -T postgres psql -U taskrail -d taskrail