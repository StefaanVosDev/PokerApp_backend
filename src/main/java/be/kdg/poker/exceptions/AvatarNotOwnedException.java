package be.kdg.poker.exceptions;

public class AvatarNotOwnedException extends RuntimeException{
    public AvatarNotOwnedException(String message) {
        super(message);
    }
}
