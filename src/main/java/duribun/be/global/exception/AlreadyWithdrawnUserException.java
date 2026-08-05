package duribun.be.global.exception;

public class AlreadyWithdrawnUserException extends RuntimeException {

    public AlreadyWithdrawnUserException(String message) {
        super(message);
    }
}
