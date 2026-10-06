package ca.jordanfulawka.taskrail.repository;

import ca.jordanfulawka.taskrail.model.JobRun;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JobRunRepository {

    private final JdbcTemplate jdbc;

    public JobRunRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(String jobType, String payload) {
        Long id = jdbc.queryForObject("INSERT INTO job_runs (job_type, payload) VALUES (?, ?::jsonb) RETURNING id", Long.class, jobType, payload);
        return Objects.requireNonNull(id);
    }

    public Optional<JobRun> claimNext() {
        List<JobRun> rows = jdbc.query("""
                UPDATE job_runs SET status = 'RUNNING'
                WHERE id = (
                    SELECT id FROM job_runs
                    WHERE status = 'PENDING'
                    ORDER BY id
                    LIMIT 1
                    FOR UPDATE SKIP LOCKED
                )
                RETURNING id, job_type, payload, status, created_at
                """,
                (rs, i) -> new JobRun(
                        rs.getLong("id"),
                        rs.getString("job_type"),
                        rs.getString("payload"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toInstant()));
        return rows.stream().findFirst();
    }

    public void markSucceeded(long id) {
        jdbc.update("UPDATE job_runs SET status = 'SUCCEEDED', finished_at = now() WHERE id = ? AND status = 'RUNNING'", id);
    }

    public void markFailed(long id, String error) {
        jdbc.update("UPDATE job_runs SET status = 'FAILED', finished_at = now(), last_error = ? WHERE id = ? AND status = 'RUNNING'", error, id);
    }




}
