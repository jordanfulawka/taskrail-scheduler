package ca.jordanfulawka.taskrail.handlers;

import ca.jordanfulawka.taskrail.exceptions.InvalidPayloadException;
import tools.jackson.databind.JsonNode;

record SleepSchema(int ms) {};

public class SleepHandler implements JobHandler {

    @Override
    public String type() {
        return "sleep";
    }

    @Override
    public void validate(JsonNode payload) {
        JsonValidator.parse(payload, SleepSchema.class);
    }

    @Override
    public void handle(JsonNode payload) throws Exception {

        SleepSchema sleep = JsonValidator.parse(payload, SleepSchema.class);

        if(sleep.ms() < 0) {
            throw new InvalidPayloadException("Sleep ms must be >= 0");
        }

        System.out.printf("""
                Thread %s sleeping for %f seconds...
                """, Thread.currentThread().getName(), (1.0)* sleep.ms()/1000);
        Thread.sleep(sleep.ms());
    }
}
