package gadgetarium.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RuntimeException {

    public BadRequestException(HttpStatus badRequest, String s) {
    }

    public BadRequestException(String message) {
        super(message);
    }
}
