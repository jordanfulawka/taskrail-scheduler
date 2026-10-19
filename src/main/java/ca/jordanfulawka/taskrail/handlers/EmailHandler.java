package ca.jordanfulawka.taskrail.handlers;

import tools.jackson.databind.JsonNode;

public class EmailHandler implements JobHandler {

    @Override
    public String type() {
        return "email";
    }

//    JsonNode payload expects fields: to, subject, body.
    @Override
    public void handle(JsonNode payload) throws Exception {

        JsonNode toField = payload.path("to");
        JsonNode subjectField = payload.path("subject");
        JsonNode bodyField = payload.path("body");

        if(toField.isMissingNode() || subjectField.isMissingNode() || bodyField.isMissingNode()) {
            throw new RuntimeException("Payload missing fields");
        }
        String toFieldStr = toField.asString();
        if(toFieldStr.contains("@")) {
            throw new RuntimeException("Invalid email");
        }

    }
}
