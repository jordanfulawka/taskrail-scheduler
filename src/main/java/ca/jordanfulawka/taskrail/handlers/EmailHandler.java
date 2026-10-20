package ca.jordanfulawka.taskrail.handlers;

import ca.jordanfulawka.taskrail.exceptions.InvalidPayloadException;
import ca.jordanfulawka.taskrail.exceptions.PermanentJobException;
import ca.jordanfulawka.taskrail.exceptions.RetryableJobException;
import tools.jackson.databind.JsonNode;

import java.util.Random;

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

        if(toField.isMissingNode() || subjectField.isMissingNode() || bodyField.isMissingNode()
        || toField.isNull() || subjectField.isNull() || bodyField.isNull()) {
            throw new InvalidPayloadException("Payload missing fields");
        }

        if(!toField.isString() || !subjectField.isString() || !bodyField.isString()) {
            throw new InvalidPayloadException("Invalid payload type");
        }

        String toFieldStr = toField.asString();
        if(!toFieldStr.contains("@")) {
            throw new PermanentJobException("Invalid email");
        }
        if(toFieldStr.contains("@bounce.test")) {
            throw new PermanentJobException("550 mailbox does not exist");
        }

        Random random = new Random();

        int failChance = random.nextInt(100);
        if(failChance < 2) throw new RetryableJobException("SMTP timeout");
        failChance = random.nextInt(100);
        if(failChance < 2) throw new RetryableJobException("421 try again later");

        int sendTime = random.nextInt(1300);
        Thread.sleep(200 + sendTime);

    }
}
