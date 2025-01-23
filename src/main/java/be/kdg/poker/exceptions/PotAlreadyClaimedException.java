package be.kdg.poker.exceptions;

public class PotAlreadyClaimedException extends RuntimeException {
    public PotAlreadyClaimedException(String message) {
        super(message);
    }
}
