package be.kdg.poker.exceptions;

public class PlayersHandNotFoundException extends RuntimeException {
    public PlayersHandNotFoundException(String message) {
        super(message);
    }
}
