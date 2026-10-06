package ca.jordanfulawka.taskrail.model;

import java.time.Instant;

public record JobRun(long id, String jobType, String payload, String status, Instant createdAt) {
}
