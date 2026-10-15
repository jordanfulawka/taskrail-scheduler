package ca.jordanfulawka.taskrail.core;

import tools.jackson.databind.JsonNode;

import java.time.Instant;

public record Job(
        long id,
        String type,
        JobStatus status,
        JsonNode payload,
        Instant runAt
//        int attempts,
//        int maxAttempts,
//        String lockedBy,
//        Instant lockedUntil,
//        String lastError
) {
}
