BEGIN;

  -- 50 valid emails, due immediately
INSERT INTO jobs (type, payload)
SELECT 'email',
       jsonb_build_object(
               'to', 'user' || i || '@example.com',
               'subject', 'Test email #' || i,
               'body', 'Hello user ' || i || ', this is a mock email.')
FROM generate_series(1, 50) AS i;

-- 5 valid emails scheduled 1-5 minutes in the future
INSERT INTO jobs (type, payload, run_at)
SELECT 'email',
       jsonb_build_object(
               'to', 'later' || i || '@example.com',
               'subject', 'Scheduled email #' || i,
               'body', 'This one should wait.'),
       now() + i * interval '1 minute'
FROM generate_series(1, 5) AS i;

-- Edge cases: each should fail permanently (once), not retry
INSERT INTO jobs (type, payload) VALUES
                                     ('email', '{"subject": "Missing to", "body": "x"}'),
                                     ('email', '{"to": "a@example.com", "body": "Missing subject"}'),
                                     ('email', '{"to": "a@example.com", "subject": "Missing body"}'),
                                     ('email', '{"to": null, "subject": "Null to", "body": "x"}'),
                                     ('email', '{"to": 12345, "subject": "Numeric to", "body": "x"}'),
                                     ('email', '{"to": "a@example.com", "subject": "", "body": "Empty subject"}'),
                                     ('email', '{"to": "a@example.com", "subject": "   ", "body": "Whitespace subject"}'),
                                     ('email', '{"to": "not-an-email", "subject": "No @", "body": "x"}'),
                                     ('email', '{"to": "@", "subject": "Just @", "body": "x"}'),
                                     ('email', '{"to": "ghost@bounce.test", "subject": "Bounce", "body": "Fails once bounces are simulated"}'),
                                     ('email', '{}');

COMMIT;

-- docker compose exec -T <db-service> psql -U <user> -d <db> < scripts/seed_email_jobs.sql