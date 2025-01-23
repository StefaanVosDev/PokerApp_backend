package be.kdg.poker.exceptions;

public class InvalidWinnerException extends RuntimeException {
    public InvalidWinnerException(String message) {
        super(message);
    }
}
