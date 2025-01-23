package be.kdg.poker.exceptions;

public class FriendAlreadyAddedException extends RuntimeException {
    public FriendAlreadyAddedException(String message) {
        super(message);
    }
}
