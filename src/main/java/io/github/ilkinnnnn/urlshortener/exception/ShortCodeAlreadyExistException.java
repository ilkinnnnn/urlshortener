package io.github.ilkinnnnn.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ShortCodeAlreadyExistException extends RuntimeException implements AddMessageToErrorResponse {
    public ShortCodeAlreadyExistException() {
        super("Short code already exists");
    }
}
