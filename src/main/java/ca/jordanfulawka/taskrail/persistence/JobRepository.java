package ca.jordanfulawka.taskrail.persistence;

import ca.jordanfulawka.taskrail.core.Job;
import ca.jordanfulawka.taskrail.core.JobStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
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

    public JobRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper) {
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

    public void markSucceeded(long id, String workerName) {
        jdbcTemplate.update("UPDATE jobs SET status = 'SUCCEEDED', updated_at = now()" +
                " WHERE id = ? AND locked_by = ?", id, workerName);
    }

    public void markFailed(long id, String lastError, String workerName) {
        jdbcTemplate.update("UPDATE jobs SET status = 'FAILED', updated_at = now(), last_error = ? " +
                "WHERE id = ? and locked_by = ?", lastError, id, workerName);
    }

    public void markPending(long id, String lastError, String workerName) {
        jdbcTemplate.update("UPDATE jobs SET status = 'PENDING', updated_at = now(), attempts = attempts + 1, last_error = ? " +
                "WHERE id = ? and locked_by = ?", lastError, id, workerName);
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
