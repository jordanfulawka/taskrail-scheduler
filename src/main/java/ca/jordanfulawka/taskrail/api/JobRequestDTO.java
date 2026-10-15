package ca.jordanfulawka.taskrail.api;

import tools.jackson.databind.JsonNode;

import java.time.Instant;

public record JobRequestDTO(String type, JsonNode payload, Instant runAt) {
}
