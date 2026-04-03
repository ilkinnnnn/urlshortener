package io.github.ilkinnnnn.urlshortener.mapper;

import io.github.ilkinnnnn.urlshortener.model.entity.Url;
import io.github.ilkinnnnn.urlshortener.model.response.UrlResponse;
import io.github.ilkinnnnn.urlshortener.model.response.UrlResponseWithUserId;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UrlMapper {
    UrlResponse urlResponse(Url url);
    UrlResponseWithUserId urlResponseWithUserId(Url url);
}
