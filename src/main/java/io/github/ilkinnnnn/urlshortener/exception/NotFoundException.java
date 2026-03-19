package io.github.ilkinnnnn.urlshortener.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotFoundException extends ResponseStatusException {
    public NotFoundException(@Nullable String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }
}
