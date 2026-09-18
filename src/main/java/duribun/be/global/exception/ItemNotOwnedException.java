package duribun.be.global.exception;

public class ItemNotOwnedException extends RuntimeException {

    public ItemNotOwnedException(String message) {
        super(message);
    }
}
