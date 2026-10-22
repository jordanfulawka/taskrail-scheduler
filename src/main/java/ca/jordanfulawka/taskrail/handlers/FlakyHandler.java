package ca.jordanfulawka.taskrail.handlers;

import ca.jordanfulawka.taskrail.exceptions.InvalidPayloadException;
import ca.jordanfulawka.taskrail.exceptions.RetryableJobException;
import tools.jackson.databind.JsonNode;

import java.util.Random;

record FlakySchema(double failRate) {};

public class FlakyHandler implements JobHandler {

    @Override
    public String type() {
        return "flaky";
    }

    @Override
    public void validate(JsonNode payload) {
        JsonValidator.parse(payload, FlakySchema.class);
    }

    @Override
    public void handle(JsonNode payload) throws Exception {

        FlakySchema flaky = JsonValidator.parse(payload, FlakySchema.class);

        if(flaky.failRate() < 0) {
            throw new InvalidPayloadException("Failrate must be >= 0)");
        }

        Random random = new Random();
        int randomValue = random.nextInt(100);

        if(randomValue < flaky.failRate() * 100) {
            throw new RetryableJobException("Flaky is throwing a controlled exception");
        }
    }
}
