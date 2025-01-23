package be.kdg.poker.exceptions;

public class CannotAddOwnAccountException extends RuntimeException {
    public CannotAddOwnAccountException(String message) {
        super(message);
    }
}
