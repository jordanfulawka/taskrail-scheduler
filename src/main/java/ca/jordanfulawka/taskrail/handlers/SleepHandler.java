package ca.jordanfulawka.taskrail.handlers;

import tools.jackson.databind.JsonNode;

public class SleepHandler implements JobHandler {

    private final int sleepTime;

    public SleepHandler(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public String type() {
        return "sleep";
    }

    @Override
    public void handle(JsonNode payload) throws Exception {
        Thread.sleep(sleepTime);
        System.out.println("Sleepy has completed!");
    }
}
