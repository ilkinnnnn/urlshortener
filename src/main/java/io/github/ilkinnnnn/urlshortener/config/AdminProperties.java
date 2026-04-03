package io.github.ilkinnnnn.urlshortener.config;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.admin")
@Getter
@Setter
public class AdminProperties {
    @NotBlank private String username;
    @NotBlank private String password;
}
