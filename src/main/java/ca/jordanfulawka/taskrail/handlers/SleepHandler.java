package ca.jordanfulawka.taskrail.handlers;

import tools.jackson.databind.JsonNode;

public class SleepHandler implements JobHandler {

    @Override
    public String type() {
        return "sleep";
    }

    @Override
    public void handle(JsonNode payload) throws Exception {

        JsonNode sleepTime = payload.path("ms");
        if(sleepTime.isMissingNode()) {
            throw new RuntimeException("Sleep job was not given a sleep time");
        }
        if(!sleepTime.canConvertToInt()) {
            throw new RuntimeException("Invalid sleep value");
        }
        System.out.printf("""
                Thread %s sleeping for %f seconds...
                """, Thread.currentThread().getName(), (1.0)*sleepTime.intValue()/1000);
        Thread.sleep(sleepTime.intValue());
    }
}
