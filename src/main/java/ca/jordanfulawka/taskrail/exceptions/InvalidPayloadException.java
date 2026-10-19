package ca.jordanfulawka.taskrail.exceptions;

public class InvalidPayloadException extends PermanentJobException {

    public InvalidPayloadException() {}

    public InvalidPayloadException(String message) {
        super(message);
    }
}
