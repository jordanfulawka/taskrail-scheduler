package ca.jordanfulawka.taskrail.handlers;

import ca.jordanfulawka.taskrail.exceptions.PermanentJobException;
import ca.jordanfulawka.taskrail.exceptions.RetryableJobException;
import tools.jackson.databind.JsonNode;

import java.util.Random;

record EmailSchema(String to, String subject, String body) {};


public class EmailHandler implements JobHandler {

    @Override
    public String type() {
        return "email";
    }

    public void validate(JsonNode payload) {
        JsonValidator.parse(payload, EmailSchema.class);
    }

    @Override
    public void handle(JsonNode payload) throws Exception {

        EmailSchema email = JsonValidator.parse(payload, EmailSchema.class);

        if(!email.to().contains("@")) {
            throw new PermanentJobException("Invalid email");
        }
        if(email.to().contains("@bounce.test")) {
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
