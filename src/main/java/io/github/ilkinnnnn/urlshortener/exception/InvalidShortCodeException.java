package io.github.ilkinnnnn.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidShortCodeException extends RuntimeException implements AddMessageToErrorResponse {
    public InvalidShortCodeException(){
        super("Invalid pattern for shortcode, correct format is 6 upper or lover case letter or digit");
    }
}
