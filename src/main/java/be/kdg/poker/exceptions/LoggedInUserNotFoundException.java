package be.kdg.poker.exceptions;

public class LoggedInUserNotFoundException extends RuntimeException {
    public LoggedInUserNotFoundException(String message) {
        super(message);
    }
}
