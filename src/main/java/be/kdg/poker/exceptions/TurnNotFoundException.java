package be.kdg.poker.exceptions;

public class TurnNotFoundException extends RuntimeException {
    public TurnNotFoundException(String message) {
        super(message);
    }
}