package ca.jordanfulawka.taskrail.handlers;

import tools.jackson.databind.JsonNode;

public interface JobHandler {

    String type();

    void handle(JsonNode payload) throws Exception;
}
