package ca.jordanfulawka.taskrail.handlers;

import tools.jackson.databind.JsonNode;

public interface JobHandler {

    String type();

    boolean validate(JsonNode payload);

    void handle(JsonNode payload) throws Exception;

}
