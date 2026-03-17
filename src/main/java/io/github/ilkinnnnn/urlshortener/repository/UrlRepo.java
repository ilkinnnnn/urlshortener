package io.github.ilkinnnnn.urlshortener.repository;

import io.github.ilkinnnnn.urlshortener.model.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepo extends JpaRepository<Url, Long> {
}
