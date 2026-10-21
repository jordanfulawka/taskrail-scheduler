package ca.jordanfulawka.taskrail.persistence;

import ca.jordanfulawka.taskrail.core.JobStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JobRunRepository {

    private final JdbcTemplate jdbcTemplate;

    public JobRunRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long startRun(long jobId, int attempt, String workerName) {
        return jdbcTemplate.queryForObject(
                "INSERT into job_runs_log (job_id, attempt, worker_name) VALUES (?, ?, ?) RETURNING id", Long.class, jobId, attempt, workerName
        );
    }

    public void updateRun(long id, JobStatus outcome) {
        jdbcTemplate.update("UPDATE job_runs_log " +
                "SET finished_at = now(), outcome = ? WHERE id = ?", outcome.name(), id);
    }

    public void updateRun(long id, JobStatus outcome, String errorMessage, String errorType) {
        jdbcTemplate.update("UPDATE job_runs_log " +
                        "SET finished_at = now(), outcome = ?, error_message = ?, error_type = ? WHERE id = ?",
                outcome.name(), errorMessage, errorType,id);
    }
}
