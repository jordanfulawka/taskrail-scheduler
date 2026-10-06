package ca.jordanfulawka.taskrail.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class JobRunRepository {

    private JdbcTemplate jdbc;

    public JobRunRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(String jobType, String payload) {
        Long id = jdbc.queryForObject("INSERT INTO job_runs (job_type, payload) VALUES (?, ?::jsonb) RETURNING id", Long.class, jobType, payload);
        return Objects.requireNonNull(id);
    }




}
