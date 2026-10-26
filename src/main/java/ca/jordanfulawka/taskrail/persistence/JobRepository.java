package ca.jordanfulawka.taskrail.persistence;

import ca.jordanfulawka.taskrail.core.Job;
import ca.jordanfulawka.taskrail.core.JobStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class JobRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;
    private final RowMapper<Job> jobMapper;
    private final JobRunRepository jobRunRepository;

    public JobRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper, JobRunRepository jobRunRepository) {
        this.jobRunRepository = jobRunRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
        this.jobMapper = (rs, rowNum) -> new Job(
                rs.getLong("id"),
                rs.getString("type"),
                JobStatus.valueOf(rs.getString("status")),
                jsonMapper.readTree(rs.getString("payload")),
                rs.getObject("run_at", OffsetDateTime.class).toInstant(),
                rs.getInt("attempts"),
                rs.getInt("max_attempts"),
                rs.getString("locked_by"),
                rs.getObject("locked_until", OffsetDateTime.class) == null
                        ? null
                        : rs.getObject("locked_until", OffsetDateTime.class).toInstant(),
                rs.getString("last_error"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant(),
                rs.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    public Job insert(String type, JsonNode payload, Instant runAt) {
        OffsetDateTime runAtValue = runAt == null ? null : runAt.atOffset(ZoneOffset.UTC);
        return jdbcTemplate.queryForObject(
                "INSERT INTO jobs (type, payload, run_at) VALUES (?, ?::jsonb, COALESCE(?::timestamptz, now())) RETURNING *",
                jobMapper, type, jsonMapper.writeValueAsString(payload), runAtValue);
    }

    public Optional<Job> findById(long id) {
        List<Job> jobList = jdbcTemplate.query("SELECT * FROM jobs WHERE id = ?", jobMapper, id);
        return jobList.stream().findFirst();
    }

    public List<Job> findByStatus(JobStatus status) {
        return jdbcTemplate.query(
                "SELECT * FROM jobs where status = ?", jobMapper, status.name()
        );
    }

    @Transactional
    public void markSucceeded(long id, String workerName, long jobRunId) {
        jdbcTemplate.update("UPDATE jobs SET status = 'SUCCEEDED', updated_at = now()" +
                " WHERE id = ? AND locked_by = ?", id, workerName);
        jobRunRepository.updateRun(jobRunId, JobStatus.SUCCEEDED);
    }

    @Transactional
    public void markFailed(long id, String lastError, String workerName, long jobRunId) {
        jdbcTemplate.update("UPDATE jobs SET status = 'FAILED', updated_at = now(), last_error = ? " +
                "WHERE id = ? and locked_by = ?", lastError, id, workerName);
        jobRunRepository.updateRun(jobRunId, JobStatus.FAILED, lastError, "PERMANENT");
    }

    @Transactional
    public void markPending(long id, String lastError, String workerName, long jobRunId) {
        jdbcTemplate.update("UPDATE jobs SET status = 'PENDING', run_at = now() + interval '1 second' * 2^attempts, updated_at = now(), last_error = ? " +
                "WHERE id = ? and locked_by = ?", lastError, id, workerName);
        jobRunRepository.updateRun(jobRunId, JobStatus.FAILED, lastError, "TEMPORARY");
    }

    @Transactional
    public List<Long> reapRunningJobs() {
        List<Long> expiredIds = jdbcTemplate.queryForList("""
                UPDATE jobs
                SET status =
                CASE
                    WHEN attempts < max_attempts THEN 'PENDING'
                    ELSE 'FAILED'
                END,
                locked_until = null, locked_by = null, updated_at = now(), last_error = 'lease expired', run_at = 
                CASE
                    WHEN attempts < max_attempts THEN now() + interval '1 minute'
                    ELSE run_at
                END
                WHERE status = 'RUNNING' AND locked_until < now() RETURNING id
                """, Long.class);
        if(expiredIds.isEmpty()) return expiredIds;

        for(Long id : expiredIds) {
            jobRunRepository.updateReapedRun(id, JobStatus.FAILED, "Lease expired", "LEASE_EXPIRED");
        }
        return expiredIds;
    }

    public Optional<Job> claimNext(String workerName) {
        List<Job> jobList = jdbcTemplate.query("UPDATE jobs " +
                "SET status = 'RUNNING', locked_by = ?, " +
                "locked_until = now() + interval '30 seconds', updated_at = now(), attempts = attempts + 1 " +
                "WHERE id = " +
                "(SELECT id FROM jobs WHERE status = 'PENDING' AND run_at <= now() ORDER BY run_at, id LIMIT 1 " +
                "FOR UPDATE SKIP LOCKED) RETURNING *", jobMapper, workerName);
        return jobList.stream().findFirst();
    }


}
