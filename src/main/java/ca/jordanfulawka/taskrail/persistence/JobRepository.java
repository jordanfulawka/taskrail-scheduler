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
                rs.getObject("run_at", OffsetDateTime.class).toInstant()
        );
    }

    public Job insert(String type, JsonNode payload, Instant runAt) {
        OffsetDateTime runAtValue = runAt == null ? null : runAt.atOffset(ZoneOffset.UTC);
        return jdbcTemplate.queryForObject(
                "INSERT INTO jobs (type, payload, run_at) VALUES (?, ?::jsonb, COALESCE(?::timestamptz, now())) RETURNING *",
                jobMapper, type, jsonMapper.writeValueAsString(payload), runAtValue);
    }

    public Job findById(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM jobs WHERE id = ?", jobMapper, id
        );
    }

    public List<Job> findByStatus(JobStatus status) {
        return jdbcTemplate.query(
                "SELECT * FROM jobs where status = ?", jobMapper, status.name()
        );
    }

    public int markSucceeded(long id) {
        return jdbcTemplate.update("UPDATE jobs SET status = 'SUCCEEDED', updated_at = now()" +
                " WHERE id = ?", id);
    }

    public int markFailed(long id) {
        return jdbcTemplate.update("UPDATE jobs SET status = 'FAILED', updated_at = now() " +
                "WHERE id = ?", id);
    }

    public Optional<Job> claimNext() {
        List<Job> jobList = jdbcTemplate.query("UPDATE jobs SET status = 'RUNNING', updated_at = now() WHERE id = " +
                "(SELECT id FROM jobs WHERE status = 'PENDING' AND run_at <= now() ORDER BY run_at, id LIMIT 1 " +
                "FOR UPDATE SKIP LOCKED) RETURNING *", jobMapper);

        return jobList.stream().findFirst();
    }





}
