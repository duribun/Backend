package duribun.be.global.exception;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidSocialTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSocialToken(InvalidSocialTokenException e) {
        return respond(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        return respond(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(UnsupportedProviderException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedProvider(UnsupportedProviderException e) {
        return respond(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RegionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRegionNotFound(RegionNotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InsufficientPointException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPoint(InsufficientPointException e) {
        return respond(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(AlreadyWithdrawnUserException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyWithdrawnUser(AlreadyWithdrawnUserException e) {
        return respond(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(AttractionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAttractionNotFound(AttractionNotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(TourApiCallException.class)
    public ResponseEntity<ErrorResponse> handleTourApiCallException(TourApiCallException e) {
        return respond(HttpStatus.BAD_GATEWAY, e.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(OptimisticLockingFailureException e) {
        return respond(HttpStatus.CONFLICT, "다른 요청에 의해 처리 중입니다. 잠시 후 다시 시도해주세요");
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFound(ItemNotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(AlreadyPurchasedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyPurchased(AlreadyPurchasedException e) {
        return respond(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ItemNotOwnedException.class)
    public ResponseEntity<ErrorResponse> handleItemNotOwned(ItemNotOwnedException e) {
        return respond(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("잘못된 요청입니다");
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message));
    }
}
