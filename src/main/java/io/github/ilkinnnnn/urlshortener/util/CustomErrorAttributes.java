package io.github.ilkinnnnn.urlshortener.util;

import io.github.ilkinnnnn.urlshortener.exception.AddMessageToErrorResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {
    @Override
    public @NonNull Map<String, Object> getErrorAttributes(
            @NonNull WebRequest webRequest,
            @NonNull ErrorAttributeOptions options) {

        Throwable error = getError(webRequest);

        if (error instanceof AddMessageToErrorResponse) {
            options = options.including(ErrorAttributeOptions.Include.MESSAGE);
        }

        return super.getErrorAttributes(webRequest, options);
    }
}
