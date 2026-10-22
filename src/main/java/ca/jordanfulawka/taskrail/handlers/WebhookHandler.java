package ca.jordanfulawka.taskrail.handlers;

import ca.jordanfulawka.taskrail.exceptions.InvalidPayloadException;
import tools.jackson.databind.JsonNode;

public class WebhookHandler implements JobHandler {

    @Override
    public String type() {
        return "webhook";
    }

    @Override
    public void validate(JsonNode payload) {
        // TODO
    }

    @Override
    public void handle(JsonNode payload) throws Exception {

        JsonNode urlField = payload.path("url");
        JsonNode eventField = payload.path("event");

        if(urlField.isMissingNode() || eventField.isMissingNode()
        || urlField.isNull() || eventField.isNull()) {
            throw new InvalidPayloadException("Payload missing fields");
        }

        if(!urlField.isString() || !eventField.isString()) {
            throw new InvalidPayloadException("Invalid payload type");
        }
    }
}
