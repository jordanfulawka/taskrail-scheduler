package ca.jordanfulawka.taskrail.handlers;

import tools.jackson.databind.JsonNode;

import java.util.Random;

public class FlakyHandler implements JobHandler {

    @Override
    public String type() {
        return "flaky";
    }

    @Override
    public void handle(JsonNode payload) throws Exception {

        JsonNode failRate = payload.path("failRate");
        if(failRate.isMissingNode()) {
            throw new RuntimeException("failRate parameter not given");
        }
        if(!failRate.isNumber()) {
            throw new RuntimeException("Invalid payload type");
        }

        Random random = new Random();
        int randomValue = random.nextInt();

        if(randomValue < failRate.doubleValue() * 100) {
            throw new RuntimeException("Flaky is throwing a controlled exception");
        }
    }
}
