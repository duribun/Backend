package duribun.be.global.exception;

// MapService 내부에서만 발생/swallow되고 컨트롤러까지 전파되지 않으므로 GlobalExceptionHandler에는 등록하지 않는다.
public class RegionMappingNotFoundException extends RuntimeException {

    public RegionMappingNotFoundException(String message) {
        super(message);
    }
}
