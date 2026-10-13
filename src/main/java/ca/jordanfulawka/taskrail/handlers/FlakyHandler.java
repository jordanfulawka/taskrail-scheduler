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

        Random random = new Random();
        int randomInt = random.nextInt(100);

        if(randomInt < 60) throw new RuntimeException("Flaky throwing an exception");
    }
}
