package io.github.ilkinnnnn.urlshortener.mapper;

import io.github.ilkinnnnn.urlshortener.model.entity.Url;
import io.github.ilkinnnnn.urlshortener.model.response.UrlResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UrlMapper {
    public UrlResponse urlResponse(Url url);
}
