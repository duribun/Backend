package duribun.be.global.exception;

public class AlreadyPurchasedException extends RuntimeException {

    public AlreadyPurchasedException(String message) {
        super(message);
    }
}
