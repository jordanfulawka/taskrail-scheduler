package ca.jordanfulawka.taskrail.exceptions;

public class RetryableJobException extends RuntimeException {


    public RetryableJobException(String message) {
        super(message);
    }


}
