package coffe.baseproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InvalidJwtAuthenticationException extends AuthenticationException {

    public InvalidJwtAuthenticationException() {
        super("It is not allowed to persist a null object!");
    }

    public InvalidJwtAuthenticationException(String message) {
        super(message);
    }
}
