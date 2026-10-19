package ca.jordanfulawka.taskrail.exceptions;

public class PermanentJobException extends RuntimeException {

    public PermanentJobException() {}

    public PermanentJobException(String message) {
        super(message);
    }
}
